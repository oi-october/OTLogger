package com.xieql.lib.logger.disk

import android.graphics.Point
import android.util.Log
import com.xieql.lib.logger.LogLevel
import com.xieql.lib.logger.core.appCtx
import com.xieql.lib.logger.util.create
import com.xieql.lib.logger.util.debugLog
import java.io.File
import java.io.FilenameFilter
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日志文件管理策略，按时间管理日志文件
 * - 默认按照小时创建日志文件
 * - 默认每个日志文件保存七天
 * -
 *
 */
open class TimeLogDiskStrategy : BaseTimeLogDiskStrategy() {

    private companion object{
        const val LogPrefix = "log_"
        const val LogSuffix = ".log"
    }
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

            //进行存储管理，删除超时文件
            clearExpirationLogFile(currentTime,getLogHoldDurationOfDay()* 24 * 60 * 60* 1000L)


            val file = File(filePath.filePath)

            if(!file.exists() || !file.isFile){  //创建新文件 并 添加文件头部内容
                file.createNewFile()
            }

            currentFilepath = filePath
            return filePath.filePath
        }
    }

    //获取日志保存天数
    override fun getLogHoldDurationOfDay():Int{
        return 7
    }
    //获取日志文件夹路径
    override fun getLogDir():String{
        return defaultLogDir
    }
    //获取日志片段
    override fun getSegment():LogTimeSegment{
        return LogTimeSegment.ONE_HOUR
    }

    /**
     * 清理过期日志
     * @param currentTime 当前时间
     * @param logHoldDuration 日志保存时长, 单位是 ms
     */
    protected open fun clearExpirationLogFile(currentTime: Long,logHoldDuration:Long){
        val expirationTime = currentTime - logHoldDuration  //小于该时间都是过期日志
        val expirationFileSection = getLogSection(expirationTime,getSegment())
        val expirationFileName = getFileName(currentTime,expirationFileSection.first) //在该日志时间之前的日志都是过去的
        val logDirFile = File(getLogDir())
        if(!logDirFile.exists() || !logDirFile.isDirectory) return
        val expirationFiles =  logDirFile.listFiles(FilenameFilter { _, name ->
            if(expirationFileName > name){
                debugLog("过期日志：${name}")
                return@FilenameFilter true
            }else{
                debugLog("未过期日志：${name}")
                return@FilenameFilter false
            }
        })
        if(expirationFiles.isNullOrEmpty()) return
        expirationFiles.forEach {
            it.delete() //删除过期日志文件
        }
    }

    /**
     * 获取日志所处时间区间
     * @return ([startHour,endHour],[startTime,endTime])
     *  比如：([11,12],[11122,112233])
     */
    protected open fun getLogSection(currentTime:Long,segment: LogTimeSegment):Pair<Point,Pair<Long,Long>>{
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
    protected open fun getFileName(logTime: Long, section: Point): String {
        val logTimeStr = logFileNameDateFormat.format(logTime)
        return "${LogPrefix}${logTimeStr}_${section.x}_${section.y}${LogSuffix}"
    }


    private class FilePath(val startTime:Long,val endTime:Long,val filePath:String){
        //是否匹配
        fun isMatch(currentTime:Long):Boolean{
            val isMatch = currentTime in startTime until endTime
            return isMatch
        }
    }


}

/**
 * 按照时间管理日志策略
 */
public abstract class BaseTimeLogDiskStrategy:BaseLogDiskStrategy(){
    /**
     * 获取日志文件夹
     */
    abstract fun getLogDir():String

    /**
     * 日志保持天数，单位：天
     * @return 超过时间
     */
    abstract fun getLogHoldDurationOfDay():Int

    /**
     * 获取片段
     * @return
     */
    abstract fun getSegment():LogTimeSegment

    //日志时间片段
    enum class LogTimeSegment(val value:Int){
        ONE_HOUR(1),
        TWO_HOURS(2),
        THREE_HOURS(3),
        FOUR_HOURS(4),
        SIX_HOURS(6),
        TWELVE_HOURS(12),
        TWENTY_FOUR_HOURS(24);
    }

}
