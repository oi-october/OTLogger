package com.october.lib.logger.print

import com.october.lib.logger.LogLevel
import com.october.lib.logger.format.BaseFormatStrategy
import com.october.lib.logger.format.LogcatDefaultFormatStrategy

/**
 * 默认Loacat 打印机
 * @property printable  是否打印日志到 logcat
 * @property minLevel   最小日志输出级别
 * @property formatStrategy 日志格式策略
 */
open class LogcatDefaultPrinter(
    val printable: Boolean = true,
    val minLevel: LogLevel = LogLevel.V,
    val formatStrategy: BaseFormatStrategy = LogcatDefaultFormatStrategy()
) : BaseLogcatPrinter() {

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