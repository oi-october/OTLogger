package com.october.lib.logger.util

import com.october.lib.logger.util.Encoding.CHARSET_GB2312
import com.october.lib.logger.util.Encoding.CHARSET_ISO_8859_1
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

private const val ZIP_EXT = ".zip"

fun File.zip(compressedFilePath: String? = null, clean: Boolean = false): File {
    val filePath = compressedFilePath?:absolutePath + ZIP_EXT
    val targetFile = File(filePath)
    val targetParentFile = targetFile.parentFile
    if (targetParentFile == null) {
        illegalArg(filePath, "parentFile cannot be null")
    } else {
        targetParentFile.mkdirs()
    }
    val zos = ZipOutputStream(FileOutputStream(filePath))

    fun action(file: File, path: String?) {
        val entryName = if (path != null) {
            path + File.separator + file.name
        } else {
            file.name
        }
        if (file.isDirectory) {
            val childFiles = file.listFiles()
            if (childFiles == null) {
                illegalArg(file, "childFiles cannot be null")
            } else {
                childFiles.forEach { action(it, entryName) }
            }
        } else {
            zos.putNextEntry(ZipEntry(entryName))
            file.forEachBlock { buffer, bytesRead ->
                zos.write(buffer, 0, bytesRead)
            }
            zos.flush()
            zos.closeEntry()
        }
    }

    zos.use {
        action(this@zip, null)
        if (clean) {
            this@zip.deleteRecursively()
        }
    }
    return File(filePath)
}


fun File.unzip(folderPath: String? = null, clean: Boolean = false): File {
    val path = folderPath ?: parent ?: ""
    val folder = File(path)
    folder.mkdirs()
    if (!folder.exists()) {
        illegalArg(path, "dir path is illegal")
    }

    fun getRealFile(baseDir: String, absFileName: String): File {
        val dirs = absFileName.split("/".toRegex()).toTypedArray()
        var file = File(baseDir)
        if (dirs.size > 1) {
            dirs.dropLast(1).forEach {
                val sub = String(it.toByteArray(CHARSET_ISO_8859_1), CHARSET_GB2312)
                file = File(file, sub)
            }
            if (!file.exists()) {
                file.mkdirs()
            }
            val sub =
                String(dirs.last().toByteArray(CHARSET_ISO_8859_1), CHARSET_GB2312)
            file = File(file, sub)
        } else {
            file = File(baseDir, absFileName)
        }
        return file
    }

    fun action(file: File, path: String) {
        val zipFile = ZipFile(file)
        zipFile.use { zip ->
            zip.entries().iterator().forEach {
                zipFile.getInputStream(it).copyTo(getRealFile(path, it.name).outputStream())
            }
        }
    }

    action(this, path)
    if (clean) {
        delete()
    }
    return File(path, nameWithoutExtension)
}

internal fun illegalArg(argument: Any?, errorMsg: String? = null): Nothing =
    throw IllegalArgumentException(
        "Illegal argument: $argument${if (errorMsg == null) "." else ", $errorMsg."}"
    )

@Suppress("unused", "MemberVisibilityCanBePrivate")
internal object Encoding {
    const val ISO_8859_1 = "ISO-8859-1"
    const val US_ASCII = "US-ASCII"
    const val UTF_16 = "UTF-16"
    const val UTF_16BE = "UTF-16BE"
    const val UTF_16LE = "UTF-16LE"
    const val UTF_8 = "UTF-8"
    const val GB2312 = "GB2312"

    val CHARSET_ISO_8859_1: Charset = Charset.forName(ISO_8859_1)
    val CHARSET_US_ASCII: Charset = Charset.forName(US_ASCII)
    val CHARSET_UTF_16: Charset = Charset.forName(UTF_16)
    val CHARSET_UTF_16BE: Charset = Charset.forName(UTF_16BE)
    val CHARSET_UTF_16LE: Charset = Charset.forName(UTF_16LE)
    val CHARSET_UTF_8: Charset = Charset.forName(UTF_8)
    val CHARSET_GB2312: Charset = Charset.forName(GB2312)
}