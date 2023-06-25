package com.xieql.lib.logger.core

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter


@Suppress("SpellCheckingInspection")
const val FMT_STANDARD = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
val standardFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(FMT_STANDARD)

/** Current ISO time. */
val now: OffsetDateTime get() = OffsetDateTime.now()

/** Current ISO time string, e.g. 2019-05-08T16:27:33.881+0800. */
val nowStr: String get() = now.format(standardFormatter)

/** Current date. */
val date: LocalDate get() = LocalDate.now()

/** Current date string. */
val dateStr: String get() = LocalDate.now().toString()

fun Long.toDateTime(): OffsetDateTime =
    OffsetDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

fun String.toDateTime(): OffsetDateTime = OffsetDateTime.parse(this, standardFormatter)

fun OffsetDateTime.toMillis(): Long = this.toInstant().toEpochMilli()

fun String.toMillis(): Long = this.toDateTime().toMillis()

fun Long.toDateTimeString(): String = this.toDateTime().format(standardFormatter)



