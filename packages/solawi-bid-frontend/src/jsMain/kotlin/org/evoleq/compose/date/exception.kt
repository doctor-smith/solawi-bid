package org.evoleq.compose.date

sealed class DateTimeException(override val message: String): Exception(message) {
    data class NoSuchFormat(val date: String) : DateTimeException("No such format: $date")
}