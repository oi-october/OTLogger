package com.xieql.lib.logger.core

import android.os.Environment
import android.util.Log
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import com.xieql.lib.logger.core.Logger.Companion.MAX_LOG_LENGTH
import com.xieql.lib.logger.unexpectedValue
import com.xieql.lib.logger.utils.create
import com.xieql.lib.logger.utils.isExist
import java.io.File
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

interface Printer {
    /** Print log to console. */
    fun printToConsole(
        logLevel: LogLevel,
        tag: String,
        message: String,
        element: StackTraceElement
    )

    /** Print log to file. */
    fun printToFile(
        storeInSdCard: Boolean,
        logLevel: LogLevel,
        message: String,
        element: StackTraceElement,
        logDir: String,
        logPrefix: String,
        logSegment:LogSegment
    )
}

object PrinterExecutor {
    /** 可执行最大线程数. */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    const val MAX_THREAD = 5

    /** 队列等待时间，超过这个时间可释放线程，单位秒 */
    const val QUEUE_WAIT_TIME = 30L

    private val threadFlag: AtomicInteger = AtomicInteger(1)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val threadPool by lazy {
        ThreadPoolExecutor(
            0,
            Int.MAX_VALUE,
            60L,
            TimeUnit.MILLISECONDS,
            SynchronousQueue()
        ) { r -> Thread(r, "logger #${threadFlag.getAndIncrement()}") }
    }

    val cacheQueue by lazy { LinkedBlockingDeque<LogToFileInfo>() }

    val look = ReentrantLock()

    private fun getActiveThreadCount(): Int {
        return threadPool.activeCount
    }

    fun executePool() {
        look.lock()
        try {
            if (getActiveThreadCount() < MAX_THREAD) {
                threadPool.execute(WriteLogToFileRunnable())
            }
        } catch (ignore: Exception) {
            // ignore
        } finally {
            look.unlock()
        }
    }
}

class DefaultPrinter : Printer {
    override fun printToConsole(
        logLevel: LogLevel,
        tag: String,
        message: String,
        element: StackTraceElement
    ) {
        log(logLevel, tag, decorateMsgForConsole(message, element))
    }

    override fun printToFile(
        storeInSdCard: Boolean,
        logLevel: LogLevel,
        message: String,
        element: StackTraceElement,
        logDir: String,
        logPrefix: String,
        logSegment: LogSegment
    ) {
        PrinterExecutor.executePool()
        val msg = decorateMsgForFile(logLevel, message, element)
        PrinterExecutor.cacheQueue.offer(
            LogToFileInfo(
                storeInSdCard,
                logDir,
                logPrefix,
                logSegment,
                msg
            )
        )
    }
}

internal fun decorateMsgForConsole(message: String, element: StackTraceElement): String =
    "------------------------------ (${element.fileName}:${element.lineNumber})" +
        "#${element.methodName} Thread:${Thread.currentThread().name} \n$message\n \n"

internal fun decorateMsgForFile(
    logLevel: LogLevel,
    message: String,
    element: StackTraceElement
): String = "[$nowStr $logLevel] > $message  [${element.fileName}:${element.lineNumber}  " +
    "${Thread.currentThread().name}]\n\n"


internal fun log(logLevel: LogLevel, tag: String, message: String) {
    val subNum = message.length / MAX_LOG_LENGTH
    if (subNum > 0) {
        var index = 0
        repeat(subNum) {
            val lastIndex = index + MAX_LOG_LENGTH
            val sub = message.substring(index, lastIndex)
            logSub(logLevel, tag, sub)
            index = lastIndex
        }

        logSub(logLevel, tag, message.substring(index, message.length))
    } else {
        logSub(logLevel, tag, message)
    }
}

internal fun logSub(logLevel: LogLevel, tag: String, message: String) {
    when (logLevel) {
        LogLevel.V -> Log.v(tag, message)
        LogLevel.D -> Log.d(tag, message)
        LogLevel.I -> Log.i(tag, message)
        LogLevel.W -> Log.w(tag, message)
        LogLevel.E -> Log.e(tag, message)
        LogLevel.WTF -> Log.wtf(tag, message)
    }
}

data class LogToFileInfo(
    val storeInSdCard: Boolean,
    val logDir: String,
    val logPrefix: String,
    val logSegment: LogSegment,
    val message: String
)

class WriteLogToFileRunnable : Runnable {
    override fun run() {
        try {
            while (!Thread.interrupted()) {
                val info = PrinterExecutor.cacheQueue.poll(
                    PrinterExecutor.QUEUE_WAIT_TIME,
                    TimeUnit.SECONDS
                )
                    ?: return
                PrinterExecutor.look.lock()
                try {
                    val dirPath = genDirPath(info.storeInSdCard, info.logDir)
                    if (dirPath != null) {
                        val fileName = genFileName(info.logPrefix, info.logSegment)
                        val filePath = dirPath + File.separator + fileName
                        write(filePath, info.message)
                    }
                } catch (ignore: Exception) {
                    // ignore
                } finally {
                    PrinterExecutor.look.unlock()
                }
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun write(filePath: String, content: String) {
        val file = File(filePath)
        var outContent: String = content
        if (!file.isExist()) {
            outContent = genInfo() + content
            file.create()
        }
        file.appendText(outContent)
    }
}

@Suppress("DEPRECATION")
fun genDirPath(storeInSdCard: Boolean, logDir: String): String? {
    return if (storeInSdCard) {
        val path = "/mnt/media_rw/extsd"
        if (File(path).isExist()) {
            path + File.separator + logDir
        } else {
            null
        }
    } else {
        Environment.getExternalStorageDirectory().absolutePath + File.separator + logDir
    }
}

fun File.logFiles(): Array<File>? {
    return listFiles { _, filename ->
        filename.endsWith(".log")
    }
}

fun Array<File>.filterLogFiles(logPrefix: String, logSegment: LogSegment): ArrayList<File> {
    val referFileName = genFileName(logPrefix, logSegment)
    val files = arrayListOf<File>()
    forEach {
        val fileName = it.name
        if (fileName.startsWith(logPrefix) and
            (fileName.length == referFileName.length) and
            (fileName < referFileName)
        ) {
            files.add(it)
        }
    }
    return files
}

fun genFileName(logPrefix: String, logSegment: LogSegment): String {
    val prefix = if (logPrefix.isEmpty()) "" else "${logPrefix}_"
    return if (logSegment == LogSegment.TWENTY_FOUR_HOURS) {
        "$prefix$dateStr.log"
    } else {
        val segment = getCurSegment(logSegment)
        "$prefix${dateStr}_$segment.log"
    }
}

fun getCurSegment(logSegment: LogSegment): String {
    val hour = now.hour
    val start = hour - hour % logSegment.value
    var end = start + logSegment.value
    if (end == 24) {
        end = 0
    }
    return getDoubleNum(start) + getDoubleNum(end)
}

internal fun getDoubleNum(num: Int): String {
    val numStr = num.toString()
    return when {
        num < 0 -> {
            unexpectedValue(num)
        }
        num < 10 -> {
            numStr.padStart(2, '0')
        }
        num < 100 -> {
            numStr
        }
        else -> {
            unexpectedValue(num)
        }
    }
}

internal fun genInfo(): String {
    fun appStr(@StringRes stringResId: Int) = appCtx.resources.getString(stringResId)
    return ""
}
