package com.xieql.lib.logger

import com.xieql.lib.logger.core.*
import com.xieql.lib.logger.core.appCtx
import java.io.File

/**
 * log 配置类
 */
class LogConfig{

    companion object{

        private const val TAG = "LogHelper"

        //获取根路径
        fun genDirPath(storeInSdCard: Boolean, logDir: String): String? {
            if (storeInSdCard) {
                return appCtx.getExternalFilesDir("")?.absolutePath + File.separator + logDir
            } else {
                LogUtils.v(TAG, "不需要存储到文件")
            }
            return null
        }

    }

    private var crashListener: CrashListener? = null
    private lateinit var logger: Logger
    private lateinit var builder: Builder

    private constructor(builder: Builder){
        this.builder = builder
    }

    fun init(): Logger {
        logger = Logger(builder.name,builder.packageLevel).apply {
            outputToConsole = builder.outputToConsole
            outputToFile = builder.outputToFile
            storeInSdCard = builder.storeInSdCard
            logDir = builder.logDir
            logSegment = builder.logSegment
            logPrefix = builder.logPrefix
            printer = builder.printer
            outputThrowableStacktrace = builder.outputThrowableStacktrace
        }
        //设置日志输出级别
        logger.logFilter =  listOf(
            LogLevel.WTF,
            LogLevel.E,
            LogLevel.W,
            LogLevel.I,
            LogLevel.D
        )
        crashListener = builder.crashListener
        return logger
    }

    fun getCrashListener():CrashListener?{
        return crashListener
    }

    fun getSN():String{
        return builder.sn
    }

    fun setSN(sn: String){
         builder.sn = sn
    }


    fun getMaxLogNum():Int{
        return builder.maxLogNum
    }

    fun getLogReporter():LogReporter?{
        return builder.reporter
    }

    fun getBugReporter():BugReporter?{
        return builder.bugReporter
    }

    fun getRestartAppIfError():Boolean{
        return builder.restartAppIfError
    }

    fun getLogDir():String{
        return builder.logDir
    }

    fun getLogPrefix():String {
        return builder.logPrefix
    }

    fun getLogSegment(): LogSegment {
        return builder.logSegment
    }

    fun getCountlyAppKey():String{
        return builder.countlyAppKey
    }


    class Builder(){
         internal var name = "default"  //app的日志统称
         internal var packageLevel = 1 //输出级别
         internal var outputToConsole = true //是否输出到控制台
         internal var outputToFile = false  //是否输出到文件
         internal var storeInSdCard = false //是否输出到 sdcard
         internal var logDir = "log"  //log 存储地址文件夹名称，真正的存在在 sdcard/Android/data/packageName/${logDir}下
         internal var logSegment = LogSegment.ONE_HOUR //存储片段
         internal var logPrefix = ""  //文件头部标识
         internal lateinit var printer: Printer //日志输出者
         internal var reporter:LogReporter?= null  //日志上报者
         internal var outputThrowableStacktrace:Boolean = true //是否输出堆栈信息
         internal var crashListener: CrashListener? = null // 监听异常信息
         internal var sn = "sn-default"   // 设备SN 号码
         internal var maxLogNum = 24 * 7  //日志文件最多数量，保持7天数据
         internal var bugReporter:BugReporter? = null
         internal var restartAppIfError = true //发生错误是否重启
         internal var countlyAppKey = "" //countly app key


        fun build():LogConfig{
            val helper =  LogConfig(this)
            printer = LogPrinter()
            return helper
        }

        fun setName(name:String):Builder{
            this.name = name
            return this
        }

        fun setLevel(packageLevel:Int):Builder{
            this.packageLevel = packageLevel
            return this
        }

        fun setOutputToConsole(outputToConsole:Boolean):Builder{
            this.outputToConsole = outputToConsole
            return this
        }

        fun setOutputToFile(outputToFile: Boolean):Builder{
            this.outputToFile = outputToFile
            return this
        }

        fun setStoreInSdCard(storeInSdCard:Boolean):Builder{
            this.storeInSdCard = storeInSdCard
            return this
        }

        fun setLogDir(logDir:String):Builder{
            this.logDir = logDir
            return this
        }

        fun setLogSegment(logSegment:LogSegment):Builder{
            this.logSegment = logSegment
            return this
        }

        fun setLogPrefix(logPrefix:String):Builder{
            this.logPrefix = logPrefix
            return this
        }

        fun setPrinter(printer:Printer):Builder{
            this.printer = printer
            return this
        }

        fun setReporter(reporter: LogReporter):Builder{
            this.reporter = reporter
            return this
        }

        fun setCrashListener(crashListener: CrashListener?):Builder{
            this.crashListener = crashListener
            return this
        }

        fun setSN(sn:String):Builder{
            this.sn = sn
            return this
        }

        fun setMaxLogNum(maxLogNum:Int):Builder{
            this.maxLogNum = maxLogNum
            return this
        }

        fun setBugReporter(bugReporter: BugReporter):Builder{
            this.bugReporter = bugReporter
            return this
        }

        fun setRestartAppIfError(restartAppIfError:Boolean):Builder{
            this.restartAppIfError = restartAppIfError
            return this
        }

        fun setCountlyAppKey(countlyAppKey:String):Builder{
            this.countlyAppKey = countlyAppKey
            return this
        }
    }
}

interface CrashListener{
    fun onCrash(thread: Thread, ex: Throwable)
}
