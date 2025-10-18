package org.evoleq.compose.date

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.internal.JSJoda.LocalTime
import org.evoleq.language.Locale

fun LocalDate.format(locale: Locale): String = when(locale) {
    Locale.De -> "$dayOfMonth.$monthNumber.$year"
    Locale.En -> "$year/$monthNumber/$dayOfMonth"
    Locale.Iso -> "$year-$monthNumber-$dayOfMonth"
}

fun LocalDateTime.format(locale: Locale): String = when(locale) {
    Locale.De -> "$dayOfMonth.$monthNumber.$year"
    Locale.En -> "$year/$monthNumber/$dayOfMonth"
    Locale.Iso -> "$year-$monthNumber-$dayOfMonth"
}

fun LocalTime.format(locale: Locale): String = when(locale) {
    Locale.De -> "${hour()}:${minute()}"
    Locale.En -> "${hour()}:${minute()}"
    Locale.Iso -> "${hour()}:${minute()}"
}
