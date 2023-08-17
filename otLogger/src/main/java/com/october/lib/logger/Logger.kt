package com.october.lib.logger

import com.october.lib.logger.crash.BaseCrashStrategy
import com.october.lib.logger.print.BaseLogTxtPrinter
import com.october.lib.logger.print.BaseLogcatPrinter
import com.october.lib.logger.print.LogcatDefaultPrinter
import java.util.BitSet


/**
 * Logger 实体
 */
open class Logger {

    internal companion object{
         var logger : Logger = Builder().build()
    }

    private lateinit var builder: Builder
    private constructor(build: Builder){
        builder = build
        //初始化异常捕获机制
        builder.crashStrategy?.init()
    }

    internal fun isDebug():Boolean{
       return builder.isDebug
    }

    fun getLogcatPrinter():BaseLogcatPrinter?{
        return builder.logcatPrinter
    }

    fun getLogTxtPrinter():BaseLogTxtPrinter?{
        return builder.logTxtPrinter
    }
    fun getCrashStrategy():BaseCrashStrategy?{
        return builder.crashStrategy
    }

    /**
     * 打印日志
     * @param level 日志等级
     * @param tag
     * @param msg 日志消息
     * @param thr 异常消息
     * @param param 其他参数
     */
    open fun println(level: LogLevel, tag:String?, msg:String?, thr:Throwable?,param:Any?){
        if(this::builder.isInitialized){
            builder.logcatPrinter?.print(level, tag, msg, thr,param)
            builder.logTxtPrinter?.print(level,tag,  msg, thr,param)
        }
    }

    class Builder{
        //控制台打印器
        internal var logcatPrinter:BaseLogcatPrinter? = LogcatDefaultPrinter()
        //日志文件打印机
        internal var logTxtPrinter:BaseLogTxtPrinter? = null

        internal var crashStrategy:BaseCrashStrategy? = null

        internal var isDebug = false

        fun setLogcatPrinter(logcatPrinter:BaseLogcatPrinter?): Builder {
            this.logcatPrinter = logcatPrinter
            return this
        }

        fun setLogTxtPrinter(logTxtPrinter:BaseLogTxtPrinter?): Builder {
            this.logTxtPrinter = logTxtPrinter
            return this
        }

        fun setCrashStrategy(crashStrategy: BaseCrashStrategy):Builder{
            this.crashStrategy = crashStrategy
            return this
        }

        fun setIsDebug(isDebug:Boolean):Builder{
            this.isDebug = isDebug
            return this
        }

        fun build(): Logger {
            return Logger(this)
        }

    }

}