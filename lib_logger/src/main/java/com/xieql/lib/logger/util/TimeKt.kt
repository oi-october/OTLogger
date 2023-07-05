package com.xieql.lib.logger.core

import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

object TimeKt{

    init {
        AndroidThreeTen.init(appCtx)
    }

    @Suppress("SpellCheckingInspection")
    const val FMT_STANDARD = "yyyy-MM-dd HH:mm:ss.SSS"
    val standardFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(FMT_STANDARD)

    /** Current ISO time. */
    val now: OffsetDateTime get() = OffsetDateTime.now()

    /** Current ISO time string, e.g. 2019-05-08 27:33.881 */
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

    //获取时间戳对应的小时
    fun getHour(time:Long):Int{
        val c = Calendar.getInstance()
        c.timeInMillis = time
        return c.get(Calendar.HOUR_OF_DAY)
    }

}




