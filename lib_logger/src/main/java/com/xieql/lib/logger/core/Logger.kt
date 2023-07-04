package com.xieql.lib.logger.core

import com.xieql.lib.logger.print.BaseLogTxtPrinter
import com.xieql.lib.logger.print.BaseLogcatPrinter
import com.xieql.lib.logger.print.LogcatDefaultPrinter
import com.xieql.lib.logger.print.LogTxtDefaultPrinter


/**
 * Logger 实体
 */
open class Logger {

    internal companion object{
         var logger :Logger = Builder().build()
    }

    private lateinit var builder:Builder
    private constructor(build: Builder){
        builder = build
    }

    /**
     * 打印日志
     * @param level 日志等级
     * @param tag
     * @param msg 日志消息
     * @param thr 异常消息
     */
    open fun println(level:LogLevel,tag:String?,msg:String?,thr:Throwable?){
        builder.logcatPrinter.print(level, tag, msg, thr)
        builder.logTxtPrinter.
    }

    class Builder{
        //控制台打印器
        internal var logcatPrinter:BaseLogcatPrinter = LogcatDefaultPrinter()
        //日志文件打印机
        internal var logTxtPrinter:BaseLogTxtPrinter = LogTxtDefaultPrinter()

        fun setLogcatPrinter(logcatPrinter:LogcatDefaultPrinter):Builder{
            this.logcatPrinter = logcatPrinter
            return this
        }

        fun setLogTxtPrinter(logTxtPrinter:LogTxtDefaultPrinter):Builder{
            this.logTxtPrinter = logTxtPrinter
            return this
        }

        fun build():Logger{
            return Logger(this)
        }

    }

}