package com.xieql.lib.logger.util

import java.io.File
import java.nio.charset.Charset


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

