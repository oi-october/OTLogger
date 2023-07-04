package com.xieql.lib.logger.core

import android.util.Log

/**
 * 日志级别
 */
enum class LogLevel(val describe:String,val logLevel:Int) {
    V("V",Log.VERBOSE),  //冗长日志
    D("D",Log.DEBUG),
    I("I",Log.INFO),
    W("W",Log.WARN),
    E("E",Log.ERROR),
    ASSERT("ASSERT",Log.ASSERT) //断言日志
}