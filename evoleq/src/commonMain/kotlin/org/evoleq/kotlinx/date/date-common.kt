package org.evoleq.kotlinx.date

import kotlinx.datetime.*

fun today(): LocalDate = with(Clock.System.now().toLocalDateTime(TimeZone.UTC)) {
    LocalDate(year, monthNumber, dayOfMonth)
}

fun todayWithTime(): LocalDateTime = now()

fun todayWithClientTime(): LocalDateTime = hereAndNow()

fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)

fun hereAndNow(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

fun LocalDate.toDateTime(): LocalDateTime = atTime(0,0,0,0)

/**
 * Assumes that given local time is expressed w.r.t. UTC
 */
fun LocalDateTime.toClientTime(clientZone: TimeZone): LocalDateTime = toInstant(TimeZone.UTC).toLocalDateTime(clientZone);

/**
 * Assumes that given local time is expressed w.r.t. the clients time zone
 */
fun LocalDateTime.toServerTime(clientZone: TimeZone): LocalDateTime = toInstant(clientZone).toLocalDateTime(TimeZone.UTC)
