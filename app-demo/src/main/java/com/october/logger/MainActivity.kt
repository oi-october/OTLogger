package com.october.logger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.october.lib.logger.LogUtils
import com.xieql.logger.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        LogUtils.i("main","主界面")
    }

}