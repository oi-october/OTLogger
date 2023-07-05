package com.xieql.lib.logger.print

import android.util.Log
import com.xieql.lib.logger.LogLevel
import com.xieql.lib.logger.format.base.BaseFormatStrategy

abstract class BaseLogcatPrinter {

    open fun print(logLevel: LogLevel, tag:String?, msg:String?, thr: Throwable?){
        if(!isPrint()){
            //debugLog("不需要打印到控制台")
            return
        }
        if(logLevel.logLevel<getPrintMinLevel().logLevel){
            //debugLog("日志级别比最低级别小，不需要打印")
            return
        }
        val msg = getLogcatFormatStrategy().format(logLevel,tag, msg, thr)
        Log.println(logLevel.logLevel,tag, msg)
    }

    //是否打印日志
    abstract fun isPrint():Boolean
    //最小打印级别，比他低级的日志不会打印
    abstract fun getPrintMinLevel(): LogLevel
    //日志输出格式
    abstract fun getLogcatFormatStrategy():BaseFormatStrategy




}