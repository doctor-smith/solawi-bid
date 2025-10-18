package org.evoleq.kotlinx.date

import kotlinx.datetime.*

fun today(): LocalDate = with(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) {
    LocalDate(year, monthNumber, dayOfMonth)
}

fun todayWithTime(): LocalDateTime = now()

fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

fun LocalDate.toDateTime(): LocalDateTime = atTime(0,0,0,0)
