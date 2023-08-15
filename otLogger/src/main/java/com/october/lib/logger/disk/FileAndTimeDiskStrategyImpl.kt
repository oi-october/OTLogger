package com.october.lib.logger.disk

import com.october.lib.logger.LogLevel
import com.october.lib.logger.common.LOG_HEARD_INFO
import com.october.lib.logger.common.getFreeStore
import com.october.lib.logger.common.getTotalStore
import com.october.lib.logger.util.debugLog
import java.io.File
import java.io.FilenameFilter
import java.text.SimpleDateFormat

/**
 * 文件+时间管理策略，同时具备[FileLogDiskStrategyImpl] 和 [TimeLogDiskStrategyImpl] 的部分特性
 *   - 默认日志文件夹最大可容纳 100M日志，超过[logDirectoryMaxStoreSizeOfMB]会按照时间顺序删除旧的日志，直到低于预定值
 *   - 默认文件名 默认文件名 log_年_月_日_时间段_时间戳.log
 *     eg: otLog_2023_02_12_16_17_11123223423423.log
 *
 * 什么时候创建新的日志文件？
 *   - 每个日志写满了会创建一个新的日志文件
 *   - 超过日志时间片段，会创建一个新的日志文件进行存储
 *   - 为了保护系统，以上都要当系统可用空闲空间大于最低限制的空闲空间[minFreeStoreOfMB]时，才会创建新的日志文件。
 */
open class FileAndTimeDiskStrategyImpl(
    val logDirectory: String = defaultLogDir,
    val minFreeStoreOfMB: Int = 200,
    val logDirectoryMaxStoreSizeOfMB: Int = 100,
    val logFileStoreSizeOfMB: Int = 5,
    val segment: LogTimeSegment = LogTimeSegment.ONE_HOUR
) : BaseLogDiskStrategy() {

    //时间管理
    private val timeLogDiskStrategy = TimeLogDiskStrategyImpl(logSegment = segment)
    private val fileLogDiskStrategy = FileLogDiskStrategyImpl(
        logDirectory = logDirectory,
        minFreeStoreOfMB = minFreeStoreOfMB,
        logDirectoryMaxStoreSizeOfMB = logDirectoryMaxStoreSizeOfMB,
        logFileStoreSizeOfMB = logFileStoreSizeOfMB
    )

    //日志文件名时间格式
    private val logFileNameDateFormat = SimpleDateFormat("yyyy_MM_dd")

    private var currentLogFilePathCache: FilePathCache? = null

    override fun createLogFile(printTime: Long, logLevel: LogLevel, logBody: String?): String? {
        val section = timeLogDiskStrategy.getLogSection(printTime, segment)
        var fileName = ""
        val currentLogFileCacheSize = currentLogFilePathCache?.getCurrentSize()
        if (currentLogFileCacheSize != null && currentLogFileCacheSize > logFileStoreSizeOfMB * 1024 * 1024) {
            //last log file is full
            fileName = getFileName(printTime, section.first)
        } else {
            //find last log file
            val logFileNamePrefix = getFileNamePrefix(printTime, section.first)
            val fileArray = File(getLogDir()).listFiles(FilenameFilter { _, name ->
                val isFilter =
                    name.startsWith(LogPrefix) && name.endsWith(LogSuffix) && name.startsWith(
                        logFileNamePrefix
                    )
                return@FilenameFilter isFilter
            })
            if (!fileArray.isNullOrEmpty()) {
                var fileList = fileArray.sortedBy {
                    it.name
                }
                val lastFile = fileList.last()
                if (lastFile.length() < logFileStoreSizeOfMB * 1024 * 1024) {
                    fileName = lastFile.name  //复用上一个没有写满的数据
                }
            }

            //上一个日志文件不可复用，创建新的日志文件
            if (fileName.isNullOrEmpty()) {
                fileName = getFileName(printTime, section.first)
            }
        }

        val path = getLogDir() + File.separator + fileName
        debugLog("create log file=${path}")
        val filePath = FilePathCache(
            section.second.first,
            section.second.second,
            logFileStoreSizeOfMB * 1024 * 1024L,
            path
        )
        currentLogFilePathCache = filePath
        return filePath.filePath
    }

    override fun isLogFilePathAvailable(
        logFilepath: String?,
        printTime: Long,
        logLevel: LogLevel,
        logBody: String?
    ): Boolean {
        return currentLogFilePathCache?.isMatch(printTime, logFilepath) == true
    }

    override fun isAllowCreateLogFile(printTime: Long): Boolean {
        fileLogDiskStrategy.checkAndClearLogDir()
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
        builder.append("all store size =${getTotalStore(getLogDir()) / 1024f / 1024f}MB")
        builder.append("free store size = ${getFreeStore(getLogDir()) / 1024f / 1024f}MB")
        builder.append("\n\n")
        return builder.toString()
    }

    override fun getLogDir(): String {
        return logDirectory
    }


    /**
     * 获取文件名称
     * @param logTime 日志打印时间
     * @param section 时间戳对应当前的开始小时和结束小时，[startHour，endHour],eg：[13,14]
     * @return 该日志对应的文件名 ，输出文件名格式：otLog_xxxx_xx_xx_startHour_endHour_timestamp.log
     *   比如按照日志片段(LogTimeSegment)是一个小时计算， 2023年11月20日，11：20分输出的日志对应的日志名称：
     *   otLog_2023_11_20_11_12_2131212312321.log
     */
    private fun getFileName(logTime: Long, section: Pair<Int, Int>): String {
        return "${getFileNamePrefix(logTime, section)}_${logTime}${LogSuffix}"
    }

    /**
     * 获取文件名前半段
     * @return 文件名前半段， eg: otLog_2023_11_20_11_12
     */
    private fun getFileNamePrefix(logTime: Long, section: Pair<Int, Int>): String {
        val logTimeStr = logFileNameDateFormat.format(logTime)
        var start = "${section.first}"
        if (start.length == 1) start = "0${start}"
        var end = "${section.second}"
        if (end.length == 1) end = "0${end}"
        return "${LogPrefix}${logTimeStr}_${start}_${end}"
    }


    private class FilePathCache(
        val startTime: Long,
        val endTime: Long,
        val logFileMaxSize: Long,
        val filePath: String
    ) {
        private val MAX_RESET_COUNT = 50
        private var curResetCount = 0
        private var currentSize = -1L

        private val logFile by lazy { File(filePath) }

        init {
            if (logFile.exists() && logFile.isFile) {
                currentSize = logFile.length()
            } else {
                currentSize = 0
            }
        }

        fun isMatch(currentTime: Long, logFilePath: String?): Boolean {
            if (curResetCount > MAX_RESET_COUNT || currentSize < 0) {  //每50次，查一次文件大小
                curResetCount = 0
                currentSize = logFile.length()
            }
            curResetCount++
            //当前文件大小 < 最大限制大小 + 文件路径相同 =  表示匹配
            return (currentSize < logFileMaxSize && currentTime in startTime until endTime && logFilePath == filePath)
        }

        fun getCurrentSize(): Long {
            return currentSize
        }

    }


}