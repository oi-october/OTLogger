package com.october.logger

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.october.lib.logger.LogLevel
import com.october.lib.logger.LogUtils
import com.october.lib.logger.Logger
import com.october.lib.logger.crash.DefaultCrashStrategyImpl
import com.october.lib.logger.disk.FileAndTimeDiskStrategyImpl
import com.october.lib.logger.format.LogcatDefaultFormatStrategy
import com.october.lib.logger.print.IPrinter
import com.october.lib.logger.print.LogTxtDefaultPrinter
import com.october.lib.logger.print.LogcatDefaultPrinter

/**
 * 测试推荐配置
 */
class TestSuggestLogcatActivity :AppCompatActivity(){

    companion object{
        private const val TAG = "TestSuggestLogcatActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggest)

        findViewById<View>(R.id.btn_init_logger).setOnClickListener {
            //init
            val logger = Logger.Builder()
                .setLogcatPrinter(LogcatDefaultPrinter())  //设置 logcat printer
                .setLogTxtPrinter(LogTxtDefaultPrinter()) //设置 LogTxt printer
                .setCrashStrategy(DefaultCrashStrategyImpl()) //设置 crash ,捕获异常日志
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


  /*
   //自定义日志打印机，上传到云
   class LogcatCloudPrinter:IPrinter{
        //日志格式
        private val formatStrategy =  LogcatDefaultFormatStrategy()
        override fun print(
            logLevel: LogLevel,
            tag: String?,
            msg: String?,
            thr: Throwable?,
            param: Any?
        ) {
            if(logLevel == LogLevel.E){
                //日志信息
                val logInfo = formatStrategy.format(logLevel, tag, msg, thr, param)
                // todo 上传 ${logInfo} 到云端

            }
        }
    }*/


}