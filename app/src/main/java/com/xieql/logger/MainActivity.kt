package com.xieql.logger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.xieql.lib.logger.LogUtils

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        testLog()
    }

    fun testLog(){
        for (i in 0 until 100){
            LogUtils.v("AAAA","测试日志：${i}")
            LogUtils.d("BBBB","测试日志：${i}")
            LogUtils.i("CCCC","测试日志：${i}")
            LogUtils.w("DDDD","测试日志：${i}",Exception("警告"))
            LogUtils.e("EEEE","测试日志：${i}",Exception("异常"))
        }

    }

}