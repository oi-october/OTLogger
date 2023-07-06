package com.xieql.logger

import com.xieql.lib.logger.LogUtils

object TestLogger {

    private const val TAG = "TestLogger"

    fun initLogger(app: App){
        test()
    }

    fun test(){
        LogUtils.i(TAG,"测试")
    }

}