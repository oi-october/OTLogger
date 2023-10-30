package com.october.lib.logger.common

import android.os.StatFs
import androidx.core.content.pm.PackageInfoCompat
import com.october.lib.logger.util.appCtx

//包名
internal val PACKAGE_NAME by lazy { appCtx.packageName }

internal val LOG_HEARD_INFO by lazy{
    val builder = StringBuilder()
    builder.append("APP Version Name：${appVerName()}\n")
    builder.append("APP Version Code：${appVerCode()}\n")
    builder.append("System Version Code: ${android.os.Build.VERSION.RELEASE}\n")
    builder.append("System API: ${android.os.Build.VERSION.SDK_INT}\n")
    builder.append("Product Name: ${android.os.Build.PRODUCT}\n")
    builder.append("Manufacturer Name: ${android.os.Build.MANUFACTURER}\n")
    builder.append("Device Model: ${android.os.Build.MODEL}\n")
    builder.append("\n\n")

    return@lazy builder.toString()
}

internal fun appVerName(pkgName: String? = null): String {
    val info = appCtx.packageManager.getPackageInfo(pkgName ?: appCtx.packageName, 0)
    return info.versionName ?: ""
}

internal fun appVerCode(pkgName: String? = null): Int {
    val info = appCtx.packageManager.getPackageInfo(pkgName ?: appCtx.packageName, 0)
    return PackageInfoCompat.getLongVersionCode(info).toInt()
}


/**
 * 获取全部存储空间
 *  单位B
 */
internal fun getTotalStore(logDir: String): Long {
    val sf = StatFs(logDir)
    val blockSize = sf.blockSizeLong
    val blockCount = sf.blockCountLong
    val size = blockSize * blockCount
    return size
}

/**
 * 获取空余存储空间
 *  单位B
 */
internal fun getFreeStore(logDir: String): Long {
    val sf = StatFs(logDir)
    val blockSize = sf.blockSizeLong
    val blockCount = sf.availableBlocksLong
    val size = blockSize * blockCount
    return size
}
