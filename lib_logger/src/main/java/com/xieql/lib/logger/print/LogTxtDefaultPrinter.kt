package com.xieql.lib.logger.print

import com.xieql.lib.logger.LogLevel
import com.xieql.lib.logger.disk.BaseLogDiskStrategy
import com.xieql.lib.logger.disk.TimeLogDiskStrategyImpl
import com.xieql.lib.logger.format.LogTxtDefaultFormatStrategy
import com.xieql.lib.logger.format.BaseFormatStrategy

/**
 * 默认日志文件打印机
 */
open class LogTxtDefaultPrinter:BaseLogTxtPrinter(){

    @Volatile
    protected var formatStrategy: BaseFormatStrategy? = null
    @Volatile
    protected var diskStrategy:BaseLogDiskStrategy? = null

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
                    formatStrategy = LogTxtDefaultFormatStrategy()
                }
            }
        }
        return formatStrategy!!
    }

    override fun getLogDiskStrategy(): BaseLogDiskStrategy {
        if(diskStrategy == null){
            synchronized(this){
                if(diskStrategy == null){
                    diskStrategy = TimeLogDiskStrategyImpl()
                }
            }
        }
        return diskStrategy!!
    }

}