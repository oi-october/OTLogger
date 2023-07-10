package com.xieql.lib.logger.disk

import android.graphics.Point
import com.xieql.lib.logger.LogLevel
import com.xieql.lib.logger.common.LOG_HEARD_INFO
import com.xieql.lib.logger.util.debugLog
import java.io.File
import java.text.SimpleDateFormat

/**
 * 文件+时间管理策略，同时具备[FileLogDiskStrategyImpl] 和 [TimeLogDiskStrategyImpl] 的特性
 *   - 默认日志文件夹最大可容纳 100M日志，超过[getLogDirMaxStoreOfMB]会按照时间顺序删除旧的日志，直到低于预定值
 *   - 默认文件名 默认文件名 log_年_月_日_时间段_创建时间戳.log
 *     eg: log_2023_02_12_16_17_1233644846.log
 *
 * 什么时候创建新的日志文件？
 *   - 每个日志写满了会创建一个新的日志文件
 *   - 超过日志时间片段，会创建一个新的日志文件进行存储
 *   - 为了保护系统，以上都要当系统可用空闲空间大于最低限制的空闲空间[getMinFreeStoreOfMB]时，才会创建新的日志文件。
 */
open class FileAndTimeDiskStrategyImpl : BaseLogDiskStrategy() {

    private companion object {
        const val LogPrefix = "log_"
        const val LogSuffix = ".log"
    }

    private val defaultFileSize =  5L   //默认文件大小
    private val defaultLogDirSize = 100L //默认日志文件夹大小

    //时间管理
    private val timeLogDiskStrategy = TimeLogDiskStrategyImpl()
    private val fileLogDiskStrategy = FileLogDiskStrategyImpl()

    //日志文件名时间格式
    private val logFileNameDateFormat = SimpleDateFormat("yyyy_MM_dd")

    private var currentLogFilePath: FilePath? = null

    override fun getLogPrintPath(logLevel: LogLevel, logBody: String?, bodySize: Long): String? {
        val currentTime = System.currentTimeMillis()
        val logFilePath = currentLogFilePath
        if (logFilePath != null && logFilePath.isMatch(currentTime)) {
            return logFilePath.filePath
        } else {
            val section = timeLogDiskStrategy.getLogSection(currentTime, getSegment())
            val fileName = getFileName(currentTime, section.first)
            val path = getLogDir() + File.separator + fileName
            val filePath = FilePath(section.second.first, section.second.second,getLogFileMaxSizeOfMB()*1024*1024, path)
            if(!fileLogDiskStrategy.checkAndClearLogDir(getLogDirMaxStoreOfMB() * 1024 *1024)){
                return ""
            }
            //检查空闲空间
            if(getMinFreeStoreOfMB() > 0 ){
                val freeStore = fileLogDiskStrategy.getFreeStore(getLogDir())
                debugLog("当前空闲空间:${freeStore/1024}KB，最低空闲空间:${getMinFreeStoreOfMB()*1024}KB")
                if(freeStore < getMinFreeStoreOfMB() * 1024 *1024){
                    return ""
                }
            }


            val file = File(filePath.filePath)
            if(!file.parentFile.exists()){
                file.parentFile.mkdirs()  //创建文件夹
            }
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
    open fun getLogDir(): String {
        return fileLogDiskStrategy.defaultLogDir
    }
    //设置每个日志文件最大空间大小 单位：MB
    open fun  getLogFileMaxSizeOfMB(): Long {
        return defaultFileSize
    }
    //设置最小空闲存储空间 单位：MB
    open fun getMinFreeStoreOfMB(): Long {
        return fileLogDiskStrategy.defaultMinFreeStoreOfMB
    }
    //设置日志文件夹最大容量 :单位MB
    open fun getLogDirMaxStoreOfMB(): Long {
        return defaultLogDirSize
    }

    //获取日志片段
    open fun getSegment(): BaseTimeLogDiskStrategy.LogTimeSegment {
        return BaseTimeLogDiskStrategy.LogTimeSegment.ONE_HOUR
    }

    /**
     * 获取文件名称
     * @param logTime 日志打印时间
     * @param section 时间戳对应当前的开始小时和结束小时，[startHour，endHour],eg：[13,14]
     * @return 该日志对应的文件名 ，输出文件名格式：log_xxxx_xx_xx_startHour_endHour_timestamp.log
     *   比如按照日志片段(LogTimeSegment)是一个小时计算， 2023年11月20日，11：20分输出的日志对应的日志名称：
     *   log_2023_11_20_11_12_12345677.log
     */
    open fun getFileName(logTime: Long, section: Point): String {
        val logTimeStr = logFileNameDateFormat.format(logTime)
        var start = "${section.x}"
        if(start.length==1)start="0${start}"
        var end = "${section.y}"
        if(end.length==1)end="0${end}"
        return "${LogPrefix}${logTimeStr}_${start}_${end}_${logTime}${LogSuffix}"
    }

    /**
     * 获取文件头部信息，创建新文件的时候写入
     * @return
     */
    open fun getLogHeardInfo():String{
        val builder = StringBuilder()
        builder.append(LOG_HEARD_INFO)
        builder.append("总存储:${fileLogDiskStrategy.getTotalStore()}")
        builder.append("空闲存储:${fileLogDiskStrategy.getFreeStore(getLogDir())}")
        builder.append("\n\n")
        return builder.toString()
    }


    private class FilePath(
        val startTime: Long,
        val endTime: Long,
        val logFileMaxSize: Long,
        val filePath: String
    ) {
        private val MaxResetCount = 100
        private var curResetCount = 0
        private var currentSize = -1L

        private val logFile by lazy { File(filePath) }

        fun isMatch(currentTime: Long): Boolean {
            if (curResetCount > MaxResetCount || currentSize < 0) {  //检查100次，查一次文件大小
                curResetCount = 0
                currentSize = logFile.length()
            }
            curResetCount++
            //当前文件大小 < 最大限制大小， 表示匹配
            return (currentSize < logFileMaxSize && currentTime in startTime until endTime)
        }
    }


}