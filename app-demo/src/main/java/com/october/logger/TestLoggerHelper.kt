package com.october.logger

import com.october.lib.logger.LogLevel
import com.october.lib.logger.LogUtils
import com.october.lib.logger.Logger
import com.october.lib.logger.crash.DefaultCrashStrategyImpl
import com.october.lib.logger.disk.BaseTimeLogDiskStrategy
import com.october.lib.logger.disk.FileAndTimeDiskStrategyImpl
import com.october.lib.logger.disk.FileLogDiskStrategyImpl
import com.october.lib.logger.disk.TimeLogDiskStrategyImpl
import com.october.lib.logger.format.LogTxtDefaultFormatStrategy
import com.october.lib.logger.format.LogcatDefaultFormatStrategy
import com.october.lib.logger.format.PrettyFormatStrategy
import com.october.lib.logger.print.LogTxtCustomPrinter
import com.october.lib.logger.print.LogTxtDefaultPrinter
import com.october.lib.logger.print.LogcatCustomPrinter
import com.october.lib.logger.print.LogcatDefaultPrinter

object TestLoggerHelper {

    private const val TAG = "TestLogger"

    fun init(app: App) {
        //userDefaultLogger()
        //initLoggerWithDefault()
        initLoggerWithPrettyFormat()
    }

    fun userDefaultLogger(app: App) {
        val logger = Logger.Builder()
            .setCrashStrategy(DefaultCrashStrategyImpl(app)) //设置捕获策略
            .build()
        LogUtils.setLogger(logger)
        LogUtils.v(TAG, "V 日志")
        LogUtils.d(TAG, "D 日志")
        LogUtils.i(TAG, "I 日志")
        LogUtils.w(TAG, "W 日志")
        LogUtils.e(TAG, "E 异常", Exception("这是一个异常"))

        //定制一个Logger
        val logger2 = Logger.Builder()
            .setLogTxtPrinter(
                LogTxtCustomPrinter(
                    true                    //是否打印到日志文件
                    ,
                    LogLevel.V                   //最低打印日志级别
                    ,
                    LogTxtDefaultFormatStrategy() //日志格式
                    ,
                    TimeLogDiskStrategyImpl()   //日志文件管理策略
                )
            )
            .build()
//设置使用该Logger
        LogUtils.setLogger(logger)
    }


    fun initLoggerWithDefault() {
        //自定义一个 Logger ，保持和默认的Logcat一致
        val logger = Logger.Builder()
            .setLogcatPrinter(LogcatDefaultPrinter()) //设置默认的Logcat Printer
            .setLogTxtPrinter(LogTxtDefaultPrinter()) //设置默认的LogTxt Printer
            .build()
        LogUtils.setLogger(logger)

        LogUtils.v(TAG, "V 日志")
        LogUtils.d(TAG, "D 日志")
        LogUtils.i(TAG, "I 日志")
        LogUtils.w(TAG, "W 日志")
        LogUtils.e(TAG, "E 异常", Exception("这是一个异常"))
    }

    fun initLoggerWithPrettyFormat() {
        //自定义一个 Logger ，Logcat 使用 PrettyFormatStrategy 格式
        val logger = Logger.Builder()
            .setLogcatPrinter(
                LogcatCustomPrinter(
                    true,
                    LogLevel.V,
                    PrettyFormatStrategy()
                )
            ) //设置Logcat Printer
            .setLogTxtPrinter(LogTxtDefaultPrinter()) //设置默认的LogTxt Printer
            .build()
        LogUtils.setLogger(logger)
        LogUtils.v(TAG, "V 日志")
        LogUtils.d(TAG, "D 日志")
        LogUtils.i(TAG, "I 日志")
        LogUtils.w(TAG, "W 日志")
        LogUtils.e(TAG, "E 异常", Exception("这是一个异常"))
    }


    fun initLoggerWithFileAndTimeDiskStrategy() {

        //我的日志管理策略
        val fileAndTimeDiskStrategyImpl = object : FileAndTimeDiskStrategyImpl() {
            override fun getLogDir(): String {
                return super.getLogDir()  //我的日志文件夹
            }

            override fun getMinFreeStoreOfMB(): Long {
                return 100  //设置系统必须至少还有100MB才能继续创建日志文件
            }

            override fun getLogDirMaxStoreOfMB(): Long {
                return 200  //设置日志文件夹最多容纳多少MB的日志
            }

            override fun getSegment(): BaseTimeLogDiskStrategy.LogTimeSegment {
                return BaseTimeLogDiskStrategy.LogTimeSegment.ONE_HOUR //设置每次间隔一个小时创建一个日志文件
            }

            override fun getLogFileMaxSizeOfMB(): Long {
                return 10  //设置每个日志文件最大是 10M ,超过10M自动创建下一个日志文件
            }

        }

        /*val timeDiskStrategyImpl = object :TimeLogDiskStrategyImpl(){
            override fun getLogDir(): String {
                return super.getLogDir()  //我的日志文件夹
            }

            override fun getSegment(): LogTimeSegment {
                return BaseTimeLogDiskStrategy.LogTimeSegment.THREE_HOURS //设置每次间隔2个小时创建一个日志文件
            }
        }*/

        /*val fileDiskStrategyImpl = object :FileLogDiskStrategyImpl(){
            override fun getLogDir(): String {
                return super.getLogDir()  //我的日志文件夹
            }

            override fun getLogFileMaxSizeOfMB(): Long {
                return 10  //设置每个日志文件最大是 10M ,超过10M自动创建下一个日志文件
            }

            override fun getMinFreeStoreOfMB(): Long {
                return 100  //设置系统必须至少还有100MB才能继续创建日志文件
            }

            override fun getLogDirMaxStoreOfMB(): Long {
                return 200  //设置日志文件夹最多容纳多少MB的日志
            }
        }*/


        val logger = Logger.Builder()
            .setLogTxtPrinter(
                LogTxtCustomPrinter(//设置默认的LogTxt Printer
                    true,
                    LogLevel.V,
                    LogTxtDefaultFormatStrategy(),
                    fileAndTimeDiskStrategyImpl
                )
            )
            .build()
        LogUtils.setLogger(logger)
        LogUtils.v(TAG, "V 日志")
        LogUtils.d(TAG, "D 日志")
        LogUtils.i(TAG, "I 日志")
        LogUtils.w(TAG, "W 日志")
        LogUtils.e(TAG, "E 异常", Exception("这是一个异常"))
    }


    fun initLoggerWithCrash(app: App) {
        val logger = Logger.Builder()
            .setCrashStrategy(DefaultCrashStrategyImpl(app))  //配置异常捕获策略
            .build()
        LogUtils.setLogger(logger)
    }

}