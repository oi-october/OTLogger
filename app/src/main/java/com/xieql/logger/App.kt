package com.xieql.logger

import android.app.Application
import com.xieql.lib.fclogger.LogUtils


class App: Application() {

    override fun onCreate() {
        super.onCreate()

        //TestFCLogger.initLogger(this)
        TestLogger.initLogger(this)
    }

}


