package org.evoleq.compose.date

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.evoleq.language.Locale

fun today(): LocalDate = with(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) {
    LocalDate(year, monthNumber, dayOfMonth)
}

fun LocalDate.format(locale: Locale): String = when(locale) {
    Locale.De -> "$dayOfMonth.$monthNumber.$year"
    Locale.En -> "$year/$monthNumber/$dayOfMonth"
    Locale.Iso -> "$year-$monthNumber-$dayOfMonth"
}

