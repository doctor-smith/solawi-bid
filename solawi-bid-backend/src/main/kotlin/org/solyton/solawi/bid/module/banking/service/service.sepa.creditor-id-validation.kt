package org.solyton.solawi.bid.module.banking.service

fun validateCreditorId(ci: String): Boolean {
    if (!ci.matches(Regex("[A-Z]{2}\\d{2}[A-Z0-9]{11,30}"))) return false

    val countryCode = ci.substring(0, 2)
    val checkDigits = ci.substring(2, 4)
    val rest = ci.substring(4)

    // Schritt 1: Buchstaben in Zahlen umwandeln
    fun charToInt(c: Char) = if (c.isDigit()) c.toString() else (c - 'A' + 10).toString()
    val numeric = (rest + countryCode + checkDigits).map { charToInt(it) }.joinToString("")

    // Schritt 2: Modulo 97
    val mod97 = numeric.chunked(9).fold(0L) { acc, chunk ->
        ((acc.toString() + chunk).toLong()) % 97
    }

    return mod97 == 1L
}
