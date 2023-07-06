package com.xieql.logger

import android.app.Application


class App: Application() {

    override fun onCreate() {
        super.onCreate()

        TestLogger.initLogger(this)
    }

}


