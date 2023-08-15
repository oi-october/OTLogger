package com.october.lib.logger.crash

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.october.lib.logger.LogUtils
import com.october.lib.logger.util.debugLog
import java.lang.ref.WeakReference

/**
 * 默认异常捕获机制
 * - 发生异常会自动捕获异常，打印到日志文件
 */
class DefaultCrashStrategyImpl(): BaseCrashStrategy() {

    private companion object{
        private const val TAG = "[crash]"
    }

    override fun init() {
        super.init()
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        debugLog("crash exception：${Thread.currentThread().name}")
        LogUtils.wtf(TAG,"Thread (${t?.name}) crashed ",e,null)
        getDefaultUncaughtExceptionHandler().uncaughtException(t,e)
    }

}