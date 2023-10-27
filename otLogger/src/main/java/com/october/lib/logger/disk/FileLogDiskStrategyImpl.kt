package com.october.lib.logger.disk

import com.october.lib.logger.LogLevel
import com.october.lib.logger.common.LOG_HEARD_INFO
import com.october.lib.logger.common.getFreeStore
import com.october.lib.logger.common.getTotalStore
import com.october.lib.logger.util.debugLog
import com.october.lib.logger.util.errorLog
import java.io.File
import java.io.FilenameFilter
import java.text.SimpleDateFormat

/**
 * 日志文件管理策略，按存储管理日志文件
 *   - 默认每个日志文件5MB，参考[logFileStoreSizeOfMB]
 *   - 默认日志文件夹最大可容纳 100M日志，超过[logDirectoryMaxStoreSizeOfMB]会按照时间顺序删除旧的日志，直到低于预定值
 *   - 默认文件名 otLog_年_月_日_时_分_秒.log
 *     eg: otLog_2023_02_12_16_28_56.log
 *
 *
 * 什么时候创建新的日志文件？
 *   - 每个日志写满了会创建一个新的日志文件
 *   - 为了保护系统，以上都要当系统可用空闲空间大于最低限制的空闲空间[minFreeStoreOfMB]时，才会创建新的日志文件。
 *
 * @param logDirectory 日志文件夹
 * @param minFreeStoreOfMB 最小空闲空间（单位MB），当系统最小空闲存储空间低于该值时，不再创建新的日志文件
 * @param logDirectoryMaxStoreSizeOfMB 日志文件夹最大的存储容量（单位MB），所有的日志文件加起来的大小不得操过该值
 * @param logFileStoreSizeOfMB 每个日志文件容量（单位MB），只有上一个日志文件操过容量，才会创建下一个日志文件
 */
open class FileLogDiskStrategyImpl(
    val logDirectory: String = defaultLogDir,
    val minFreeStoreOfMB: Int = 200,
    val logDirectoryMaxStoreSizeOfMB: Int = 100,
    val logFileStoreSizeOfMB:Int = 5
) : BaseLogDiskStrategy() {


    //日志文件名时间格式
    private val logFileNameDateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
    private var currentFilePathCache: FilePathCache? = null

    //获取日志文件夹路径
    override fun getLogDir(): String {
        return logDirectory
    }

    override fun createLogFile(printTime: Long, logLevel: LogLevel, logBody: String?): String? {
        var fileName = ""
        val currentLogFileCacheSize = currentFilePathCache?.getCurrentSize()
        if (currentLogFileCacheSize != null && currentLogFileCacheSize > logFileStoreSizeOfMB * 1024 * 1024) {
            //last log file is full
            fileName = getFileName(printTime)
        } else {
            //find last log file
            val fileArray = File(getLogDir()).listFiles(FilenameFilter { _, name ->
                return@FilenameFilter(name.startsWith(getLogPrefix()) && name.endsWith(getLogSuffix()))
            })

            if(!fileArray.isNullOrEmpty()){
                var fileList = fileArray.sortedBy {
                    it.name
                }
                val lastFile = fileList.last()
                if(lastFile.length() < logFileStoreSizeOfMB *1024*1024){
                    fileName = lastFile.name
                }
            }
            //创建新的日志文件
            if(fileName.isNullOrEmpty()){
                fileName = getFileName(printTime)
            }
        }

        val path = getLogDir() + File.separator + fileName  //文件路径
        debugLog("create log file=${path}")
        val filePath = FilePathCache(logFileStoreSizeOfMB * 1024 * 1024L, path)
        currentFilePathCache = filePath
        return filePath.filePath
    }

    override fun isLogFilePathAvailable(
        logFilepath: String?,
        printTime: Long,
        logLevel: LogLevel,
        logBody: String?
    ): Boolean {
        return currentFilePathCache?.isMatch(logFilepath) == true
    }

    override fun isAllowCreateLogFile(printTime: Long): Boolean {
        checkAndClearLogDir()
        //检查空闲空间
        if (minFreeStoreOfMB > 0) {
            val freeStore = getFreeStore(getLogDir())
            debugLog("free store size=${freeStore / 1024f / 1024f}MB，min free store size=${minFreeStoreOfMB * 1024f / 1024f}MB")
            if (freeStore < minFreeStoreOfMB * 1024 * 1024) {
                return false
            }
        }
        return true
    }

    override fun logHeadInfo(): String? {
        val builder = StringBuilder()
        builder.append(LOG_HEARD_INFO)
        builder.append("all store size =${getTotalStore(getLogDir())/1024f/1024f}MB")
        builder.append("free store size = ${getFreeStore(getLogDir())/1024f/1024f}MB")
        builder.append("\n\n")
        return builder.toString()
    }


    /**
     * 检查并清理日志文件夹，如果文件夹超过[logDirMaxStore]，会删除旧的文件，直到低于[logDirMaxStore]
     */
    internal fun checkAndClearLogDir() {
        val logDirFile = File(getLogDir())
        // log dir exist？ read and write permission?
        if (!logDirFile.exists() || !logDirFile.isDirectory || !logDirFile.canRead() || !logDirFile.canWrite()) {
            errorLog(" log dir exist？ read and write permission?")
            return
        }
        val logFileArray = logDirFile.listFiles(FilenameFilter { _, name ->
            val name = name.trim()
            if (name.startsWith(getLogPrefix()) && name.endsWith(getLogSuffix())) {
                return@FilenameFilter true
            }
            return@FilenameFilter false
        })
        var logList = logFileArray.asList()
        if (logList.isNullOrEmpty()) {
            //no log file
            return
        }
        logList = logList.sortedBy {
            it.name
        }
        var size = 0L
        var outSizeIndex = -1
        val logDirMaxStore = logDirectoryMaxStoreSizeOfMB * 1024 * 1024
        for (i in logList.size - 1 downTo 0) {
            val logFile = logList.get(i)
            val length = logFile.length()
            size += length
            debugLog("file(${logFile.name}).size=${length/1024f/1024f}MB，size=${size/1024f/1024f}MB，maxSize=${logDirectoryMaxStoreSizeOfMB}MB")
            if (size > logDirMaxStore) { //超过了最大容量，当前和后面的日志文件都会被删除
                outSizeIndex = i
                break
            }
        }

        if (outSizeIndex < 0) {
            // no log file need delete
            return
        }
        for (j in 0 until outSizeIndex + 1) {
            val logFile = logList.get(j)
            val isSuccess = logFile.delete()
            debugLog("delete log file(${logFile.name}) success? ${isSuccess}")
        }
        debugLog("after clear invalid log file，log dir size=${size / 1024f / 1024f}MB")
    }

    /**
     * 获取文件名称
     * @param logTime 日志打印时间
     * @return 该日志对应的文件名 ，输出文件名格式：log_xxxx_xx_xx_currentHour_timestamp.log
     *   2023年11月20日，11：20分55秒 输出的日志对应的日志名称：otLog_yyyy_MM_dd_HH_mm_ss.log
     *   otLog_2023_11_20_11_20_55.log
     *
     */
    private fun getFileName(logTime: Long): String {
        val logTimeStr = logFileNameDateFormat.format(logTime)
        return "${getLogPrefix()}${logTimeStr}${getLogSuffix()}"
    }

    private class FilePathCache(val logFileMaxSize: Long, val filePath: String) {
        private val MAX_RESET_COUNT = 50
        private var curResetCount = 0
        private var currentSize = -1L

        private val logFile by lazy {
            File(filePath)
        }

        init {
            if(logFile.exists() && logFile.isFile){
                currentSize = logFile.length()
            }else{
                currentSize = 0
            }
        }

        fun isMatch(logFilePath: String?): Boolean {
            if (curResetCount > MAX_RESET_COUNT || currentSize < 0) {  //每50次，查一次文件大小
                curResetCount = 0
                currentSize = logFile.length()
            }
            curResetCount++
            //当前文件大小 < 最大限制大小， 表示匹配
            return currentSize < logFileMaxSize && filePath == logFilePath
        }

        fun getCurrentSize(): Long {
            return currentSize
        }


    }


}
