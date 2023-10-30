package com.october.lib.logger.compress

import com.october.lib.logger.disk.BaseLogDiskStrategy
import com.october.lib.logger.util.debugLog
import com.october.lib.logger.util.zip
import java.io.File
import java.io.FileFilter

/**
 * 压缩日志文件成为zip包
 */
class ZipLogCompressStrategy : BaseLogCompressStrategy() {

    companion object {
        private const val ZIP_SUFFIX = ".zip"
    }

    override fun getCompressLogs(diskStrategy: BaseLogDiskStrategy): List<File> {
        val logDir = diskStrategy.getLogDir() //获得文件夹路径
        val currentLogName = File(diskStrategy.getCurrentLogFilePath()).name  //获得当前正在写入的日志文件名称

        val files = File(logDir).listFiles(FileFilter { logFile ->
            if (logFile.name < currentLogName
                && (logFile.name.startsWith(diskStrategy.getLogPrefix()) && logFile.name.endsWith(diskStrategy.getLogSuffix()))
            ) {
                return@FileFilter true
            }
            return@FileFilter false
        })

        return files.toList()
    }

    override fun compressOne(diskStrategy: BaseLogDiskStrategy, logFile: File): File? {
        var fileName = logFile.name
        fileName = fileName.substring(0, fileName.lastIndexOf("."))
        val zipFileName = "${fileName}${ZIP_SUFFIX}"
        try {
            val zipFile =
                logFile.zip(logFile.parentFile.absolutePath + File.separator + zipFileName)
            return zipFile
        } catch (e: Exception) {
            debugLog("压缩log异常:${fileName}")
            e.printStackTrace()
            return null
        }
    }

    override fun isDeleteOriginal(): Boolean {
        return true
    }

    override fun isCompressFile(compressFile: File): Boolean {
        return compressFile.name.endsWith(ZIP_SUFFIX)
    }

}