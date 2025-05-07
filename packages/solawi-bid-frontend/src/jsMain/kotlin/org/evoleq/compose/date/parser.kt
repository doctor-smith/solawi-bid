package org.evoleq.compose.date

import kotlinx.datetime.LocalDate
import org.evoleq.language.Locale

fun String.parse(locale: Locale) : LocalDate = when(locale) {
    Locale.Iso  -> with(destruct("-")) {
        LocalDate(this[0], this[1], this[2])
    }
    Locale.De -> with(destruct(".")) {
        LocalDate(this[2], this[1], this[0])
    }
    Locale.En -> when{
        contains(".") -> with(destruct(".")) {
            LocalDate(this[0], this[1], this[2])
        }
        contains("/") -> with(destruct("/")) {
            LocalDate(this[0], this[1], this[2])
        }
        else -> throw DateTimeException.NoSuchFormat(this@parse)
    }
}

private fun String.destruct(del: String): List<Int> = split(del).map{it.toInt()}