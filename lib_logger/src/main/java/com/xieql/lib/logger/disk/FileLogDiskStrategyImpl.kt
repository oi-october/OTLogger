package com.xieql.lib.logger.disk

import android.os.StatFs
import com.xieql.lib.logger.LogLevel
import com.xieql.lib.logger.core.appCtx
import com.xieql.lib.logger.util.debugLog
import java.io.File
import java.text.SimpleDateFormat

/**
 * 日志文件管理策略，按存储管理日志文件
 * - 默认每个日志文件10M
 * - 默认日志文件夹最大可容纳 200M日志，操过空间会按照时间顺序删除旧的日志
 * - 默认文件名 log_年_月_日_时_分_秒.log
 *   eg: log_2023_02_12_16_28_56.log
 * - 什么时候创建新的日志文件？
 *   1. 每个日志写满了会创建一个新的日志文件
 *   2. 内存中的日志对象[currentLogFile]为null，也会创建一个新的文件，而不是复用上一个未写满的日志文件
 */
class FileLogDiskStrategyImpl :BaseFileLogDiskStrategy(){

    private companion object{
        const val LogPrefix = "log_"
        const val LogSuffix = ".log"
    }

    //日志文件名时间格式
    private val logFileNameDateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")

    private val currentLogFile:File? = null

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
        return ""
    }


    //获取日志文件夹路径
    override fun getLogDir():String{
        return defaultLogDir
    }

    override fun minFreeStore():Long{
        return defaultMinFreeStore
    }

    override fun getLogFileMaxSize(): Long {
        TODO("Not yet implemented")
    }

    override fun getMinFreeStore(): Long {
        return defaultMinFreeStore
    }

    override fun getMaxLogDirStore(): Long {
        TODO("Not yet implemented")
    }


    /**
     * 获取文件名称
     * @param logTime 日志打印时间
     * @param section 时间戳对应当前的开始小时和结束小时，[startHour，endHour],eg：[13,14]
     * @return 该日志对应的文件名 ，输出文件名格式：log_xxxx_xx_xx_currentHour_timestamp.log
     *   2023年11月20日，11：20分输出的日志对应的日志名称：
     *   log_2023_11_20_11_1234556.log
     *
     */
    protected open fun getFileName(logTime: Long, hourTime:Int): String {
        val logTimeStr = logFileNameDateFormat.format(logTime)
        return "${LogPrefix}${logTimeStr}_${hourTime.}_${logTime}${LogSuffix}"
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
    abstract fun getLogFileMaxSize():Long

    /**
     * 设置最小空闲存储空间 单位：MB
     * - 创建新文件时候， 必须满足 [系统空闲存储 > 最小空闲存储空间] 的时候，才能创建新的日志文件
     * -
     * @return
     */
    abstract fun getMinFreeStore():Long

    /**
     * 设置日志文件夹最大容量 :单位MB
     * - 只有日志文件夹中，[所有的日志文件大小之和 < 日志文件夹最大容量] 的时候，才能创建新的日志文件.
     * - [所有的日志文件大小之和 >= 日志文件夹最大容量]的时候，会触发回删操作
     * @return
     */
    abstract fun getMaxLogDirStore():Long

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
    open fun getFreeStore():Long{
        val sf = StatFs(getLogDir())
        val blockSize = sf.availableBlocksLong
        val blockCount = sf.blockCountLong
        val size = blockSize * blockCount
        debugLog("block大小:$blockSize,block数目: $blockCount , 总大小: ${size/1024}KB")
        return size
    }


}


