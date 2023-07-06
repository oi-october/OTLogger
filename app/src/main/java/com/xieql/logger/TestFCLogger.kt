package com.xieql.logger

import android.util.Log
import com.xieql.lib.fclogger.CrashListener
import com.xieql.lib.fclogger.LogConfig
import com.xieql.lib.fclogger.LogReporter
import com.xieql.lib.fclogger.LogUtils

object TestFCLogger {

    //测试 FC 日志模块
    fun initLogger(app: App) {
        val logName = "saasbox-cabinet"
        LogUtils.init(
            app, LogConfig.Builder()
                .setName(logName)
                .setOutputToConsole(true)
                .setOutputToFile(true)
                .setStoreInSdCard(true)
                .setSN("123456")
                .setReporter(LogReportWorker())  //日志上报
                .setCountlyAppKey("")
                .setCrashListener(MyCrashListener())
                .build()
        )
        testLog()
    }


    fun testLog() {
        LogUtils.i("Test","***************** start ****************")
        Thread {
            for (i in 0 until 100) {
                LogUtils.v("AAAA", "测试日志：${i}")
                LogUtils.d("BBBB", "测试日志：${i}")
                LogUtils.i("CCCC", "测试日志：${i}")
                LogUtils.w("DDDD", "测试日志：${i}", Exception("警告"))
                LogUtils.e("EEEE", "测试日志：${i}", Exception("异常"))
            }

            LogUtils.i("Test","***************** end ****************")

        }.start()

    }


    class LogReportWorker : LogReporter() {
        override fun start() {
            LogUtils.i("LogReportWorker", "上报开始")
        }

        override fun stop() {
            LogUtils.i("LogReportWorker", "上报结束")
        }
    }

    class MyCrashListener : CrashListener {
        override fun onCrash(thread: Thread, ex: Throwable) {
            Log.w("MyCrashListener", "异常崩溃", ex)
        }
    }


}