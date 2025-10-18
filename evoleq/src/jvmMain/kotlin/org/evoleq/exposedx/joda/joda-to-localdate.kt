package org.evoleq.exposedx.joda

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

fun LocalDateTime.toJoda(
    zone: TimeZone = TimeZone.UTC
): DateTime {
    // 1. Convert to kotlinx Instant (requires a zone)
    val instant = toInstant(zone)

    // 2. Convert kotlinx Instant -> java.time.Instant
    val javaInstant = instant.toJavaInstant()

    // 3. Convert java.time.Instant -> org.joda.time.DateTime
    return DateTime(javaInstant.toEpochMilli(), DateTimeZone.forID(zone.id))
}

fun DateTime.toKotlinxWithZone(): LocalDateTime {
    val instant = toInstant().toDate().toInstant().toKotlinInstant()
    val zone = TimeZone.of(zone.id)
    return instant.toLocalDateTime(zone)
}
