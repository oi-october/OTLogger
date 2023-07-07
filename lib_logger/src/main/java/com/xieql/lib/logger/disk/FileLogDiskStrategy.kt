package com.xieql.lib.logger.disk

import android.graphics.Point
import android.os.StatFs
import com.xieql.lib.logger.LogLevel
import com.xieql.lib.logger.core.appCtx
import com.xieql.lib.logger.util.debugLog
import java.io.File
import java.text.SimpleDateFormat

/**
 * 日志文件管理策略，按存储来管理日志文件
 */
class FileLogDiskStrategy :BaseLogDiskStrategy(){

    private companion object{
        const val LogPrefix = "log_"
        const val LogSuffix = ".log"
    }

    //日志文件名时间格式
    private val logFileNameDateFormat = SimpleDateFormat("yyyy_MM_dd")

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

    //至少遗留数据 , 单位B
    private val defaultAtLeastResidual by lazy {
        val total = getTotalStore()
        if(total>2*1024*1024*1024){
            debugLog("总存储 >2GB，至少遗留存储空间:${500} MB")
            return@lazy 500 * 1024 * 1024
        }else{
            val atLeast = total/4
            debugLog("总存储 < 2GB，至少遗留存储空间:${atLeast/1024/1024} MB")
            return@lazy total/4
        }
    }


    override fun getLogPrintPath(logLevel: LogLevel, logBody: String?, bodySize: Long): String? {
        return ""
    }


    //获取日志文件夹路径
    open fun getLogDir():String{
        return defaultLogDir
    }

    /**
     * 至少剩余空闲空间，单位 B
     * - 如果该值 > 0   ：系统空闲存储空间只有大于该值时候[getFreeStore]>[minFreeStore]，才能进行创建新的日志文件
     * - 如果该值 <= 0  ：创建新的日志文件前不会判断空闲空间大小
     * @return 最小应剩余空闲空间
     */
    open fun minFreeStore():Long{
        return defaultAtLeastResidual
    }


    /**
     * 获取全部存储空间
     *  单位KB
     */
    open fun getTotalStore():Long{
        val sf = StatFs(getLogDir())
        val blockSize = sf.blockSizeLong
        val blockCount = sf.blockCountLong
        val size = blockSize * blockCount
        debugLog("block大小:$blockSize,block数目: $blockCount , 总大小: ${size}KB")
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
        debugLog("block大小:$blockSize,block数目: $blockCount , 总大小: ${size}KB")
        return size
    }

    /**
     * 获取日志所处时间区间
     * @return ([startHour,endHour],[startTime,endTime])
     *  比如：([11,12],[11122,112233])
     */
    open fun getLogSection(currentTime:Long):Pair<Point,Pair<Long,Long>>?{
        return null
    }

    /**
     * 获取文件名称
     * @param logTime 日志打印时间
     * @param section 时间戳对应当前的开始小时和结束小时，[startHour，endHour],eg：[13,14]
     * @return 该日志对应的文件名 ，输出文件名格式：log_xxxx_xx_xx_startHour_endHour_timestamp.log
     *   比如按照日志片段(LogSegment)是一个小时计算， 2023年11月20日，11：20分输出的日志对应的日志名称：
     *   log_2023_11_20_11_12_1234556.log
     *
     */
    open fun getFileName(logTime: Long, section: Point): String {
        val logTimeStr = logFileNameDateFormat.format(logTime)
        return "${LogPrefix}${logTimeStr}_${section.x}_${section.y}_${logTime}${LogSuffix}"
    }

}

