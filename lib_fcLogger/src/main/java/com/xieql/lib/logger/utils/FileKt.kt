package com.xieql.lib.logger.utils

import com.xieql.lib.logger.illegalArg
import com.xieql.lib.logger.utils.Encoding.CHARSET_GB2312
import com.xieql.lib.logger.utils.Encoding.CHARSET_ISO_8859_1
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream


const val ZIP_EXT = ".zip"

/** Check if a [File] exists. */
fun File?.isExist() = this?.exists() ?: false

/**
 * Creates a new, empty file named by this abstract pathname if
 * and only if a file with this name does not yet exist.
 *
 * @return `true` if file exist, `false` if file does not exist
 */
fun File.create(): Boolean {
    if (exists()) return true
    val dir: File = parentFile ?: return false

    if (!dir.exists()) {
        val result = dir.mkdirs()
        if (!result) return false
    }

    createNewFile()
    return exists()
}

/** Calculate the size of file. */
fun File.calSize(): Long {
    var size = 0L
    if (isDirectory) {
        val files = listFiles()
        if (files == null) {
            return 0
        } else {
            files.iterator().forEach {
                size += if (it.isDirectory) {
                    it.calSize()
                } else {
                    it.length()
                }
            }
        }
    } else if (isFile) {
        size += length()
    }
    return size
}

/**
 * Compress file or directory, if compressed file path([compressedFilePath]) is not specified,
 * it uses origin file path suffix with ‘.zip’ as compressed file path, if origin file needs to be
 * cleaned after compressing, you can make parameter [clean] to be `true`.
 */
fun File.zip(compressedFilePath: String? = null, clean: Boolean = false): File {
    val filePath = compressedFilePath ?: absolutePath + ZIP_EXT
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

/**
 * Uncompress a zip file to specified folder, if [folderPath] is not specified, it use origin zip
 * file's parent folder as target folder, if origin zip file needs to be cleaned after
 * uncompressing, you can make parameter [clean] to be `true`.
 */
fun File.unzip(folderPath: String? = null, clean: Boolean = false): File {
    val path = folderPath ?: parent ?: ""
    val folder = File(path)
    folder.mkdirs()
    if (!folder.isExist()) {
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

@Suppress("unused", "MemberVisibilityCanBePrivate")
object Encoding {
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