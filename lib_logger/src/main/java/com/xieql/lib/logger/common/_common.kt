package com.xieql.lib.logger.common

import android.os.StatFs
import androidx.core.content.pm.PackageInfoCompat
import com.xieql.lib.logger.core.appCtx
import com.xieql.lib.logger.util.debugLog

//包名
internal val PACKAGE_NAME by lazy { appCtx.packageName }

internal val LOG_HEARD_INFO by lazy{
    val builder = StringBuilder()
    builder.append("应用版本名：${appVerName()}\n")
    builder.append("应用版本号：${appVerCode()}\n")
    builder.append("系统版本号: ${android.os.Build.VERSION.RELEASE}\n")
    builder.append("系统API等级: ${android.os.Build.VERSION.SDK_INT}\n")
    builder.append("产品名称: ${android.os.Build.PRODUCT}\n")
    builder.append("设备制造商: ${android.os.Build.MANUFACTURER}\n")
    builder.append("设备型号: ${android.os.Build.MODEL}\n")

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
