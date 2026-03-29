package org.evoleq.compose.date

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.internal.JSJoda.LocalTime
import org.evoleq.language.Locale

fun LocalDate.format(locale: Locale): String = when(locale) {
    Locale.De -> "${dayOfMonth.toString().padStart(2, '0')}." +
            "${monthNumber.toString().padStart(2, '0')}.$year"

    Locale.En -> "$year/" +
            "${monthNumber.toString().padStart(2, '0')}/" +
            dayOfMonth.toString().padStart(2, '0')

    Locale.Iso -> toString()
}

fun LocalDateTime.format(locale: Locale): String = when(locale) {
    Locale.De -> "${dayOfMonth.toString().padStart(2, '0')}." +
            "${monthNumber.toString().padStart(2, '0')}.$year"

    Locale.En -> "$year/" +
            "${monthNumber.toString().padStart(2, '0')}/" +
            "${dayOfMonth.toString().padStart(2, '0')}"

    Locale.Iso -> "$year-" +
            "${monthNumber.toString().padStart(2, '0')}-" +
            "${dayOfMonth.toString().padStart(2, '0')}"
}

fun LocalTime.format(locale: Locale): String = when(locale) {
    Locale.De -> "${hour()}:${minute()}"
    Locale.En -> "${hour()}:${minute()}"
    Locale.Iso -> "${hour()}:${minute()}"
}
