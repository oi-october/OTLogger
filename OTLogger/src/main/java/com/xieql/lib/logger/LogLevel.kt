package com.xieql.lib.logger

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
    WTF("E",Log.ERROR)  //Log.wtf 输出的日志级别也是 E ,保持和 Log 一致
    ;

}