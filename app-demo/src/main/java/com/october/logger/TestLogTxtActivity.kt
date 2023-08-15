package com.october.logger

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.october.lib.logger.LogUtils
import com.october.lib.logger.Logger
import com.october.lib.logger.disk.FileAndTimeDiskStrategyImpl
import com.october.lib.logger.disk.FileLogDiskStrategyImpl
import com.october.lib.logger.disk.TimeLogDiskStrategyImpl
import com.october.lib.logger.format.LogTxtDefaultFormatStrategy
import com.october.lib.logger.print.LogTxtDefaultPrinter
import kotlin.math.log

class TestLogTxtActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "TestLogTxtActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_logtxt)
        initListener()
    }

    private fun initListener() {
        findViewById<Button>(R.id.btn_set_default_logtxt_printer).setOnClickListener {
            /**
             *  [LogTxtDefaultPrinter] default init with  [LogTxtDefaultFormatStrategy] and [TimeLogDiskStrategyImpl]
             */
            val logger = Logger.Builder().setLogTxtPrinter(LogTxtDefaultPrinter()).build()
            LogUtils.setLogger(logger)
        }
        findViewById<Button>(R.id.btn_set_time_disk_strategy).setOnClickListener {
            /**
             *  和 R.id.btn_set_default_logtxt_printer 一致
             */
            val logger = Logger.Builder()
                .setLogTxtPrinter(LogTxtDefaultPrinter(diskStrategy = TimeLogDiskStrategyImpl()))
                .build()
            LogUtils.setLogger(logger)
        }

        findViewById<Button>(R.id.btn_set_file_disk_strategy).setOnClickListener {
            val logger = Logger.Builder()
                .setLogTxtPrinter(LogTxtDefaultPrinter(diskStrategy = FileLogDiskStrategyImpl()))
                .build()
            LogUtils.setLogger(logger)
        }
        findViewById<Button>(R.id.btn_set_file_and_time_disk_strategy).setOnClickListener {
            val logger = Logger.Builder()
                .setLogTxtPrinter(LogTxtDefaultPrinter(diskStrategy = FileAndTimeDiskStrategyImpl()))
                .build()
            LogUtils.setLogger(logger)
        }

        findViewById<Button>(R.id.print_log).setOnClickListener {
            LogUtils.v(TAG, "V Log")
            LogUtils.d(TAG, "D Log")
            LogUtils.i(TAG, "I Log")
            LogUtils.w(TAG, "W Log")
            LogUtils.e(TAG, "E Log", Exception("this is a Exception"))
            Thread.sleep(10)
        }

    }

}