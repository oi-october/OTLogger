package com.october.lib.logger.disk

import android.graphics.Point
import android.provider.ContactsContract.Directory
import com.october.lib.logger.LogLevel
import com.october.lib.logger.common.LOG_HEARD_INFO
import com.october.lib.logger.util.appCtx
import com.october.lib.logger.util.debugLog
import com.october.lib.logger.util.errorLog
import java.io.File
import java.io.FilenameFilter
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日志文件管理策略，按时间管理日志文件
 * - 默认按照小时创建日志文件
 * - 默认每个日志文件保存七天
 * - 默认文件名 log_年_月_日_时间段.log
 *     eg：otLog_2023_02_12_15_16.log ，这里的 15_16 表示该文件储存 15点到 16点的日志
 * @param logDirectory 日志文件夹
 * @param logKeepOfDay 日志保存天数
 * @param logSegment 创建日志文件间隔，默认每个小时创建一份新的日志文件
 */
open class TimeLogDiskStrategyImpl(
    val logDirectory: String = defaultLogDir,
    val logKeepOfDay: Int = 7,
    val logSegment:LogTimeSegment= LogTimeSegment.ONE_HOUR
) : BaseLogDiskStrategy() {

    //日志文件名时间格式
    private val logFileNameDateFormat = SimpleDateFormat("yyyy_MM_dd")
    //当前文件路径
    @Volatile
    private var currentFilePathCache: FilePathCache? = null

    override fun createLogFile(printTime: Long, logLevel: LogLevel, logBody: String?): String? {
        val section = getLogSection(printTime, getSegment())
        val fileName = getFileName(printTime, section.first)
        val path = getLogDir() + File.separator + fileName
        val filePath = FilePathCache(section.second.first, section.second.second, path)
        currentFilePathCache = filePath
        return filePath.filePath
    }

    override fun isLogFilePathAvailable(
        logFilepath: String?,
        printTime: Long,
        logLevel: LogLevel,
        logBody: String?
    ): Boolean {
        return currentFilePathCache?.isMatch(printTime, logFilepath) == true
    }

    override fun isAllowCreateLogFile(printTime: Long): Boolean {
        val logHoldDuration = getLogHoldDurationOfDay() * 24 * 60 * 60 * 1000L
        val expirationTime = printTime - logHoldDuration  //小于该时间都是过期日志
        val expirationFileSection = getLogSection(expirationTime, getSegment())
        val expirationFileName = getFileName(expirationTime, expirationFileSection.first) //在该日志时间之前的日志都是过去的
        debugLog("the log before [${expirationFileName}] is invalid")
        val logDirFile = File(getLogDir())
        // log dir exist？ read and write permission?
        if (!logDirFile.exists() || !logDirFile.isDirectory || !logDirFile.canRead() || !logDirFile.canWrite()) {
            errorLog(" log dir exist？ read and write permission?")
            return false
        }
        val expirationFiles = logDirFile.listFiles(FilenameFilter { _, name ->
            if (expirationFileName > name) {
                debugLog("invalid log= ${name}")
                return@FilenameFilter true
            } else {
                debugLog("valid log= ${name}")
                return@FilenameFilter false
            }
        })
        if (expirationFiles.isNullOrEmpty()) return true
        expirationFiles.forEach {
            it.delete() //删除过期日志文件
        }
        return true
    }

    override fun getLogDir(): String {
        return logDirectory
    }

    override fun logHeadInfo(): String? {
        return LOG_HEARD_INFO
    }

    //获取日志保存天数
    open fun getLogHoldDurationOfDay(): Int {
        return logKeepOfDay
    }

    //获取日志片段
    open fun getSegment(): LogTimeSegment {
        return logSegment
    }

    /**
     *
     * 获取[time]所处小时的区间
     * @param time
     * @param segment 时间段分割
     * @return 返回时间段 ([startHour,endHour],[startTime,endTime])
     *  比如：([12,13],[1597377600000,1597381200000])
     */
    open fun getLogSection(
        time: Long,
        segment: LogTimeSegment
    ): Pair<Pair<Int, Int>, Pair<Long, Long>> {
        val curCalendar = Calendar.getInstance()
        curCalendar.timeInMillis = time
        val curHour = curCalendar.get(Calendar.HOUR_OF_DAY) //获取当前时间的日期
        val section = Point(0, segment.value)
        while (section.y <= 24) {  //在 section 小于24小时情况下，可以循环查找区间
            if (curHour in section.x until section.y) {
                break
            } else {
                section.x = section.x + segment.value
                section.y = section.y + segment.value
            }
        }
        curCalendar.set(Calendar.HOUR_OF_DAY, section.x)
        curCalendar.set(Calendar.MINUTE, 0)
        curCalendar.set(Calendar.SECOND, 0)
        curCalendar.set(Calendar.MILLISECOND, 0)
        val statTime = curCalendar.time.time
        val endTime = statTime + segment.value * 60 * 60 * 1000
        return Pair(Pair(section.x, section.y), Pair(statTime, endTime))
    }


    /**
     * 获取文件名称
     * @param logTime 日志打印时间
     * @param section 时间戳对应当前的开始小时和结束小时，[startHour，endHour],eg：[13,14]
     * @return 该日志对应的文件名 ，输出文件名格式：log_xxxx_xx_xx_startHour_endHour.log
     *   比如按照日志片段(LogTimeSegment)是一个小时计算， 2023年11月20日，11：20分输出的日志对应的日志名称：
     *   otLog_2023_11_20_11_12.log
     */
    private fun getFileName(logTime: Long, section: Pair<Int, Int>): String {
        val logTimeStr = logFileNameDateFormat.format(logTime)
        var start = "${section.first}"
        if (start.length == 1) start = "0${start}"
        var end = "${section.second}"
        if (end.length == 1) end = "0${end}"
        return "${LogPrefix}${logTimeStr}_${start}_${end}${LogSuffix}"
    }

    private class FilePathCache(val startTime: Long, val endTime: Long, val filePath: String) {
        //是否匹配
        fun isMatch(currentTime: Long, logFilePath: String?): Boolean {
            val isMatch = currentTime in startTime until endTime && logFilePath == filePath
            return isMatch
        }
    }


}


