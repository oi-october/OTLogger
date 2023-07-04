package com.xieql.lib.logger.print

import com.xieql.lib.logger.core.LogLevel
import java.nio.file.Path

/**
 * 文件夹管理
 */
open class LogDirStrategy {
    
    /**
     * 获取日志输出文件路径
     * @param logLevel 日志等级
     * @param tag  日志tag
     * @param msg  日志消息
     * @param throwable 日志异常消息
     */
    fun getLogPrintPath(logLevel: LogLevel,tag:String?,msg:String?,throwable: Throwable?):Path{

    }

}