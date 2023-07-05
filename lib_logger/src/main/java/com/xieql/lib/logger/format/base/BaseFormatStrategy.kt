package com.xieql.lib.logger.format.base

import android.util.Log
import com.xieql.lib.logger.common.PACKAGE_NAME
import com.xieql.lib.logger.LogLevel
import com.xieql.lib.logger.core.TimeKt

/**
 * 基础的输出格式
 */
abstract class BaseFormatStrategy {

    /**
     * @param logLevel  日志等级
     * @param tag  日志tag
     * @param msg  日志内容
     * @param thr  异常日志内存
     * @param packageName 打印日志的包的包名
     * @return 日志输出格式
     */
    abstract fun format(logLevel: LogLevel, tag:String?, msg:String?, thr: Throwable?, packageName:String= PACKAGE_NAME):String

    /**
     *  获取当前时间
     *  时间格式 ：yyyy-MM-dd HH:mm:ss.SSS
     */
    fun getNowTimeStr():String{
        return TimeKt.nowStr
    }

    /**
     * 获取进程id
     * @return
     */
    fun getPid():Int{
        return android.os.Process.myPid()
    }

    /**
     * 获取进程uid
     * @return
     */
    fun getUid():Int{
        return android.os.Process.myUid()
    }

    /**
     * 获取该进程的主线程id
     * @return
     */
    fun getTid():Int{
        return android.os.Process.myTid()
    }

    /**
     * 获取当前线程id
     * @return
     */
    fun getCurrentThreadId():Int{
        return Thread.currentThread().id.toInt()
    }

    /**
     * 获取当前线程名称
     * @return
     */
    fun getCurrentThreadName():String{
        return Thread.currentThread().name
    }

    /**
     * 获取异常信息
     * @param thr
     * @return
     */
    fun getStackTraceString(thr: Throwable?):String{
        return Log.getStackTraceString(thr)
    }


}