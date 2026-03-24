package org.evoleq.kotlinx.date.data

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.evoleq.kotlinx.date.now

data class DateInterval(val start: LocalDate = now().date, val end: LocalDate = now().date) {
    init {
        require(start <= end)
    }
}

data class TimeInterval(val start: LocalTime, val end: LocalTime) {
    init {
        require(start <= end)
    }
}

data class DateTimeInterval(val start: LocalDateTime, val end: LocalDateTime) {
    init {
        require(start <= end)
    }
}

fun DateInterval.contains(date: LocalDate): Boolean = date in start..end

fun DateInterval.cutOff(interval: DateInterval): List<DateInterval> = when{
    interval.start > end -> listOf(this)
    interval.end < start -> listOf(this)
    contains(interval.start) && contains(interval.end) -> listOf(
        DateInterval(start = start, end = interval.start.minus(DatePeriod(days = 1))),
        DateInterval(start = interval.end.plus(DatePeriod(days = 1)), end = end)
    )
    contains(interval.start) -> listOf(DateInterval(start = start, end = interval.start.minus(DatePeriod(days = 1))))
    contains(interval.end) -> listOf(DateInterval(start = interval.end.plus(DatePeriod(days = 1)), end = end))
    else -> emptyList()
}
