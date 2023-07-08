package com.xieql.lib.logger.print

import com.xieql.lib.logger.LogLevel
import com.xieql.lib.logger.disk.BaseLogDiskStrategy
import com.xieql.lib.logger.format.base.BaseFormatStrategy

/**
 * 自定义日志文件打印机
 * @property printable 是否写入到文件
 * @property minLevel  最低输出日志
 * @property formatStrategy 日志格式策略
 * @property diskStrategy 文件管理策略
 */
open class LogTxtCustomPrinter(val printable:Boolean,val minLevel:LogLevel
        ,val formatStrategy:BaseFormatStrategy,val diskStrategy:BaseLogDiskStrategy):BaseLogTxtPrinter(){

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