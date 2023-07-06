package com.xieql.logger

import android.util.Log
import com.xieql.lib.logger.LogUtils

object TestLogger {

    private const val TAG = "TestLogger"

    fun initLogger(app: App){
        test()
    }

    fun test(){
        LogUtils.i(TAG,"测试")
        LogUtils.d(TAG,"Pid=${android.os.Process.myPid()}")
        LogUtils.w(TAG,"Uid=${android.os.Process.myUid()}")
        LogUtils.w(TAG,"Tid=${android.os.Process.myTid()}")
        LogUtils.v(TAG,"MainTid=${Thread.currentThread().id}")
        LogUtils.i(null,"${null}")
        LogUtils.w(TAG,"警告",Exception("这是一个警告"))
        LogUtils.e(TAG,"异常1")
        LogUtils.e(TAG,Exception("这是异常2"))
        LogUtils.e(TAG,"异常3",Exception("这是异常3"))
    }

}