package com.october.lib.logger.print

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import com.october.lib.logger.LogLevel
import com.october.lib.logger.disk.BaseLogDiskStrategy
import com.october.lib.logger.format.BaseFormatStrategy
import com.october.lib.logger.util.debugLog
import java.io.File

abstract class BaseLogTxtPrinter:IPrinter{

    @Volatile
    private var handler: WriteHandler? = null

    override fun print(logLevel: LogLevel, tag: String?, msg: String?, thr: Throwable?, param:Any?) {
        if (!isPrint()) {
            //debugLog("不需要打印到文件")
            return
        }
        if (logLevel.logLevel < getPrintMinLevel().logLevel) {
            //debugLog("日志级别比最低级别小，不需要打印")
            return
        }
        val logBody = getLogcatFormatStrategy().format(logLevel, tag, msg, thr,param) //组装数据
        getWriterHandler().sendMessage(getWriterHandler().obtainMessage(999, arrayOf<Any?>(logLevel,logBody)))
    }

    //释放打印日志
    abstract fun isPrint(): Boolean

    //最小打印级别，比他低级的日志不会打印
    abstract fun getPrintMinLevel(): LogLevel

    //日志输出格式
    abstract fun getLogcatFormatStrategy(): BaseFormatStrategy

    //获取日志文件夹管理策略
    abstract fun getLogDiskStrategy(): BaseLogDiskStrategy

    //获取写入日志的 Handler
    open fun getWriterHandler(): WriteHandler {
        if (handler == null) {
            synchronized(this) {
                if (handler == null) {
                    val handlerThread = HandlerThread("Logger")
                    handlerThread.start()
                    handler = WriteHandler(handlerThread.looper,getLogDiskStrategy()) //获取日志文件夹管理策略
                }
            }
        }
        return handler!!
    }


}

open class WriteHandler(
    looper: Looper,
    val logDiskStrategy: BaseLogDiskStrategy
) : Handler(looper) {

    override fun handleMessage(msg: Message) {
        val obj = msg.obj as Array<Any>
        val logLevel = obj[0] as LogLevel
        val logBody = if(obj[1] != null)(obj[1] as String) else null
        log(logLevel,logBody?:"")
    }

    open fun log(logLevel: LogLevel, logBody:String) {
        val logFilePath =
            logDiskStrategy.getLogPrintPath(logLevel, logBody, logBody.length.toLong())
        val logFile = File(logFilePath)
        val parentFile = logFile.parentFile
        try {
            //创建父文件夹
            if (!parentFile.exists() || !parentFile.isDirectory) {
                val isSuccess = parentFile.mkdirs()
                if (!isSuccess) {
                    debugLog("无法创建日志文件夹:" + parentFile.absolutePath)
                    return
                }
            }
            //创建文件
            if (!logFile.exists() || !logFile.isFile) {
                val isCreateSuccess = logFile.createNewFile()
                if (!isCreateSuccess ) {
                    debugLog("无法创建日志文件:" + logFilePath)
                    return
                }
            }

            logFile.appendText(logBody)


        } catch (e: Exception) {
            debugLog("日志写入异常，日志文件名称=" + logFilePath)
            debugLog(Log.getStackTraceString(e))
        }


    }


}



