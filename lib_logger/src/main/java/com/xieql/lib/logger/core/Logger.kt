package com.xieql.lib.logger.core

import android.content.ContentValues
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.jakewharton.threetenabp.AndroidThreeTen
import com.xieql.lib.logger.core.Logger.Companion.ANONYMOUS_CLASS
import com.xieql.lib.logger.core.Logger.Companion.MAX_TAG_LENGTH
import com.xieql.lib.logger.core.LoggerMetaData.Companion.DIR_NAME
import com.xieql.lib.logger.core.LoggerMetaData.Companion.INFO_CONTENT_URI
import com.xieql.lib.logger.core.LoggerMetaData.Companion.NAME
import com.xieql.lib.logger.core.LoggerMetaData.Companion.PREFIX
import com.xieql.lib.logger.core.LoggerMetaData.Companion.SEGMENT
import com.xieql.lib.logger.core.LoggerMetaData.Companion.STORE_IN_SD_CARD
import com.xieql.lib.logger.core.LoggerMetaData.Companion.UPLOAD_TOKEN
import com.xieql.lib.logger.msg
import com.xieql.lib.logger.utils.debugLog
import java.io.PrintWriter
import java.io.StringWriter
import java.util.logging.Logger
import java.util.regex.Pattern

class Logger(val name: String, val packageLevel: Int = 0) {

    init {
        //初始化时间库
        AndroidThreeTen.init(appCtx)
    }

    var outputToConsole = true
    var outputToFile = true
    var outputThrowableStacktrace = false
    var logFilter: List<LogLevel> = listOf(
        LogLevel.WTF,
        LogLevel.E,
        LogLevel.W,
        LogLevel.I
    )
    var printer: Printer = DefaultPrinter()

    var storeInSdCard: Boolean = false
        set(value) {
            field = value
            refresh()
        }

    var logDir: String = "log"
        set(value) {
            field = value
            refresh()
        }

    var logPrefix: String = ""
        set(value) {
            field = value
            refresh()
        }

    var logSegment: LogSegment = LogSegment.TWENTY_FOUR_HOURS
        set(value) {
            field = value
            refresh()
        }

    var uploadToken: String = ""
        set(value) {
            field = value
            refresh()
        }

    init {
        refresh()
    }

    companion object {
        const val MAX_LOG_LENGTH = 4000
        const val MAX_TAG_LENGTH = 23
        val ANONYMOUS_CLASS: Pattern = Pattern.compile("(\\$\\d+)+$")
    }

    fun v(message: String?, vararg args: Any?) {
        printLog(
            packageLevel = 0,
            logLevel = LogLevel.V,
            t = null,
            message = message,
            args = args
        )
    }

    fun v(packageLevel: Int, message: String?, vararg args: Any?) {
        printLog(
            packageLevel = packageLevel,
            logLevel = LogLevel.V,
            t = null,
            message = message,
            args = args
        )
    }

    fun d(message: String?, vararg args: Any?) {
        printLog(
            packageLevel = 0,
            logLevel = LogLevel.D,
            t = null,
            message = message,
            args = args
        )
    }

    fun d(packageLevel: Int, message: String?, vararg args: Any?) {
        printLog(
            packageLevel = packageLevel,
            logLevel = LogLevel.D,
            t = null,
            message = message,
            args = args
        )
    }

    fun i(message: String?, vararg args: Any?) {
        printLog(
            packageLevel = 0,
            logLevel = LogLevel.I,
            t = null,
            message = message,
            args = args
        )
    }

    fun i(packageLevel: Int, message: String?, vararg args: Any?) {
        printLog(
            packageLevel = packageLevel,
            logLevel = LogLevel.I,
            t = null,
            message = message,
            args = args
        )
    }

    fun w(message: String?, vararg args: Any?) {
        printLog(
            packageLevel = 0,
            logLevel = LogLevel.W,
            t = null,
            message = message,
            args = args
        )
    }

    fun w(packageLevel: Int, message: String?, vararg args: Any?) {
        printLog(
            packageLevel = packageLevel,
            logLevel = LogLevel.W,
            t = null,
            message = message,
            args = args
        )
    }

    fun e(t: Throwable?) {
        printLog(packageLevel = 0, logLevel = LogLevel.E, t = t, message = null)
    }

    fun e(packageLevel: Int, t: Throwable?) {
        printLog(
            packageLevel = packageLevel,
            logLevel = LogLevel.E,
            t = t,
            message = null
        )
    }

    fun e(t: Throwable?, message: String?, vararg args: Any?) {
        printLog(
            packageLevel = 0,
            logLevel = LogLevel.E,
            t = t,
            message = message,
            args = args
        )
    }

    fun e(packageLevel: Int, t: Throwable?, message: String?, vararg args: Any?) {
        printLog(
            packageLevel = packageLevel,
            logLevel = LogLevel.E,
            t = t,
            message = message,
            args = args
        )
    }

    fun e(message: String?, vararg args: Any?) {
        printLog(
            packageLevel = 0,
            logLevel = LogLevel.E,
            t = null,
            message = message,
            args = args
        )
    }

    fun e(packageLevel: Int, message: String?, vararg args: Any?) {
        printLog(
            packageLevel = packageLevel,
            logLevel = LogLevel.E,
            t = null,
            message = message,
            args = args
        )
    }

    fun wtf(t: Throwable?) {
        printLog(packageLevel = 0, logLevel = LogLevel.WTF, t = t, message = null)
    }

    fun wtf(packageLevel: Int, t: Throwable?) {
        printLog(
            packageLevel = packageLevel,
            logLevel = LogLevel.WTF,
            t = t,
            message = null
        )
    }

    fun wtf(t: Throwable?, message: String?, vararg args: Any?) {
        printLog(
            packageLevel = 0,
            logLevel = LogLevel.WTF,
            t = t,
            message = message,
            args = args
        )
    }

    fun wtf(packageLevel: Int, t: Throwable?, message: String?, vararg args: Any?) {
        printLog(
            packageLevel = packageLevel,
            logLevel = LogLevel.WTF,
            t = t,
            message = message,
            args = args
        )
    }

    fun wtf(message: String?, vararg args: Any?) {
        printLog(
            packageLevel = 0,
            logLevel = LogLevel.WTF,
            t = null,
            message = message,
            args = args
        )
    }

    fun wtf(packageLevel: Int, message: String?, vararg args: Any?) {
        printLog(
            packageLevel = packageLevel,
            logLevel = LogLevel.WTF,
            t = null,
            message = message,
            args = args
        )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun refresh() {
        val resolver = appCtx.contentResolver
        val values = ContentValues(5).apply {
            put(NAME, name)
            put(DIR_NAME, logDir)
            put(PREFIX, logPrefix)
            put(SEGMENT, logSegment.value)
            put(STORE_IN_SD_CARD, storeInSdCard)
            put(UPLOAD_TOKEN, uploadToken)
        }
        resolver.insert(INFO_CONTENT_URI, values)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun prepareMessage(
        t: Throwable?,
        message: String?,
        vararg args: Any?
    ): String {
        var msg = message
        if (msg.isNullOrEmpty()) {
            if (t == null) {
                return ""
            }
            msg = if (outputThrowableStacktrace) t.toStackTraceString() else t.msg
        } else {
            if (args.isNotEmpty()) {
                msg = msg.format(args = args)
            }
            if (t != null) {
                msg += "\n${if (outputThrowableStacktrace) t.toStackTraceString() else t.msg}"
            }
        }
        return msg
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun logElement(packageLevel: Int): StackTraceElement {
        val elements = Throwable().stackTrace

        val index = elements
            .indexOfFirst {
                it.className !in fqcnIgnore
            }
            .let {
                val level: Int = if (packageLevel == 0) this.packageLevel else packageLevel
                it + level + 3
            }

        return elements[index]
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun printLog(
        packageLevel: Int,
        logLevel: LogLevel,
        t: Throwable?,
        message: String?,
        vararg args: Any?
    ) {
        val msg = prepareMessage(t, message, args = args)
        val element = logElement(packageLevel)  //获取打印日志的位置
        val tag = element.getTag()  //获取打印日志的类

        if (outputToConsole) {
            printer.printToConsole(logLevel, tag, msg, element)
        }
        debugLog("输出到文件条件:${outputToFile},${logFilter.contains(logLevel)}")
        val outputToFile = outputToFile && logFilter.contains(logLevel)

        if (outputToFile) {
            printer.printToFile(
                storeInSdCard,
                logLevel,
                msg,
                element,
                logDir,
                logPrefix,
                logSegment
            )
        }
    }
}

private val fqcnIgnore = listOf(
    Logger::class.java.name
)

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun StackTraceElement.getTag(): String {
    var tag = className.substringAfterLast('.')
    val m = ANONYMOUS_CLASS.matcher(tag)
    if (m.find()) {
        tag = m.replaceAll("")
    }

    // Tag length limit was removed in API 24.
    return if (tag.length <= MAX_TAG_LENGTH || Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        tag
    } else {
        tag.substring(0, MAX_TAG_LENGTH)
    }
}

/** Get stack trace string of throwable, which does not filter [java.net.UnknownHostException].  */
private fun Throwable.toStackTraceString(): String {
    val sw = StringWriter(256)
    val pw = PrintWriter(sw, false)
    printStackTrace(pw)
    pw.flush()
    return sw.toString()
}