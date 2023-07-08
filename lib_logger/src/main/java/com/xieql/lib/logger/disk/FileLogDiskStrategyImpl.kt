package com.xieql.lib.logger.disk

import android.os.StatFs
import com.xieql.lib.logger.LogLevel
import com.xieql.lib.logger.common.LOG_HEARD_INFO
import com.xieql.lib.logger.core.appCtx
import com.xieql.lib.logger.util.debugLog
import java.io.File
import java.io.FilenameFilter
import java.text.FieldPosition
import java.text.SimpleDateFormat
import java.util.Arrays

/**
 * 日志文件管理策略，按存储管理日志文件
 *   - 默认每个日志文件5MB，参考[getLogFileMaxSizeOfMB]
 *   - 默认日志文件夹最大可容纳 100M日志，超过[getLogDirMaxStoreOfMB]会按照时间顺序删除旧的日志
 *   - 默认文件名 log_年_月_日_时_分_秒.log
 *     eg: log_2023_02_12_16_28_56.log
 *
 *
 * 什么时候创建新的日志文件？
 *   - 每个日志写满了会创建一个新的日志文件
 *   - 内存中的日志对象[currentLogFilePath]为null，也会创建一个新的文件，而不是复用上一个未写满的日志文件
 *   - 为了保护系统，当系统可用空闲空间小于最低限制的空闲空间[getMinFreeStoreOfMB]时，不会创建新的日志文件。
 *
 */
class FileLogDiskStrategyImpl :BaseFileLogDiskStrategy(){

    private companion object{
        const val LogPrefix = "log_"
        const val LogSuffix = ".log"
    }

    //日志文件名时间格式
    private val logFileNameDateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")

    private var currentLogFilePath:FilePath? = null

    private val defaultFileSize =  5L   //默认文件大小
    private val defaultLogDirSize = 100L //默认日志文件夹大小

    //默认存储地址
    private val defaultLogDir by lazy {
        val path = appCtx.getExternalFilesDir("")?.absolutePath+ File.separator+"log"
        val file = File(path)
        if(!file.exists()|| !file.isDirectory){
            file.mkdirs()
        }
        debugLog("日志存储路径:${file.absolutePath}")
        return@lazy file.absolutePath
    }

    //最少空闲空间 , 单位B
    private val defaultMinFreeStore by lazy {
        val total = getTotalStore()
        if(total>2*1024*1024*1024){
            debugLog("总存储 >2GB，至少遗留存储空间:${500} MB")
            return@lazy 500 * 1024 * 1024
        }else{
            val atLeast = total/4L
            debugLog("总存储 < 2GB，至少遗留存储空间:${atLeast/1024/1024} MB")
            return@lazy total/4L
        }
    }

    override fun getLogPrintPath(logLevel: LogLevel, logBody: String?, bodySize: Long): String? {
        val currentTime = System.currentTimeMillis()
        val logFilePath = currentLogFilePath
        if(logFilePath !=null && logFilePath.isMatch()){
            return logFilePath.filePath
        }else{
            val fileName = getFileName(currentTime)
            val path = getLogDir()+File.separator+fileName  //问价路径
            val filePath = FilePath(getLogFileMaxSizeOfMB()*1024*1024,path)

            if(!checkAndClearLogDir(getMinFreeStoreOfMB() * 1024 *1024)){
                return ""
            }
            //检查空闲空间
            if(getMinFreeStoreOfMB() > 0 ){
                val freeStore = getFreeStore(getLogDir())
                debugLog("当前空闲空间:${freeStore/1024}KB，最低空闲空间:${getMinFreeStoreOfMB()*1024}KB")
                if(freeStore < getMinFreeStoreOfMB() * 1024 *1024){
                    return ""
                }
            }
            val file = File(filePath.filePath)
            if (!file.exists() || !file.isFile) {  //创建新文件 并 添加文件头部内容
                if(file.createNewFile()){
                    file.appendText(getLogHeardInfo())  //写入文件头
                }
            }

            currentLogFilePath = filePath

            return filePath.filePath

        }
        return ""
    }

    override fun getCurrentLogFilePath(): String? {
        return currentLogFilePath?.filePath
    }

    //获取日志文件夹路径
    override fun getLogDir():String{
        return defaultLogDir
    }

    override fun getLogFileMaxSizeOfMB(): Long {
        return defaultFileSize
    }

    override fun getMinFreeStoreOfMB(): Long {
        return defaultMinFreeStore
    }

    override fun getLogDirMaxStoreOfMB(): Long {
        return defaultLogDirSize
    }

    /**
     * 检查并清理日志文件夹，如果文件夹超过[logDirMaxStore]，会删除旧的文件，直到低于[logDirMaxStore]
     * @return
     *  是否处理成功
     */
    open fun checkAndClearLogDir(logDirMaxStore:Long):Boolean{
        val logDirFile =  File(getLogDir())
        if(!logDirFile.exists() || !logDirFile.isDirectory) return true
        val logFileArray = logDirFile.listFiles(FilenameFilter { _, name ->
            if(name.startsWith(LogPrefix) && name.endsWith(LogSuffix)){
                return@FilenameFilter true
            }
            return@FilenameFilter false
        })
        val logList =  logFileArray.asList()
        logList.sortedBy {
            it.name
        }
        debugLog("排序后日志文件:\n${Arrays.toString(logList.toTypedArray())}")
        var size = 0L
        var startDelete = false
        for (i in logList.size-1 until 0){
            val logFile = logList.get(i)

            if(startDelete){  //删除后续日志文件，把后续的文件全部删除了
                logFile.delete()
                continue
            }

            size += logFile.length()
            if(size > logDirMaxStore){ //超过了最大容量，当前和后面的日志文件都会被删除
                startDelete =  true
                logFile.delete()  //删除当前
            }
        }

        debugLog("清理完成后日志文件总大小：${size/1024/1024} MB")

        return true
    }

    /**
     * 获取文件头部信息，创建新文件的时候写入
     * @return
     */
    open fun getLogHeardInfo():String{
         val builder = StringBuilder()
        builder.append(LOG_HEARD_INFO)
        builder.append("总存储:${getTotalStore()}")
        builder.append("空闲存储:${getFreeStore(getLogDir())}")
        builder.append("\n\n")
        return builder.toString()
    }

    /**
     * 获取文件名称
     * @param logTime 日志打印时间
     * @return 该日志对应的文件名 ，输出文件名格式：log_xxxx_xx_xx_currentHour_timestamp.log
     *   2023年11月20日，11：20分55秒 输出的日志对应的日志名称：log_yyyy_MM_dd_HH_mm_ss.log
     *   log_2023_11_20_11_20_55.log
     *
     */
    protected open fun getFileName(logTime: Long): String {
        val logTimeStr = logFileNameDateFormat.format(logTime)
        return "${LogPrefix}${logTimeStr}${LogSuffix}"
    }

    private class FilePath(val logFileMaxSize:Long,val filePath:String) {
        private val CheckTime = 100
        private var currentTime = 0
        private var currentSize = -1L

        private val logFile by lazy { File(filePath) }

        fun  isMatch():Boolean{
            if(currentTime > CheckTime || currentSize<0){  //检查200次，查一次文件大小
                currentTime = 0
                currentSize = logFile.length()
            }
            currentTime ++
            //当前文件大小 < 最大限制大小， 表示匹配
            return currentSize < logFileMaxSize
        }
    }

}

/**
 * 日志文件管理策略基类
 * 定义了可配置参数
 */
public abstract class BaseFileLogDiskStrategy:BaseLogDiskStrategy(){

    //文件存储路径
    abstract fun getLogDir():String

    /**
     * 设置每个日志文件最大空间大小 单位：MB
     * @return
     */
    abstract fun getLogFileMaxSizeOfMB():Long

    /**
     * 设置最小空闲存储空间 单位：MB
     * - 创建新文件时候， 必须满足 [系统空闲存储 > 最小空闲存储空间] 的时候，才能创建新的日志文件
     * @return
     * - 最小空闲存储空间 <= 0 : 表示不使用该参数作为创建日志文件的限制
     */
    abstract fun getMinFreeStoreOfMB():Long

    /**
     * 设置日志文件夹最大容量 :单位MB
     * - 只有日志文件夹中，[所有的日志文件大小之和 < 日志文件夹最大容量] 的时候，才能创建新的日志文件.
     * - [所有的日志文件大小之和 >= 日志文件夹最大容量]的时候，会触发回删操作
     * @return
     */
    abstract fun getLogDirMaxStoreOfMB():Long

    /**
     * 获取全部存储空间
     *  单位B
     */
    open fun getTotalStore():Long{
        val sf = StatFs(getLogDir())
        val blockSize = sf.blockSizeLong
        val blockCount = sf.blockCountLong
        val size = blockSize * blockCount
        debugLog("block大小:$blockSize,block数目: $blockCount , 总大小: ${size/1024}KB")
        return size
    }
    /**
     * 获取空余存储空间
     *  单位B
     */
    open fun getFreeStore(logDir:String):Long{
        val sf = StatFs(logDir)
        val blockSize = sf.availableBlocksLong
        val blockCount = sf.blockCountLong
        val size = blockSize * blockCount
        debugLog("block大小:$blockSize,block数目: $blockCount , 总大小: ${size/1024}KB")
        return size
    }

}


