package com.xieql.lib.logger

/**
 * 日志上报
 */
abstract class LogReporter {

    /**
     * 开始任务
     */
    abstract fun start()

    /**
     * 取消任务
     */
    abstract fun stop()


}
