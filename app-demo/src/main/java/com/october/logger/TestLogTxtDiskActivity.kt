package com.october.logger

import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.october.lib.logger.LogUtils
import com.october.lib.logger.Logger
import com.october.lib.logger.compress.BaseLogCompressStrategy
import com.october.lib.logger.compress.ZipLogCompressStrategy
import com.october.lib.logger.disk.BaseLogDiskStrategy
import com.october.lib.logger.disk.FileAndTimeDiskStrategyImpl
import com.october.lib.logger.disk.FileLogDiskStrategyImpl
import com.october.lib.logger.disk.TimeLogDiskStrategyImpl
import com.october.lib.logger.format.LogTxtDefaultFormatStrategy
import com.october.lib.logger.format.LogTxtPrettyFormatStrategy
import com.october.lib.logger.print.LogTxtDefaultPrinter
import kotlin.math.log

/**
 * 测试日志磁盘管理策略
 */
class TestLogTxtDiskActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "TestLogTxtActivity"
    }

    private var logCompressStrategy:BaseLogCompressStrategy? = null //压缩

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_logtxt_disk)
        initListener()
    }

    private fun initListener() {

        findViewById<RadioGroup>(R.id.radio_group).setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.btn_compress->{
                     logCompressStrategy = ZipLogCompressStrategy()
                }
                else->{
                    logCompressStrategy = null
                }
            }
        }

        findViewById<Button>(R.id.btn_set_time_disk_strategy).setOnClickListener {
            /**
             *  [LogTxtDefaultPrinter]默认使用[TimeLogDiskStrategyImpl]磁盘管理策略
             */
            val diskStrategy= TimeLogDiskStrategyImpl().also {
                it.setLogCompressStrategy(logCompressStrategy) //设置压缩策略
            }
            val logger = Logger.Builder()
                .setLogTxtPrinter(
                    LogTxtDefaultPrinter(diskStrategy = diskStrategy)
                )
                .setIsDebug(true)
                .build()
            LogUtils.setLogger(logger)
        }

        findViewById<Button>(R.id.btn_set_file_disk_strategy).setOnClickListener {
            val diskStrategy = FileLogDiskStrategyImpl().also {
                it.setLogCompressStrategy(logCompressStrategy) //设置压缩策略
            }
            val logger = Logger.Builder()
                .setLogTxtPrinter(
                    LogTxtDefaultPrinter(diskStrategy = diskStrategy)
                )
                .setIsDebug(true)
                .build()
            LogUtils.setLogger(logger)
        }
        findViewById<Button>(R.id.btn_set_file_and_time_disk_strategy).setOnClickListener {
            val diskStrategy = FileAndTimeDiskStrategyImpl().also {
                it.setLogCompressStrategy(logCompressStrategy) //设置压缩策略
            }
            val logger = Logger.Builder()
                .setLogTxtPrinter(
                    LogTxtDefaultPrinter(diskStrategy = diskStrategy)
                )
                .setIsDebug(true)
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