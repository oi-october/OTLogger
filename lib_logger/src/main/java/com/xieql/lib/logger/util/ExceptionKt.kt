package com.xieql.lib.logger.util

import android.util.Log

/**
 * 异常管理类
 */

/** Throws an [IllegalStateException] with a message that includes [value]. */
internal fun unexpectedValue(value: Any?): Nothing = throw IllegalStateException("Unexpected value: $value.")

/** Throws an [IllegalArgumentException] with the passed [argument] and [errorMsg]. */
internal fun illegalArg(argument: Any?, errorMsg: String? = null): Nothing =
    throw IllegalArgumentException(
        "Illegal argument: $argument${if (errorMsg == null) "." else ", $errorMsg."}"
    )

/** Throws an [UnsupportedOperationException] with the given message. */
internal fun unsupported(errorMsg: String? = null): Nothing = throw UnsupportedOperationException(errorMsg)

/** Get non null message from throwable. */
internal val Throwable.msg: String
    get() = message ?: javaClass.simpleName

/** Get stack trace message from throwable. */
internal val Throwable.stackTraceMsg: String
    get() = Log.getStackTraceString(this)
