package com.october.logger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.october.lib.logger.LogUtils
import com.october.lib.logger.Logger
import com.october.lib.logger.format.PrettyFormatStrategy
import com.october.lib.logger.print.LogcatDefaultPrinter

class TestLogcatActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "TestLogcatActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_logcat)
        initListener()
    }

    private fun initListener(){
        findViewById<Button>(R.id.default_logcat_format).setOnClickListener {
            //系统默认Logger，默认时候使用如下的Logger：
            val logger = Logger.Builder()
                .setLogcatPrinter(LogcatDefaultPrinter())
                .build()
            LogUtils.setLogger(logger)
        }
        findViewById<Button>(R.id.pretty_logcat_format).setOnClickListener {
            val logger =  Logger.Builder()
                .setLogcatPrinter(LogcatDefaultPrinter(formatStrategy = PrettyFormatStrategy()))
                .build()
            LogUtils.setLogger(logger)
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