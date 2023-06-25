package com.xieql.logger

import android.app.Application
import android.util.Log
import com.xieql.lib.logger.CrashListener
import com.xieql.lib.logger.LogConfig
import com.xieql.lib.logger.LogReporter
import com.xieql.lib.logger.LogUtils

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        initLogger()
    }

    private fun initLogger(){
        val logName = "saasbox-cabinet"
        LogUtils.init(
            this, LogConfig.Builder()
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
    }

}

class LogReportWorker : LogReporter(){
    override fun start() {
        LogUtils.i("LogReportWorker","上报开始")
    }

    override fun stop() {
        LogUtils.i("LogReportWorker","上报结束")
    }
}

class MyCrashListener : CrashListener{
    override fun onCrash(thread: Thread, ex: Throwable) {
        Log.w("MyCrashListener","异常崩溃",ex)
    }
}
