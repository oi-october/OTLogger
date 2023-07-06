package com.xieql.lib.logger.format

import android.util.Log
import com.xieql.lib.logger.LogLevel
import com.xieql.lib.logger.format.base.BaseFormatStrategy

/**
 *  默认文件日志格式策略
 */
open class LogTxtDefaultFormatStrategy:BaseFormatStrategy(){

    companion object{
        val NEW_LINE = System.getProperty("line.separator")  //换行
        const val SPACE = " "  //空格
        const val COLON = ":"  //冒号
        const val CROSS_BAR = "-" //顿号
    }

    /**
     * 默认文件日志输出格式：
     *  年-月-日 时:分:秒.毫秒 pid-线程id/包名 等级 tag: msg
     *    thr.printMessage
     */
    override fun format(logLevel: LogLevel, tag: String?, msg: String?, thr: Throwable?, packageName: String): String {
        val tag = tag?:""
        val msg = msg?:"NULL"

        val builder = StringBuilder()
        builder.append(getNowTimeStr()) //时间
        builder.append(SPACE)
        builder.append(getPid()) //pid
        builder.append(CROSS_BAR)
        builder.append(getCurrentThreadId()) //线程id
        builder.append("/")
        builder.append(packageName) //包名
        builder.append(SPACE)
        builder.append(logLevel.describe) //日志级别
        builder.append(SPACE)
        builder.append(tag)  //tag
        builder.append(COLON)
        builder.append(msg) //日志
        builder.append(NEW_LINE)
        if(thr !=null){
            val errorMsg = Log.getStackTraceString(thr)
            builder.append(errorMsg)
            builder.append(NEW_LINE)
        }
        val log =  builder.toString()
        builder.clear()
        return log
    }




}