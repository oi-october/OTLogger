package com.xieql.lib.logger.format

import android.util.Log
import com.xieql.lib.logger.common.PACKAGE_NAME
import com.xieql.lib.logger.LogLevel
import com.xieql.lib.logger.core.TimeKt
import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException

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
     * @param extraParams 拓展参数，传入其他额外的数据，可以是额外的日志信息、日志格式信息等
     * @return 日志输出格式
     */
    abstract fun format(logLevel: LogLevel, tag:String?, msg:String?, thr: Throwable?,param:Any? = null):String

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
        return getStackTraceStringWithPrefix(thr,"")
    }

    /**
     * 获取异常堆栈信息，并且在每一行开头加入头部信息[prefix]
     * @param thr
     * @param prefix
     * @return
     */
    fun getStackTraceStringWithPrefix(thr: Throwable?,prefix:String):String{
        if (thr == null) {
            return "$prefix"
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        var t: Throwable? = thr
        while (t != null) {
            if (t is UnknownHostException) {
                return "$prefix"
            }
            t = t.cause
        }


        val sw = StringWriter()
        val pw = object : PrintWriter(sw){
            override fun println(x: Any?) {
                print("$prefix ")  //改写打印日志，添加自己的格式
                super.println(x)
            }
        }
        thr.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }


}