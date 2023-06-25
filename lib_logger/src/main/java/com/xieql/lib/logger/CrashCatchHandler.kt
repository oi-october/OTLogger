package com.xieql.lib.logger

import android.app.Activity
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.Process
import com.xieql.lib.logger.core.appCtx
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.ref.WeakReference
import kotlin.system.exitProcess

object CrashCatchHandler : Thread.UncaughtExceptionHandler {
    private const val TAG = "CrashCatchHandler"

    /*** 系统默认的UncaughtException处理类  */
    private var mDefHandler: Thread.UncaughtExceptionHandler? = null

    private val TIME = 4000L

    private var crashListener: CrashListener? = null


    private val activityList = arrayListOf<WeakReference<Activity>>()
    private lateinit var application: Application

    fun init(application: Application,crashListener: CrashListener?) {
        // 获取系统默认的UncaughtException处理器
        this.application = application
        mDefHandler = Thread.getDefaultUncaughtExceptionHandler()
        // 设置当前CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
        this.crashListener = crashListener
        collectAllActivity()
    }

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        if (!handleException(ex) && mDefHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefHandler!!.uncaughtException(thread, ex)
        } else {
            crashListener?.let {
                it.onCrash(thread, ex)
            }
            //异常上报给外部
            LogUtils.getHelper().getBugReporter()?.report(thread,ex)

            finishActivity()

            try {
                Thread.sleep(TIME)
            } catch (e: InterruptedException) {
                LogUtils.e(TAG, "delay error reboot error", e)
            }

            // 杀死进程
            Process.killProcess(Process.myPid())
            exitProcess(1)
        }
    }

    /**
     * 自定义错误处理
     *
     * @param e
     * @return 如果处理了该异常信息, 返回true;否则返回false.
     */
    private fun handleException(e: Throwable?): Boolean {
        if (e == null) {
            LogUtils.w(TAG, "handleException-ex == null")
            return false
        }

        val sw = StringWriter()
        val pw = PrintWriter(sw)
        e.printStackTrace(pw)
        val result: String = sw.toString()
        try {
            sw.close()
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        pw.close()
        // 保存异常信息到文件中
        LogUtils.e(TAG, result)
        return true
    }

    /**
     * 收集所有的Activity
     */
    private fun collectAllActivity(){
        application.registerActivityLifecycleCallbacks(object:Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                LogUtils.v(TAG,"创建Activity:${p0}")
                synchronized(this){
                    var target:WeakReference<Activity>? = null
                    activityList.forEach {
                        if(it.get() == p0){
                            target = it
                            return@forEach
                        }
                    }
                    if(target == null){
                        activityList.add(WeakReference(p0))
                    }
                }

            }
            override fun onActivityStarted(p0: Activity) {
                LogUtils.v(TAG,"${p0} 进入 onStart")
            }

            override fun onActivityResumed(p0: Activity) {
                LogUtils.v(TAG,"${p0} 进入 onResume")
            }

            override fun onActivityPaused(p0: Activity) {
                LogUtils.v(TAG,"${p0} 进入 onPaused")
            }

            override fun onActivityStopped(p0: Activity) {
                LogUtils.v(TAG,"${p0} 进入 onStop")
            }

            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
                LogUtils.v(TAG,"${p0} 进入 onSaveInstanceState")
            }

            override fun onActivityDestroyed(p0: Activity) {
                LogUtils.v(TAG,"销毁Activity:${p0}")

                synchronized(this){
                    var target:WeakReference<Activity>? = null
                    activityList.forEach {
                        if(it.get() == p0){
                            target = it
                            return@forEach
                        }
                    }
                    if(target == null){
                        activityList.remove(target)
                    }
                }

            }
        })
    }

    /**
     * 结束所有 Activity
     */
    private fun finishActivity(){
        activityList.forEach {
                try {
                    LogUtils.v(TAG,"结束Activity:${it.get()}")
                    it.get()?.finish()
                }catch (e:Exception){
                    LogUtils.e(TAG,"结束Activity失败：${it.get()}",e)
                }
            }
        }


}

/**
 * 异常上报
 */
interface BugReporter{
    /**
     * @param thread 异常线程
     * @param ex 异常消息
     * @return 上报是否成功
     */
    fun report(thread: Thread, ex: Throwable):Boolean
}



