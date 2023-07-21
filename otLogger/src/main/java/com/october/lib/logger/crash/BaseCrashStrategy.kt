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

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        if(!handleException(t,e)){
            //如果没有处理异常则让系统默认的异常处理器来处理
            getDefaultUncaughtExceptionHandler().uncaughtException(t,e)
        }
    }

    /**
     * 获取系统默认的 UncaughtExceptionHandler
     * @return
     */
    protected fun getDefaultUncaughtExceptionHandler():UncaughtExceptionHandler{
        return mDefHandler
    }


    /**
     * 自定义错误处理
     * @param t
     * @param e
     * @return 如果处理了该异常信息, 返回true;否则返回false.
     */
    abstract fun handleException(t:Thread? ,e: Throwable?): Boolean



}