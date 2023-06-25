package com.xieql.lib.logger.utils

import android.os.Build
import android.os.Environment
import android.os.Process
import androidx.annotation.VisibleForTesting
import androidx.core.content.pm.PackageInfoCompat
import com.xieql.lib.logger.core.appCtx
import java.io.File
import java.text.DecimalFormat
import java.util.*


/** The manufacturer of the product/hardware. */
val manufacturer: String get() = Build.MANUFACTURER

/** The end-user-visible name for the end product. */
val model: String get() = Build.MODEL

/** The name of the overall product. */
val product: String get() = Build.PRODUCT

/** The consumer-visible brand with which the product/hardware will be associated, if any. */
val brand: String get() = Build.BRAND

/** The SDK version of the software currently running on this hardware device. */
val osVerCode: Int get() = Build.VERSION.SDK_INT

/** The user-visible version string.  E.g., "1.0" or "3.4b5". */
val osVerName: String get() = Build.VERSION.RELEASE

/** A build ID string meant for displaying to the user. */
val osVerDisplayName: String get() = Build.DISPLAY

/** Application version code. */
fun appVerCode(pkgName: String? = null): Int {
    val info = appCtx.packageManager.getPackageInfo(pkgName ?: appCtx.packageName, 0)
    return PackageInfoCompat.getLongVersionCode(info).toInt()
}

/** Application version name. */
fun appVerName(pkgName: String? = null): String {
    val info = appCtx.packageManager.getPackageInfo(pkgName ?: appCtx.packageName, 0)
    return info.versionName ?: ""
}

/** System language. */
val language: String get() = Locale.getDefault().language

/** System language and country info. */
val languageAndCountry: String
    get() {
        val locale = Locale.getDefault()
        return "${locale.language}-${locale.country}"
    }

/** Process name. */
val processName: String by lazy {
    File("/proc/${Process.myPid()}/cmdline").readLines().first().trim { it <= ' ' }
}

/** Memory total size, unit is MB. */
val memTotalSize: Int get() = readMemSize(MemInfo.MEM_TOTAL)

/** Memory available size, unit is MB. */
val memAvailableSize: Int
    get() {
        return readMemSize(MemInfo.MEM_FREE) + readMemSize(MemInfo.BUFFERS) +
                readMemSize(MemInfo.CACHED)
    }

/** Available memory percent, its format seems like '00.00'. */
val memAvailablePercent: String
    get() {
        val percent = (memAvailableSize.toDouble() / memTotalSize.toDouble()) * 100
        return DecimalFormat("00.00").format(percent)
    }

/** Globally unique identifier. */
val guid: String by lazy {
    installationContent()
}

/** Check external storage writable or not. */
val externalStorageWritable: Boolean
    get() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

/** Check external storage readable or not. */
val externalStorageReadable: Boolean
    get() {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
    }

private fun readMemSize(memInfo: MemInfo): Int {
    File("/proc/meminfo").readLines().forEach {
        if (it.contains(memInfo.value)) {
            // line = "MemTotal:        1535124 kB"
            // list = ["MemTotal", "        1535124 kB"]
            val list = it.split(":".toRegex(), 2)
            // list[1] = [        1535124 kB]
            // subList = ["1535124", "KB"]
            val subList = list[1].trim().split(" ".toRegex())
            // subList[0] = "1535124"
            return subList[0].toInt() / 1024
        }
    }
    return 0
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun installationContent(): String {
    val file = File(appCtx.filesDir, "INSTALLATION")
    if (!file.exists()) {
        file.create()
        file.writeText(UUID.randomUUID().toString())
    }
    return file.readText()
}

enum class MemInfo(val value: String) {
    MEM_TOTAL("MemTotal"),
    MEM_FREE("MemFree"),
    BUFFERS("Buffers"),
    CACHED("Cached")
}
