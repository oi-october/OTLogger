package com.october.lib.logger.print

import com.october.lib.logger.LogLevel
import com.october.lib.logger.disk.BaseLogDiskStrategy
import com.october.lib.logger.disk.TimeLogDiskStrategyImpl
import com.october.lib.logger.format.LogTxtDefaultFormatStrategy
import com.october.lib.logger.format.BaseFormatStrategy

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