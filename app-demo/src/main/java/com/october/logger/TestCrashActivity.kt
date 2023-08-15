package com.october.logger

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.october.lib.logger.LogUtils
import com.october.lib.logger.Logger
import com.october.lib.logger.crash.DefaultCrashStrategyImpl
import com.october.lib.logger.print.LogTxtDefaultPrinter
import com.october.lib.logger.print.LogcatDefaultPrinter

class TestCrashActivity :AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_crash)

        val logger = Logger.Builder()
            .setLogTxtPrinter(LogTxtDefaultPrinter())
            .setCrashStrategy(DefaultCrashStrategyImpl())
            .build()
        LogUtils.setLogger(logger)

        findViewById<Button>(R.id.btn_crash_main).setOnClickListener {
            val a = 1/0
        }
        findViewById<Button>(R.id.btn_crash_sub).setOnClickListener {
            Thread{
                val a = 1/0
            }.start()
        }

    }
}