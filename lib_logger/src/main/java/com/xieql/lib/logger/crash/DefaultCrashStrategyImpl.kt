package com.xieql.lib.logger.crash

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.xieql.lib.logger.LogUtils
import java.lang.ref.WeakReference

/**
 * 默认异常捕获机制
 * - 发生异常会自动捕获异常，打印到日志文件
 * - 然后退出所有Activity ,继而退出应用
 *
 * @property application
 */
class DefaultCrashStrategyImpl(val application: Application): BaseCrashStrategy() {

    private companion object{
        private const val TAG = "【崩溃】"
    }


    private val activityList = arrayListOf<WeakReference<Activity>>()

    init {
        init()
        collectAllActivity()
    }

    override fun handleException(t: Thread?, e: Throwable?): Boolean {
        LogUtils.wtf(TAG,"线程(${t?.name}) 崩溃",e,null)
        finishActivity()
        return true
    }

    //收集打开的Activity
    protected open fun collectAllActivity(){
        application.registerActivityLifecycleCallbacks(object:Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                LogUtils.v(TAG, "创建Activity:${p0}")
                synchronized(this){
                    var target: WeakReference<Activity>? = null
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
            override fun onActivityStarted(p0: Activity) {}

            override fun onActivityResumed(p0: Activity) {}

            override fun onActivityPaused(p0: Activity) {}

            override fun onActivityStopped(p0: Activity) {}

            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}

            override fun onActivityDestroyed(p0: Activity) {
                LogUtils.v(TAG, "销毁Activity:${p0}")

                synchronized(this){
                    var target: WeakReference<Activity>? = null
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

    protected open fun finishActivity(){
        LogUtils.i(TAG,"退出所有Activity")
        activityList.forEach {
            try {
                LogUtils.i(TAG,"结束Activity:${it.get()}")
                it.get()?.finish()
            }catch (e:Exception){
                LogUtils.e(TAG, "结束Activity失败：${it.get()}", e)
            }
        }
    }

}