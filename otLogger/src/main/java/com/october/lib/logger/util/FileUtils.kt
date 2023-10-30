package com.october.lib.logger.util

import java.io.File

/**
 * 判断文件是否存在
 */
fun File?.isExist() = this?.exists() ?: false

/**
 * 删除文件
 * @return 是否删除成功
 */
fun File.deleteFile():Boolean{
    try {
        if(!this.isExist())return true
        if(this.isFile && this.exists())this.delete()
    }catch (e:Exception){
        e.printStackTrace()
    }
    return !this.isExist()
}


/**
 * 创建文件
 * @return 创建是否成功
 */
fun File.create(): Boolean {
    if (this.isExist()) return true
    val dir: File = this.parentFile ?: return false

    if (!dir.exists()) {
        val result = dir.mkdirs()
        if (!result) return false
    }
    this.createNewFile()
    return this.isExist()
}