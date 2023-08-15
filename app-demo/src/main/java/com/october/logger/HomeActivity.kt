package com.october.logger

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.october.lib.logger.LogUtils
import com.october.lib.logger.Logger
import com.october.lib.logger.print.LogcatDefaultPrinter

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initListener()
    }

    private fun initListener(){
        findViewById<View>(R.id.btn_test_logcat_format).setOnClickListener {
            startActivity(Intent(this,TestLogcatActivity::class.java))
        }
        findViewById<View>(R.id.btn_test_logtxt).setOnClickListener {
            startActivity(Intent(this,TestLogTxtActivity::class.java))
        }
        findViewById<View>(R.id.btn_test_crash).setOnClickListener {
            startActivity(Intent(this,TestCrashActivity::class.java))
        }

    }





}