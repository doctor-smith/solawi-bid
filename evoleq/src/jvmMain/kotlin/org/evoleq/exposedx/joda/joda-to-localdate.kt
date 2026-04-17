package org.evoleq.exposedx.joda

import kotlinx.datetime.LocalDateTime as KotlinXLocalDateTime
import kotlinx.datetime.LocalDate as KotlinXLocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import org.evoleq.kotlinx.date.toDateTime
import org.joda.time.LocalDate as JodaLocalDate
import org.joda.time.LocalDateTime as JodaLocalDateTime

import org.joda.time.DateTime as JodaDateTime
import org.joda.time.DateTimeZone

fun KotlinXLocalDateTime.toJoda(
    zone: TimeZone = TimeZone.UTC
): JodaDateTime {
    // 1. Convert to kotlinx Instant (requires a zone)
    val instant = toInstant(zone)

    // 2. Convert kotlinx Instant -> java.time.Instant
    val javaInstant = instant.toJavaInstant()

    // 3. Convert java.time.Instant -> org.joda.time.DateTime
    return JodaDateTime(javaInstant.toEpochMilli(), DateTimeZone.forID(zone.id))
}

fun JodaDateTime.toKotlinxWithZone(): KotlinXLocalDateTime {
    val instant = toInstant().toDate().toInstant().toKotlinInstant()
    val zone = TimeZone.of(zone.id)
    return instant.toLocalDateTime(zone)
}

fun KotlinXLocalDate.toJoda(
    zone: TimeZone = TimeZone.UTC
): JodaLocalDate {
    return toDateTime().toJoda(zone).toLocalDate()
}

