package com.xieql.lib.logger

import android.app.Application
import com.xieql.lib.logger.core.LogLevel
import com.xieql.lib.logger.core.appCtx

object LogUtils {


    //默认Logger
    private var helper = LogConfig.Builder().build()
    private var logger = helper.init()

    const val PACKAGE_LEVEL = 1  //包体封装级别,级别正确才能打印出正确的日志位置
    const val PACKAGE_LEVEL_ENCRYPT = 2 //加密包体封装级别,级别正确才能打印出正确的日志位置

    private const val TAG = "SAASBOX-"

    fun init(application:Application,logHelper: LogConfig){
        this.helper = logHelper
        logger = logHelper.init()
        CrashCatchHandler.init(application,logHelper.getCrashListener())
        helper.getLogReporter()?.start()
    }

    fun getHelper():LogConfig{
        return helper
    }

    /**
     * verbose 日志：冗长日志，仅仅debug包打印该日志
     * @param tag  tag
     * @param msg 日志信息
     * @param tr 错误信息
     * @param packageLevel 封装级别，用于定位打印日志位置。没有重新封装日志库，必须要填写
     */
    fun v(tag:String?,msg:String?,tr:Throwable?=null,packageLevel:Int = PACKAGE_LEVEL):Int{
        if(BuildConfig.DEBUG){
            logger.printLog(
                packageLevel = packageLevel,
                logLevel =  LogLevel.V,
                t = tr,
                message = "$TAG$tag:$msg"
            )
        }
        return 0
    }

    /**
     * debug 日志
     * 调试日志
     */
    fun d(tag:String?,msg:String?,tr:Throwable?=null,packageLevel:Int = PACKAGE_LEVEL):Int{
        logger.printLog(
            packageLevel = packageLevel,
            logLevel =  LogLevel.D,
            t = tr,
            message = "$TAG$tag:$msg"
        )
        return 0
    }

    /**
     * debug 加密日志 ： debug 期间不加密
     *
     */
    fun dd(tag:String?,msg: String?,tr: Throwable?=null,packageLevel: Int = PACKAGE_LEVEL_ENCRYPT):Int{
        if(isDebug()){
            val tag = "${LogRule.LogTag.WITH_ENCRYPT_DEBUG} $tag"
            d(tag,msg,tr,packageLevel+1)
        }else{
            val tag = "${LogRule.LogTag.WITH_ENCRYPT} $tag"
            val msg = if(msg!=null)LogAESEncrypt.encrypt(msg) else msg
            d(tag,msg,tr,packageLevel+1)
        }
        return 0
    }


    /**
     * info 日志
     * 业务流程日志
     */
    fun i(tag:String?,msg:String?,tr: Throwable?=null,packageLevel:Int = PACKAGE_LEVEL):Int{
        logger.printLog(
            packageLevel = packageLevel,
            logLevel =  LogLevel.I,
            t = tr,
            message = "$TAG$tag:$msg"
        )
        return 0
    }

    /**
     * info 加密日志 ： debug 期间不加密
     *
     */
    fun ii(tag:String?,msg: String?,tr: Throwable?=null,packageLevel: Int = PACKAGE_LEVEL_ENCRYPT):Int{
        if(isDebug()){
            val tag = "${LogRule.LogTag.WITH_ENCRYPT_DEBUG} $tag"
            i(tag,msg,tr,packageLevel+1)
        }else{
            val tag = "${LogRule.LogTag.WITH_ENCRYPT} $tag"
            val msg = if(msg!=null)LogAESEncrypt.encrypt(msg) else msg
            i(tag,msg,tr,packageLevel+1)
        }
        return 0
    }

    /**
     * warn 日志
     * 警告日志：出现该日志表示可能有异常，或者工程师知道这个可能有异常，但是已经捕获
     */
    fun w(tag: String?, msg: String?, tr: Throwable?=null,packageLevel:Int = PACKAGE_LEVEL): Int{
        logger.printLog(
            packageLevel = packageLevel,
            logLevel =  LogLevel.W,
            t = tr,
            message = "$TAG$tag:$msg"
        )
        return 0
    }


    /**
     * warn 加密日志 ： debug 期间不加密
     *
     */
    fun ww(tag:String?,msg: String?,tr: Throwable?=null,packageLevel: Int = PACKAGE_LEVEL_ENCRYPT):Int{
        if(isDebug()){
            val tag = "${LogRule.LogTag.WITH_ENCRYPT_DEBUG} $tag"
            w(tag,msg,tr,packageLevel+1)
        }else{
            val tag = "${LogRule.LogTag.WITH_ENCRYPT} $tag"
            val msg = if(msg!=null)LogAESEncrypt.encrypt(msg) else msg
            w(tag,msg,tr,packageLevel+1)
        }
        return 0
    }


    /**
     * error 日志
     * 错误日志：不可能运行到的位置，运行到了。出现该级别的日志表示出现了异常/不正常的操作
     */
    fun e(tag: String?, msg: String?, tr: Throwable?=null,packageLevel:Int = PACKAGE_LEVEL): Int{
        logger.printLog(
            packageLevel = packageLevel,
            logLevel =  LogLevel.E,
            t = tr,
            message = "$TAG$tag:$msg"
        )
        return 0
    }

    /**
     * error 加密日志 ： debug 期间不加密
     *
     */
    fun ee(tag:String?,msg: String?,tr: Throwable?=null,packageLevel: Int = PACKAGE_LEVEL_ENCRYPT):Int{
        if(isDebug()){
            val tag = "${LogRule.LogTag.WITH_ENCRYPT_DEBUG} $tag"
            e(tag,msg,tr,packageLevel+1)
        }else{
            val tag = "${LogRule.LogTag.WITH_ENCRYPT} $tag"
            val msg = if(msg!=null)LogAESEncrypt.encrypt(msg) else msg
            e(tag,msg,tr,packageLevel+1)
        }
        return 0
    }


    /**
     * what a terrible failed 日志
     * 一个严重错误的日志：出现该日志一定是有 bug
     */
    fun wtf(tag: String?, msg: String?, tr: Throwable?=null,packageLevel:Int = PACKAGE_LEVEL): Int{
        logger.printLog(
            packageLevel = packageLevel,
            logLevel =  LogLevel.WTF,
            t = tr,
            message = "$TAG$tag:$msg"
        )
        return 0
    }


    /**
     * what a terrible failed 加密日志 ： debug 期间不加密
     *
     */
    fun wtff(tag:String?,msg: String?,tr: Throwable?=null,packageLevel: Int = PACKAGE_LEVEL_ENCRYPT):Int{
        if(isDebug()){
            val tag = "${LogRule.LogTag.WITH_ENCRYPT_DEBUG} $tag"
            wtf(tag,msg,tr,packageLevel+1)
        }else{
            val tag = "${LogRule.LogTag.WITH_ENCRYPT} $tag"
            val msg = if(msg!=null)LogAESEncrypt.encrypt(msg) else msg
            wtf(tag,msg,tr,packageLevel+1)
        }
        return 0
    }

}

fun isDebug():Boolean{
    return BuildConfig.DEBUG
}

