package com.october.logger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import com.october.lib.logger.LogUtils
import com.october.lib.logger.Logger
import com.october.lib.logger.format.LogTxtDefaultFormatStrategy
import com.october.lib.logger.format.LogTxtPrettyFormatStrategy
import com.october.lib.logger.format.LogcatDefaultFormatStrategy
import com.october.lib.logger.format.LogcatPrettyFormatStrategy
import com.october.lib.logger.print.LogTxtDefaultPrinter
import com.october.lib.logger.print.LogcatDefaultPrinter

/**
 * 测试日志格式策略
 */
class TestLogFormatActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "TestLogcatActivity"
    }
    var logtxtFormatStrategy = LogTxtDefaultFormatStrategy()
    var logcatFormatStrategy = LogcatDefaultFormatStrategy()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_log_format)
        initListener()

        val logger = Logger.Builder()
            .setLogcatPrinter(LogcatDefaultPrinter(formatStrategy = logcatFormatStrategy))
            .setLogTxtPrinter(LogTxtDefaultPrinter(formatStrategy = logtxtFormatStrategy))
            .setIsDebug(true)
            .build()
        LogUtils.setLogger(logger)

    }

    private fun initListener(){

        findViewById<RadioGroup>(R.id.format_group).setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.btn_default_format -> {
                    logtxtFormatStrategy = LogTxtDefaultFormatStrategy()
                    logcatFormatStrategy = LogcatDefaultFormatStrategy()
                    val logger = Logger.Builder()
                        .setLogcatPrinter(LogcatDefaultPrinter(formatStrategy = logcatFormatStrategy))
                        .setLogTxtPrinter(LogTxtDefaultPrinter(formatStrategy = logtxtFormatStrategy))
                        .setIsDebug(true)
                        .build()
                    LogUtils.setLogger(logger)
                }

                R.id.btn_pretty_format -> {
                    logtxtFormatStrategy = LogTxtPrettyFormatStrategy()
                    logcatFormatStrategy = LogcatPrettyFormatStrategy()
                    val logger = Logger.Builder()
                        .setLogcatPrinter(LogcatDefaultPrinter(formatStrategy = logcatFormatStrategy))
                        .setLogTxtPrinter(LogTxtDefaultPrinter(formatStrategy = logtxtFormatStrategy))
                        .setIsDebug(true)
                        .build()
                    LogUtils.setLogger(logger)
                }
            }
        }

        findViewById<Button>(R.id.print_log).setOnClickListener {
            LogUtils.v(TAG, "V Log")
            LogUtils.d(TAG, "D Log")
            LogUtils.i(TAG, "I Log")
            LogUtils.w(TAG, "W Log")
            LogUtils.e(TAG, "E Log", Exception("this is a Exception"))
        }
    }
    



}