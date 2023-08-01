package com.october.lib.logger.disk

import com.october.lib.logger.LogLevel
import com.october.lib.logger.util.debugLog
import java.io.File

/**
 * 日志存储管理
 */
abstract class BaseLogDiskStrategy {

    companion object{
        const val LogPrefix = "otLog_"
        const val LogSuffix = ".log"
    }

    @Volatile
    private var isLogDirUsable = false

    internal fun internalGetLogPrintPath(logLevel: LogLevel, logBody: String?, bodySize: Long):String?{
        if(!isLogDirUsable){  // check log dir until it is available
            val logDirFile = File(getLogDir())
            if(!logDirFile.exists() || !logDirFile.isDirectory){
                val isCreateLogDirSuccess = logDirFile.mkdirs() //create log dir
                if(!isCreateLogDirSuccess){
                    debugLog("can not create log dir :${getLogDir()}")
                    return ""
                }
            }
            if(!logDirFile.canRead() || !logDirFile.canWrite()){
                debugLog("can not read or write to log dir")
                return ""
            }
            isLogDirUsable = true
        }
        return getLogPrintPath(logLevel, logBody, bodySize)
    }

    /**
     * 获取日志输出文件路径
     * - 每次写入日志时候都会进行获取
     * @param logLevel 本次需要写入的日志级别
     * @param logBody  本次需要写入的数据
     * @param bodySize 本次需要写入的数据大小
     * @return 返回路径写入路径
     */
    abstract fun getLogPrintPath(logLevel: LogLevel, logBody: String?, bodySize: Long): String?

    /**
     * 获取当前正在被写入的日志文件路径
     * @return
     */
    abstract fun getCurrentLogFilePath():String?

    /**
     * 获取日志所在文件夹
     * @return
     */
    abstract fun getLogDir(): String





}