package com.xieql.logger

import android.util.Log
import com.xieql.lib.logger.LogLevel
import com.xieql.lib.logger.LogUtils
import com.xieql.lib.logger.Logger
import com.xieql.lib.logger.disk.FileLogDiskStrategyImpl
import com.xieql.lib.logger.disk.TimeLogDiskStrategyImpl
import com.xieql.lib.logger.format.LogTxtDefaultFormatStrategy
import com.xieql.lib.logger.format.LogcatDefaultFormatStrategy
import com.xieql.lib.logger.format.PrettyFormatStrategy
import com.xieql.lib.logger.print.LogTxtCustomPrinter
import com.xieql.lib.logger.print.LogcatCustomPrinter

object TestLoggerHelper {

    private const val TAG = "TestLogger"

    fun init(app: App) {
        //initLogger(app)
        //initLogger2(app)
        //initLogger3(app)
        //initLogger4(app)
        //initLogger5()
        initLogger6()
    }

    //测试默认你的日志打印策略
    private fun initLogger(app: App) {
        fun test() {
            for (i in 0 until 20) {
                Thread {
                    Thread.sleep(30000)
                    val TAG = TAG + "${Thread.currentThread().name}"
                    for (i in 0 until 5000) {
                        LogUtils.e(TAG, "----------------开始输出${i}---------------")
                        LogUtils.i(TAG, "测试")
                        LogUtils.d(TAG, "Pid=${android.os.Process.myPid()}")
                        LogUtils.w(TAG, "Uid=${android.os.Process.myUid()}")
                        LogUtils.w(TAG, "Tid=${android.os.Process.myTid()}")
                        LogUtils.v(TAG, "MainTid=${Thread.currentThread().id}")
                        LogUtils.i(null, "${null}")
                        LogUtils.w(TAG, "警告", Exception("这是一个警告"))
                        LogUtils.e(TAG, "异常1")
                        LogUtils.e(TAG, Exception("这是异常2"))
                        LogUtils.e(TAG, "异常3", Exception("这是异常3"))
                        LogUtils.e(TAG, "----------------结束输出${i}---------------")
                        Thread.sleep(50)
                    }
                }.start()
            }


        }
        test()
    }

    //测试不输出和最低输出日志策略的日志管理策略
    private fun initLogger2(app: App) {
        fun test2() {
            LogUtils.v(TAG, "V 日志")
            Thread.sleep(1000)
            LogUtils.d(TAG, "D 日志")
            Thread.sleep(1000)
            LogUtils.i(TAG, "I 日志")
            Thread.sleep(1000)
            LogUtils.w(TAG, "W 日志")
            Thread.sleep(1000)
            LogUtils.w(TAG, "W 日志2", Exception("W 日志2"))
            Thread.sleep(1000)
            LogUtils.e(TAG, "E 日志")
            Thread.sleep(1000)
            LogUtils.e(TAG, "E 日志2", Exception("E 日志2"))
            Thread.sleep(1000)
            LogUtils.e(TAG, Exception("E 日志3"))
            Thread.sleep(1000)
        }
        //设置自定义打印机日志
        LogUtils.setLogger(
            Logger.Builder()
                .setLogcatPrinter(
                    LogcatCustomPrinter(true, LogLevel.D, LogcatDefaultFormatStrategy())
                ).setLogTxtPrinter(
                    LogTxtCustomPrinter(
                        true,
                        LogLevel.I,
                        LogTxtDefaultFormatStrategy(),
                        TimeLogDiskStrategyImpl()
                    )
                ).build()
        )

        test2()
    }

    //测试日志自定义格式
    private fun initLogger3(app: App) {
        fun test3() {
            LogUtils.v(TAG, "V 日志")
            Thread.sleep(1000)
            LogUtils.d(TAG, "D 日志")
            Thread.sleep(1000)
            LogUtils.i(TAG, "I 日志")
            Thread.sleep(1000)
            LogUtils.w(TAG, "W 日志")
            Thread.sleep(1000)
            LogUtils.w(TAG, "W 日志2", Exception("W 日志2"))
            Thread.sleep(1000)
            LogUtils.e(TAG, "E 日志")
            Thread.sleep(1000)
            LogUtils.e(TAG, "E 日志2", Exception("E 日志2"))
            Thread.sleep(1000)
            LogUtils.e(TAG, Exception("E 日志3"))
            Thread.sleep(1000)
        }
        //自定义logcat 输出格式
        val logcatFormat = object : LogcatDefaultFormatStrategy() {
            override fun format(
                logLevel: LogLevel,
                tag: String?,
                msg: String?,
                thr: Throwable?,
                param: Any?
            ): String {
                var logBody = super.format(logLevel, tag, msg, thr, param)
                logBody = " \n -------- $tag start-----\n ${logBody}\n -------$tag end--------"
                return logBody
            }
        }
        val logTxtFormat = object : LogTxtDefaultFormatStrategy() {
            override fun format(
                logLevel: LogLevel,
                tag: String?,
                msg: String?,
                thr: Throwable?,
                params: Any?
            ): String {
                var logBody = super.format(logLevel, tag, msg, thr, params)
                logBody =
                    " \n ********* $tag start ********* \n ${logBody}\n  ********* $tag end ********* "
                return logBody
            }
        }

        LogUtils.setLogger(
            Logger.Builder()
                .setLogcatPrinter(
                    LogcatCustomPrinter(true, LogLevel.V, logcatFormat)
                ).setLogTxtPrinter(
                    LogTxtCustomPrinter(true, LogLevel.V, logTxtFormat, TimeLogDiskStrategyImpl())
                ).build()
        )

        test3()

    }

    //测试时间日志管理器
    private fun initLogger4(app: App) {
        fun test4() {
            Thread {
                while (true) {
                    LogUtils.v(TAG, "V 日志")
                    Thread.sleep(1000)
                    LogUtils.d(TAG, "D 日志")
                    Thread.sleep(1000)
                    LogUtils.i(TAG, "I 日志")
                    Thread.sleep(1000)
                    LogUtils.w(TAG, "W 日志")
                    Thread.sleep(1000)
                    LogUtils.w(TAG, "W 日志2", Exception("W 日志2"))
                    Thread.sleep(1000)
                    LogUtils.e(TAG, "E 日志")
                    Thread.sleep(1000)
                    LogUtils.e(TAG, "E 日志2", Exception("E 日志2"))
                    Thread.sleep(1000)
                    LogUtils.e(TAG, Exception("E 日志3"))
                    Thread.sleep(1000)
                }
            }.start()
        }
        LogUtils.setLogger(
            Logger.Builder()
                .setLogcatPrinter(
                    LogcatCustomPrinter(true, LogLevel.V, LogcatDefaultFormatStrategy())
                ).setLogTxtPrinter(
                    LogTxtCustomPrinter(
                        true,
                        LogLevel.V,
                        LogTxtDefaultFormatStrategy(),
                        TimeLogDiskStrategyImpl()
                    )
                ).build()
        )
        test4()

        /**
         * 测试数据
        adb shell
        date "2023-10-01 10:01:00"
        date "2023-10-01 11:01:00"
        date "2023-10-01 12:00:00"
        date "2023-10-01 13:01:00"
        date "2023-10-01 16:01:00"
        date "2023-10-02 11:00:00"
        date "2023-10-02 12:00:00"
        date "2023-10-03 12:00:00"
        date "2023-10-04 12:00:00"
        date "2023-10-05 12:00:00"
        date "2023-10-06 12:00:00"
        date "2023-10-07 12:00:00"
        date "2023-10-08 12:00:00"
        date "2023-10-09 12:00:00"
        date "2023-10-10 12:00:00"
         */
    }

    //验证文件管理策略
    fun initLogger5() {
        fun test5() {
            Thread {
                while (true) {
                    Thread.sleep(10)
                    LogUtils.v(TAG, "V 日志")
                    LogUtils.d(TAG, "D 日志")
                    LogUtils.i(TAG, "I 日志")
                    LogUtils.w(TAG, "W 日志")
                    LogUtils.w(TAG, "W 日志2", Exception("W 日志2"))
                    LogUtils.e(TAG, "E 日志")
                    LogUtils.e(TAG, "E 日志2", Exception("E 日志2"))
                    LogUtils.e(TAG, Exception("E 日志3"))
                }
            }.start()
        }
        LogUtils.setLogger(
            Logger.Builder()
                .setLogcatPrinter(
                    LogcatCustomPrinter(true, LogLevel.V, LogcatDefaultFormatStrategy())
                ).setLogTxtPrinter(
                    LogTxtCustomPrinter(
                        true,
                        LogLevel.V,
                        LogTxtDefaultFormatStrategy(),
                        FileLogDiskStrategyImpl()
                    )
                ).build()
        )
        test5()
    }

    //使用 PrettyFormatStrategy 格式输出日志
    fun initLogger6() {
        fun test6() {

            try {
                throw Exception("异常")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            LogUtils.v(TAG, "V 日志")
            LogUtils.d(TAG, "D 日志")
            LogUtils.i(TAG, "I 日志")
            LogUtils.w(TAG, "W 日志")
            LogUtils.w(TAG, "W 日志2", Exception("W 日志2"))
            LogUtils.e(TAG, "E 日志")
            LogUtils.e(TAG, "E 日志2", Exception("E 日志2"))
            LogUtils.e(TAG, Exception("E 日志3"))

        }
        LogUtils.setLogger(
            Logger.Builder()
                .setLogcatPrinter(
                    LogcatCustomPrinter(true, LogLevel.V, PrettyFormatStrategy())
                ).build()
        )
        test6()
    }




}