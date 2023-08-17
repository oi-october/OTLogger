package com.october.lib.logger.print

import com.october.lib.logger.LogLevel
import com.october.lib.logger.disk.BaseLogDiskStrategy
import com.october.lib.logger.disk.TimeLogDiskStrategyImpl
import com.october.lib.logger.format.BaseFormatStrategy
import com.october.lib.logger.format.LogTxtDefaultFormatStrategy

/**
 * 默认日志文件打印机
 * @property printable 是否写入到文件
 * @property minLevel  最低输出日志
 * @property formatStrategy 日志格式策略
 * @property diskStrategy 文件管理策略
 */
open class LogTxtDefaultPrinter(
    val printable: Boolean = true,
    val minLevel: LogLevel = LogLevel.V,
    val formatStrategy: LogTxtDefaultFormatStrategy = LogTxtDefaultFormatStrategy(),
    val diskStrategy: BaseLogDiskStrategy = TimeLogDiskStrategyImpl()
) : BaseLogTxtPrinter() {

    override fun isPrint(): Boolean {
        return printable
    }

    override fun getPrintMinLevel(): LogLevel {
        return minLevel
    }

    override fun getLogcatFormatStrategy(): BaseFormatStrategy {
        return formatStrategy
    }

    override fun getLogDiskStrategy(): BaseLogDiskStrategy {
        return diskStrategy
    }

}