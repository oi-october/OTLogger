package com.october.lib.logger.compress

import android.os.Handler
import android.os.HandlerThread
import com.october.lib.logger.disk.BaseLogDiskStrategy
import com.october.lib.logger.util.debugLog
import com.october.lib.logger.util.deleteFile
import java.io.File
import java.util.Arrays

/**
 * 日志压缩策略
 */
abstract class BaseLogCompressStrategy : HandlerThread {

    private lateinit var handler: Handler

    constructor() : super("LogCompress") {}

    init {
        this.start()
        handler = Handler(looper)
    }

    /**
     * 调用这里开始压缩文件
     * @param diskStrategy 当前的日志存储策略
     * @param logDir 日志文件夹
     */
    open fun compressLog(diskStrategy: BaseLogDiskStrategy, logDir: String) {
        handler.removeCallbacksAndMessages(null)
        handler.post {

            val logList = getCompressLogs(diskStrategy)
            debugLog("需要压缩日志数量:${logList.size} \n 日志名称:${Arrays.toString(logList.toTypedArray())}")
            if (logList.isNullOrEmpty()) {
                return@post
            }

            logList.forEach {
                val logZip = compressOne(diskStrategy,it)
                if(logZip != null && logZip.isFile && isDeleteOriginal()){
                    debugLog("删除日志文件:${it.absolutePath}")
                    it?.deleteFile()
                }
            }

        }
    }

    /**
     * 获取需要被压缩的的文件
     */
    abstract fun getCompressLogs(diskStrategy: BaseLogDiskStrategy): List<File>

    /**
     * 压缩一个文件
     * @param logFile 需要压缩日志文件
     * @return 压缩后的文件名称，压缩失败返回 null
     */
    abstract fun compressOne(diskStrategy: BaseLogDiskStrategy,logFile: File): File?

    /**
     * 压缩完成是否删除原件
     */
    abstract fun isDeleteOriginal(): Boolean

    /**
     * 判断是否是压缩文件
     */
    abstract fun isCompressFile(compressFile:File):Boolean
    
}