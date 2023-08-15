package com.october.lib.logger.disk

import com.october.lib.logger.LogLevel
import com.october.lib.logger.util.appCtx
import com.october.lib.logger.util.debugLog
import com.october.lib.logger.util.errorLog
import java.io.File

/**
 * 日志存储管理
 */
abstract class BaseLogDiskStrategy {

    companion object{
        const val LogPrefix = "otLog_"
        const val LogSuffix = ".log"

        //default log dir
        internal val defaultLogDir by lazy {
            val path = appCtx.getExternalFilesDir("")?.absolutePath + File.separator + "log"
            val file = File(path)
            if (!file.exists() || !file.isDirectory) {
                file.mkdirs()
            }
            debugLog("log dir path:${file.absolutePath}")
            return@lazy file.absolutePath
        }
    }

    @Volatile
    private var isLogDirAvailable = false
    private var currentLogFilePath:String? = null

    internal fun internalGetLogPrintPath(printTime: Long,logLevel: LogLevel, logBody: String?, bodySize: Long):String?{
        if(!isLogDirAvailable){  // check log dir until it is available
            val logDirFile = File(getLogDir())
            if(!logDirFile.exists() || !logDirFile.isDirectory){
                val isCreateLogDirSuccess = logDirFile.mkdirs() //create log dir
                if(!isCreateLogDirSuccess){
                    errorLog("can not create log dir :${getLogDir()}")
                    return ""
                }
            }
            if(!logDirFile.canRead() || !logDirFile.canWrite()){
                errorLog("can not read or write to log dir")
                return ""
            }
            isLogDirAvailable = true
        }
        return getLogPrintPath(printTime,logLevel, logBody, bodySize)
    }



    /**
     * 获取日志输出文件路径
     * - 每次写入日志时候都会进行获取
     * @param printTime 打印时间
     * @param logLevel 本次需要写入的日志级别
     * @param logBody  本次需要写入的数据
     * @param bodySize 本次需要写入的数据大小
     * @return 返回路径写入路径
     */
    open fun getLogPrintPath(printTime: Long,logLevel: LogLevel, logBody: String?, bodySize: Long): String?{
        val path = getCurrentLogFilePath()
        if(isLogFilePathAvailable(path,printTime,logLevel, logBody)){
            return path
        }else{
            if(!isAllowCreateLogFile(printTime)) {
                errorLog("is not allow create log file")
                return ""
            }
            var path:String? = createLogFile(printTime,logLevel,logBody)
            appendLogHead2NewFile(logHeadInfo(),path) //write log head info into log file
            setCurrentFilePath(path)
            return getCurrentLogFilePath()
        }
    }

    private fun appendLogHead2NewFile(logHead:String?, filePath:String?){
        if(!filePath.isNullOrEmpty() && !logHead.isNullOrEmpty()){
            val file = File(filePath)
            if(!file.exists() || !file.isFile){
                if(file.createNewFile()){
                    file.appendText(logHead)  //写入文件头
                }
            }
        }
    }

    /**
     * 获取日志所在文件夹
     * @return
     */
    abstract fun getLogDir(): String

    /**
     * 创建新的日志文件，用于写入日志内容[logBody]
     * @return 新的日志文件路径
     */
    abstract fun createLogFile(printTime: Long,logLevel: LogLevel, logBody: String?):String?

    /**
     * 判断日志[logBody]是否可以输出到[logFilepath]文件中
     * @param logFilepath 带判断的日志文件
     * @param printTime 打印时间
     * @param logLevel 本次日志的日志等级
     * @param logBody 日志内容
     */
    abstract fun isLogFilePathAvailable(logFilepath:String?,printTime:Long,logLevel: LogLevel, logBody: String?):Boolean

    /**
     * 是否允许创建新的日志文件
     *  - 一般在这里进行旧文件清理工作，注意在这里不要执行过多阻塞性的工作
     * @return 是否允许创建
     */
    abstract fun isAllowCreateLogFile(printTime: Long):Boolean

    /**
     * 日志头部
     * 每次创建日志文件，会在每个日志文件头部添加该数据
     */
    abstract fun logHeadInfo():String?

    /**
     * 获取当前正在被写入的日志文件路径
     * @return 正在被写入的日志路径
     */
    fun getCurrentLogFilePath():String?{
        return currentLogFilePath
    }

    private fun setCurrentFilePath(path:String?){
        currentLogFilePath = path
    }


}

//日志时间片段
enum class LogTimeSegment(val value: Int) {
    ONE_HOUR(1),
    TWO_HOURS(2),
    THREE_HOURS(3),
    FOUR_HOURS(4),
    SIX_HOURS(6),
    TWELVE_HOURS(12),
    TWENTY_FOUR_HOURS(24);
}

