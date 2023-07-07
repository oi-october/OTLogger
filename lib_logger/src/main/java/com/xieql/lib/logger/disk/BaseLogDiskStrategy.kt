package com.xieql.lib.logger.disk

import com.xieql.lib.logger.LogLevel

/**
 * 日志存储管理
 */
abstract class BaseLogDiskStrategy {

    /**
     * 获取日志输出文件路径
     * - 每次写入日志时候都会进行获取
     * @param logLevel 本次需要写入的日志级别
     * @param logBody  本次需要写入的数据
     * @param bodySize 本次需要写入的数据大小
     * @return 返回路径写入路径
     */
    abstract fun getLogPrintPath(logLevel: LogLevel, logBody: String?, bodySize: Long): String?

}