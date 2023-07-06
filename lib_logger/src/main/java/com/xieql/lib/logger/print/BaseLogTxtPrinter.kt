package com.xieql.lib.logger.print

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import com.xieql.lib.logger.LogLevel
import com.xieql.lib.logger.disk.BaseLogDiskStrategy
import com.xieql.lib.logger.format.base.BaseFormatStrategy
import com.xieql.lib.logger.util.debugLog
import java.io.File

abstract class BaseLogTxtPrinter {

    @Volatile
    private var handler:WriteHandler? = null

    open fun print(logLevel: LogLevel, tag:String?, msg:String?, thr:Throwable?){
        if(!isPrint()){
            //debugLog("不需要打印到文件")
            return
        }
        if(logLevel.logLevel<getPrintMinLevel().logLevel){
            //debugLog("日志级别比最低级别小，不需要打印")
            return
        }
        getWriterHandler().sendMessage(getWriterHandler().obtainMessage(999, arrayOf(logLevel,tag,msg,thr)))
    }

    //释放打印日志
    abstract fun isPrint():Boolean
    //最小打印级别，比他低级的日志不会打印
    abstract fun getPrintMinLevel(): LogLevel
    //日志输出格式
    abstract fun getLogcatFormatStrategy(): BaseFormatStrategy
    //获取日志文件夹管理策略
    abstract fun getLogDirStrategy(): BaseLogDiskStrategy
    //获取写入日志的 Handler
    open fun getWriterHandler():WriteHandler{
        if(handler == null){
            synchronized(this){
                if(handler == null){
                    val handlerThread = HandlerThread("Logger")
                    handlerThread.start()
                    handler = WriteHandler(getLogDirStrategy(),getLogcatFormatStrategy()) //获取日志文件夹管理策略
                }
            }
        }
        return handler!!
    }

}

open class WriteHandler(val logDiskStrategy: BaseLogDiskStrategy, val formatStrategy: BaseFormatStrategy):Handler(){

    override fun handleMessage(msg: Message) {
        val obj = msg.obj as Array<Any>
        val logLevel = obj[0] as LogLevel
        val logTag = obj[1] as String
        val logMsg = obj[2] as String
        val logThr = obj[3] as Throwable
        log(logLevel,logTag,logMsg,logThr)
    }

    open fun log(logLevel: LogLevel, tag: String?, msg: String?, thr: Throwable?){
        val logBody = formatStrategy.format(logLevel, tag, msg, thr)
        val logFilePath =  logDiskStrategy.getLogPrintPath(logLevel,logBody,logBody.length.toLong())
        val logFile = File(logFilePath)
        val parentFile = logFile.parentFile
        try {
            //创建父文件夹
            if(!parentFile.exists() || !parentFile.isDirectory){
                val isSuccess = parentFile.mkdirs()
                if(isSuccess){
                    debugLog("无法创建日志文件夹:"+parentFile.absolutePath)
                    return
                }
            }
            //创建文件
            if(!logFile.exists() || !logFile.isFile){
                val isCreateSuccess =  logFile.createNewFile()
                if(!isCreateSuccess){
                    debugLog("无法创建日志文件:"+logFilePath)
                    return
                }
            }

            logFile.appendText(logBody)

        }catch (e:Exception){
            debugLog("日志写入异常，日志文件名称="+logFilePath)
            debugLog(Log.getStackTraceString(e))
        }


    }




}


