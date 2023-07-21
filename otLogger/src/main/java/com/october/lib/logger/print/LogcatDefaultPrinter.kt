package com.october.lib.logger.print

import com.october.lib.logger.LogLevel
import com.october.lib.logger.format.LogcatDefaultFormatStrategy
import com.october.lib.logger.format.BaseFormatStrategy

/**
 * 默认的Locat 日志打印机
 */
open class LogcatDefaultPrinter:BaseLogcatPrinter(){

    @Volatile
    protected var formatStrategy: BaseFormatStrategy? = null

    override fun isPrint(): Boolean {
       return true
    }

    override fun getPrintMinLevel(): LogLevel {
        return LogLevel.V
    }

    override fun getLogcatFormatStrategy(): BaseFormatStrategy {
        if(formatStrategy == null){
            synchronized(this){
                if(formatStrategy == null){
                    formatStrategy = LogcatDefaultFormatStrategy()
                }
            }
        }
        return formatStrategy!!
    }

}