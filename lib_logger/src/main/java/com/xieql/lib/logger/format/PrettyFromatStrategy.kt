package com.xieql.lib.logger.format

import com.xieql.lib.logger.LogLevel
import java.util.logging.Logger

/***
 * 漂亮的Logcat日志输出格式
 *  参考：com.orhanobut.logger.PrettyFormatStrategy 的输出格式
 *  ┌──────────────────────────
 *  │ Method stack history
 *  ├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
 *  │ Thread information
 *  ├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
 *  │ TAG
 *  ├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
 *  │ Log message
 *  └──────────────────────────
 */
class PrettyFormatStrategy :LogcatDefaultFormatStrategy(){
    companion object{
        private const val TOP_LEFT_CORNER = '┌'
        private const val BOTTOM_LEFT_CORNER = '└'
        private const val MIDDLE_CORNER = '├'
        private const val HORIZONTAL_LINE = '│'
        private const val DOUBLE_DIVIDER = "────────────────────────────────────────────────────────"
        private const val SINGLE_DIVIDER = "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄"
        private const val TOP_BORDER = TOP_LEFT_CORNER.toString() + DOUBLE_DIVIDER + DOUBLE_DIVIDER
        private const val BOTTOM_BORDER = BOTTOM_LEFT_CORNER.toString() + DOUBLE_DIVIDER + DOUBLE_DIVIDER
        private const val MIDDLE_BORDER = MIDDLE_CORNER.toString() + SINGLE_DIVIDER + SINGLE_DIVIDER
    }
    override fun format(
        logLevel: LogLevel,
        tag: String?,
        msg: String?,
        thr: Throwable?,
        param: Any?
    ): String {
        val builder = StringBuilder(" \n")
        builder.append("${TOP_BORDER}\n")
        builder.append("${HORIZONTAL_LINE}     ${getMethodTask()}     \n")
        builder.append("${MIDDLE_BORDER}\n")
        builder.append("${HORIZONTAL_LINE}     Thread:${Thread.currentThread().name} (${Thread.currentThread().id})\n")
        builder.append("${MIDDLE_BORDER}\n")
        builder.append("${HORIZONTAL_LINE}     ${tag}")
        builder.append("${MIDDLE_BORDER}\n")
        if(thr != null){
            builder.append( "${HORIZONTAL_LINE}     $msg \n")
            builder.append(builderThrowableMsg(thr))
        }else{
            builder.append( "${HORIZONTAL_LINE}     ${msg}\n" )
        }
        builder.append("${BOTTOM_BORDER}\n")
        builder.append("\n")
        return builder.toString()
    }

    open fun getMethodTask():String{
        val elements = Throwable().stackTrace
        val index = elements
            .indexOfFirst {
                it.className !in fqcnIgnore
            }.let {
               return@let it +3
            }
        val element =  elements[index]
        val elementMsg ="(${element.fileName}:${element.lineNumber})#${element.methodName}"
        return elementMsg
    }

    open fun builderThrowableMsg(throwable: Throwable):String{
        val thrMsg = getStackTraceString(throwable)
        return thrMsg
    }

    private val fqcnIgnore = listOf(
        Logger::class.java.name
    )

}