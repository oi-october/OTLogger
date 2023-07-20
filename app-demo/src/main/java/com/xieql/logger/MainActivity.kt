package com.xieql.logger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.xieql.lib.logger.LogUtils
import com.xieql.lib.logger.Logger

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        LogUtils.i("main","主界面")
    }

}