package com.october.lib.logger.crash

import android.os.Process
import java.lang.Thread.UncaughtExceptionHandler
import kotlin.system.exitProcess

/**
 * 线程崩溃的异常捕获
 */
abstract class BaseCrashStrategy: Thread.UncaughtExceptionHandler{

    /*** 系统默认的UncaughtException处理类  */
    private lateinit var mDefHandler:UncaughtExceptionHandler


    open fun init(){
        if(!this::mDefHandler.isInitialized){
            mDefHandler = Thread.getDefaultUncaughtExceptionHandler()
        }
        Thread.setDefaultUncaughtExceptionHandler(this)
    }


    /**
     * 获取系统默认的 UncaughtExceptionHandler
     * @return
     */
    protected fun getDefaultUncaughtExceptionHandler():UncaughtExceptionHandler{
        return mDefHandler
    }





}