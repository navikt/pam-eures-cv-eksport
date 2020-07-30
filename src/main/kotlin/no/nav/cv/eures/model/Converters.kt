package no.nav.cv.eures.model

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Converters {
    @JvmStatic
    fun localdatetimeToTimestamp(ldt: LocalDateTime): Long =
            ldt.atZone(ZoneId.of("Europe/Oslo")).toInstant().toEpochMilli()

    @JvmStatic
    fun isoDatetimeToTimestamp(isoDatetime : String): Long =
            localdatetimeToTimestamp(
                    LocalDateTime.parse(isoDatetime, DateTimeFormatter.ISO_LOCAL_DATE_TIME))

    @JvmStatic
    fun timestampToLocalDateTime(ts: Long): LocalDateTime =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.of("Europe/Oslo"))

}