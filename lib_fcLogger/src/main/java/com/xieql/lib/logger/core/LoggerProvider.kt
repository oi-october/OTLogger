package com.xieql.lib.logger.core

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.xieql.lib.logger.core.LoggerMetaData.Companion.CONTENT_ITEM_TYPE
import com.xieql.lib.logger.core.LoggerMetaData.Companion.CONTENT_TYPE
import com.xieql.lib.logger.core.LoggerMetaData.Companion.DIR_NAME
import com.xieql.lib.logger.core.LoggerMetaData.Companion.INFO
import com.xieql.lib.logger.core.LoggerMetaData.Companion.INFOS
import com.xieql.lib.logger.core.LoggerMetaData.Companion.INFOS_CODE
import com.xieql.lib.logger.core.LoggerMetaData.Companion.INFO_CODE
import com.xieql.lib.logger.core.LoggerMetaData.Companion.NAME
import com.xieql.lib.logger.core.LoggerMetaData.Companion.PREFIX
import com.xieql.lib.logger.core.LoggerMetaData.Companion.SEGMENT
import com.xieql.lib.logger.core.LoggerMetaData.Companion.STORE_IN_SD_CARD
import com.xieql.lib.logger.core.LoggerMetaData.Companion.UPLOAD_TOKEN
import com.xieql.lib.logger.unexpectedValue
import com.xieql.lib.logger.unsupported

class LoggerProvider : ContentProvider() {
    private lateinit var uriMatcher: UriMatcher
    private lateinit var loggerMetaDatas: MutableMap<String, LoggerMetaData>

    override fun onCreate(): Boolean {
        loggerMetaDatas = mutableMapOf()
        val authority = "${appCtx.packageName}.provider.LoggerProvider"
        uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        uriMatcher.addURI(authority, INFO, INFO_CODE)
        uriMatcher.addURI(authority, INFOS, INFOS_CODE)
        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (values != null) {
            when (val code = uriMatcher.match(uri)) {
                INFO_CODE -> {
                    var loggerMetaData: LoggerMetaData
                    with(values) {
                        loggerMetaData = LoggerMetaData(
                            getAsString(NAME),
                            getAsString(DIR_NAME),
                            getAsString(PREFIX),
                            getAsInteger(SEGMENT),
                            getAsBoolean(STORE_IN_SD_CARD),
                            getAsString(UPLOAD_TOKEN)
                        )
                    }
                    loggerMetaDatas[loggerMetaData.name] = loggerMetaData
                }
                else -> unexpectedValue(code)
            }
        }
        return null
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {
        when (val code = uriMatcher.match(uri)) {
            INFOS_CODE -> {
                val cursor = MatrixCursor(
                    arrayOf(
                        NAME,
                        DIR_NAME,
                        PREFIX,
                        SEGMENT,
                        STORE_IN_SD_CARD,
                        UPLOAD_TOKEN
                    )
                )
                loggerMetaDatas.forEach {
                    with(it.value) {
                        cursor.addRow(
                            arrayListOf(
                                name,
                                dirName,
                                prefix,
                                segment,
                                storeInSdCard,
                                uploadToken
                            )
                        )
                    }
                }
                return cursor
            }
            else -> unexpectedValue(code)
        }
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int = unsupported()

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int =
        unsupported()

    override fun getType(uri: Uri): String {
        return when (val code = uriMatcher.match(uri)) {
            INFO_CODE -> CONTENT_ITEM_TYPE
            INFOS_CODE -> CONTENT_TYPE
            else -> unexpectedValue(code)
        }
    }
}

class LoggerMetaData(
    val name: String,
    val dirName: String,
    val prefix: String,
    val segment: Int,
    val storeInSdCard: Boolean,
    val uploadToken: String
) {
    companion object {
        const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.fcbox.provider.logger"
        const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.fcbox.provider.logger"

        const val INFO_CODE = 1
        const val INFOS_CODE = 2

        const val INFO = "info"
        const val INFOS = "infos"

        const val NAME = "name"
        const val UPLOAD_TOKEN = "upload_token"
        const val DIR_NAME = "dir_name"
        const val PREFIX = "prefix"
        const val SEGMENT = "segment"
        const val STORE_IN_SD_CARD = "store_in_sd_card"

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val BASE_URI: Uri =
            Uri.parse("content://${appCtx.packageName}.provider.LoggerProvider")
        val INFO_CONTENT_URI: Uri = Uri.withAppendedPath(BASE_URI, INFO)
        val INFOS_CONTENT_URI: Uri = Uri.withAppendedPath(BASE_URI, INFOS)
    }
}

fun Cursor.loggerName(): String = getString(getColumnIndex(NAME))
fun Cursor.loggerUploadToken(): String = getString(getColumnIndex(UPLOAD_TOKEN))
fun Cursor.loggerDirName(): String = getString(getColumnIndex(DIR_NAME))
fun Cursor.loggerPrefix(): String = getString(getColumnIndex(PREFIX))
fun Cursor.loggerSegment(): LogSegment = LogSegment.fromValue(getInt(getColumnIndex(SEGMENT)))
fun Cursor.loggerStoreInSdCard(): Boolean =
    getString(getColumnIndex(STORE_IN_SD_CARD))?.toBoolean() ?: false
