package com.xieql.lib.logger.print

import com.xieql.lib.logger.LogLevel
import com.xieql.lib.logger.format.BaseFormatStrategy

/**
 * 自定义 Loacat 打印机
 * @property printable  是否打印日志到 logcat
 * @property minLevel   最小日志输出级别
 * @property formatStrategy 日志格式策略
 */
open class LogcatCustomPrinter(val printable:Boolean, val minLevel: LogLevel, val formatStrategy: BaseFormatStrategy):BaseLogcatPrinter(){

    override fun isPrint(): Boolean {
        return printable
    }

    override fun getPrintMinLevel(): LogLevel {
        return minLevel
    }

    override fun getLogcatFormatStrategy(): BaseFormatStrategy {
        return formatStrategy
    }

}