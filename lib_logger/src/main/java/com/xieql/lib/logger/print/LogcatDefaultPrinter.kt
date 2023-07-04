package com.xieql.lib.logger.print

import com.xieql.lib.logger.core.LogLevel
import com.xieql.lib.logger.format.LogcatDefaultFormatStrategy
import com.xieql.lib.logger.format.base.BaseFormatStrategy

/**
 * 默认的Locat 日志打印机
 */
open class LogcatDefaultPrinter:BaseLogcatPrinter(){

    override fun isPrint(): Boolean {
       return true
    }

    override fun getPrintMinLevel(): LogLevel {
        return LogLevel.V
    }

    override fun getLogcatFormatStrategy(): BaseFormatStrategy {
        return LogcatDefaultFormatStrategy()
    }

}