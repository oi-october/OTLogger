package com.october.lib.logger.format

import com.october.lib.logger.LogLevel

/**
 * 默认Logcat日志格式策略
 */
open class LogcatDefaultFormatStrategy: BaseFormatStrategy(){
    /**
     * @return 输出日志格式 : 输出 msg 信息
     *  注意，因为最终还是使用官方的 Log.println(tag,msg,thr) 函数打印日志，所以我们能够定制部分有限。
     *  其中msg 就是最建立我们去定制的部分，如果能够接受google 官方的日志格式，直接使用该类即可。
     */
    override fun format(
        logLevel: LogLevel,
        tag: String?,
        msg: String?,
        thr: Throwable?,
        param: Any?
    ): String {
        if(thr != null){
            return "$msg  \n ${getStackTraceString(thr)}"
        }else{
            return "${msg}"
        }
    }

}