package com.xieql.logger

import com.xieql.lib.logger.LogUtils

object TestLoggerHelper {

    private const val TAG = "TestLogger"

    fun initLogger(app: App){
        test()
    }

    fun test(){
        for (i in 0 until 20){
            Thread{
                Thread.sleep(30000)
                val TAG = TAG +"${Thread.currentThread().name}"
                for (i in 0 until 5000){
                    LogUtils.e(TAG,"----------------开始输出${i}---------------")
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
                    LogUtils.e(TAG,"----------------结束输出${i}---------------")
                    Thread.sleep(50)
                }
            }.start()
        }


    }

}