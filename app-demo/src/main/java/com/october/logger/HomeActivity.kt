package com.october.logger

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initListener()
    }

    private fun initListener(){
        findViewById<View>(R.id.btn_suggest).setOnClickListener {
            startActivity(Intent(this,TestSuggestLogcatActivity::class.java))
        }
        findViewById<View>(R.id.btn_test_log_format).setOnClickListener {
            startActivity(Intent(this,TestLogFormatActivity::class.java))
        }
        findViewById<View>(R.id.btn_test_logtxt_disk).setOnClickListener {
            startActivity(Intent(this,TestLogTxtDiskActivity::class.java))
        }
        findViewById<View>(R.id.btn_test_crash).setOnClickListener {
            startActivity(Intent(this,TestCrashActivity::class.java))
        }

    }





}