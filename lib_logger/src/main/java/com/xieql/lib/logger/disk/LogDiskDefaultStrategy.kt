package com.xieql.lib.logger.disk

import android.graphics.Point
import android.os.StatFs
import com.xieql.lib.logger.LogLevel
import com.xieql.lib.logger.core.appCtx
import com.xieql.lib.logger.util.debugLog
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

open class LogDiskDefaultStrategy : BaseLogDiskStrategy() {

    //日志文件名时间格式
    private val logFileNameDateFormat = SimpleDateFormat("yyyy_MM_dd")
    //默认存储地址
    private val defaultLogDir by lazy {
        val path = appCtx.getExternalFilesDir("")?.absolutePath+File.separator+"log"
        val file = File(path)
        if(!file.exists()|| !file.isDirectory){
            file.mkdirs()
        }
        debugLog("日志存储路径:${file.absolutePath}")
        return@lazy file.absolutePath
    }
    //至少遗留数据
    private val defaultAtLeastResidual by lazy {
        val total = getTotalStore()
        if(total>2*1024*1024){
            debugLog("总存储 >2GB，至少遗留存储空间:${500*1024} KB")
            return@lazy 500
        }else{
            val atLeast = total/4
            debugLog("总存储过小，至少遗留存储空间:${atLeast} KB")
            return@lazy total/4
        }
    }
    //当前文件路径
    @Volatile
    private var currentFilepath:FilePath? = null

    override fun getLogPrintPath(logLevel: LogLevel, logBody: String?, bodySize: Long): String? {
        val filePath= currentFilepath
        val currentTime = System.currentTimeMillis()
        if(filePath != null && filePath.isMatch(currentTime)){
            return filePath.filePath
        }else{
            val section  = getLogSection(currentTime,getSegment())
            val fileName = getFileName(currentTime,section.first)
            val path = getLogDir()+File.separator+fileName
            val filePath = FilePath(section.second.first,section.second.second,path)

            //todo 进行存储管理，并创建文件

            //todo 添加文件头部内容

            currentFilepath = filePath
            return filePath.filePath
        }
    }


    //获取日志文件夹路径
    open fun getLogDir():String{
        return defaultLogDir
    }
    /**
     * 至少剩余该数量的磁盘空间留给程序运行，否则可能会塞满磁盘，导致设备异常
     * 单位 KB
     */
    open fun atLeastResidual():Long{
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
        val size = blockSize * blockCount / 1024
        debugLog("block大小:$blockSize,block数目: $blockCount , 总大小: ${size}KB")
        return size
    }
    /**
     * 获取空余存储空间
     *  单位KB
     */
    open fun getFreeStore():Long{
        val sf = StatFs(getLogDir())
        val blockSize = sf.availableBlocksLong
        val blockCount = sf.blockCountLong
        val size = blockSize * blockCount / 1024
        debugLog("block大小:$blockSize,block数目: $blockCount , 总大小: ${size}KB")
        return size
    }

    /**
     * 获取日志所处时间区间
     * @return ([startHour,endHour],[startTime,endTime])
     *  比如：([11,12],[11122,112233])
     */
    open fun getLogSection(currentTime:Long,segment: LogSegment):Pair<Point,Pair<Long,Long>>{
        val curCalendar = Calendar.getInstance()
        curCalendar.timeInMillis = currentTime
        val curHour = curCalendar.get(Calendar.HOUR_OF_DAY) //获取当前时间的日期
        val section = Point(0,segment.value)
        while ( section.y<=24 ){  //在 section 小于24小时情况下，可以循环查找区间
            if(curHour in section.x until section.y){
                break
            }else{
                section.x = section.x+segment.value
                section.y = section.y+segment.value
            }
        }
        curCalendar.set(Calendar.HOUR_OF_DAY,section.x)
        curCalendar.set(Calendar.MINUTE,0)
        curCalendar.set(Calendar.SECOND,0)
        curCalendar.set(Calendar.MILLISECOND,0)
        val statTime = curCalendar.time.time

        curCalendar.set(Calendar.HOUR_OF_DAY,section.y)
        curCalendar.set(Calendar.MINUTE,59)
        curCalendar.set(Calendar.SECOND,59)
        curCalendar.set(Calendar.MILLISECOND,999)
        val endTime = curCalendar.time.time

        val timeSection = Pair(statTime,endTime)

        return Pair(section,timeSection)
    }

    /**
     * 获取文件名称
     * @param logTime 日志打印时间
     * @param section 时间戳对应当前的开始小时和结束小时，[startHour，endHour],eg：[13,14]
     * @return 该日志对应的文件名 ，输出文件名格式：log_xxxx_xx_xx_startHour_endHour.log
     *   比如按照日志片段(LogSegment)是一个小时计算， 2023年11月20日，11：20分输出的日志对应的日志名称：
     *   log_2023_11_20_11_12.log
     *
     */
    open fun getFileName(logTime: Long, section: Point): String {
        val logTimeStr = logFileNameDateFormat.format(logTime)
        return "log_${logTimeStr}_${section.x}_${section.y}.log"
    }

    /**
     * 获取片段
     * @return
     */
    open fun getSegment():LogSegment{
        return LogSegment.ONE_HOUR
    }


    //日志片段
    enum class LogSegment(val value:Int){
        ONE_HOUR(1),
        TWO_HOURS(2),
        THREE_HOURS(3),
        FOUR_HOURS(4),
        SIX_HOURS(6),
        TWELVE_HOURS(12),
        TWENTY_FOUR_HOURS(24);
    }
    private class FilePath(val startTime:Long,val endTime:Long,val filePath:String){
        //是否匹配
        fun isMatch(currentTime:Long):Boolean{
            val isMatch = currentTime in startTime until endTime
            return isMatch
        }
    }


}


