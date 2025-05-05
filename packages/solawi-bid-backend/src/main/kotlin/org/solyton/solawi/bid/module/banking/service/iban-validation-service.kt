package org.solyton.solawi.bid.module.banking.service

fun isValidIban(iban: String): Boolean {
    // Remove all spaces and make the IBAN uppercase
    val normalizedIban = iban.replace(" ", "").uppercase()

    // Check if the IBAN length is valid for the country
    if (!isValidIbanLength(normalizedIban)) {
        return false
    }

    // Rearrange the IBAN (move the first 4 characters to the end)
    val rearrangedIban = normalizedIban.substring(4) + normalizedIban.substring(0, 4)

    // Replace letters with numbers (A = 10, B = 11, ..., Z = 35)
    val numericIban = rearrangedIban.map {
        if (it.isDigit()) it.toString()
        else (it.code - 'A'.code + 10).toString()
    }.joinToString("")

    // Perform the modulo-97 operation
    return numericIban.toBigInteger() % 97.toBigInteger() == 1.toBigInteger()
}

private fun isValidIbanLength(iban: String): Boolean {
    val countryLengths = mapOf(
        "AL" to 28, "AD" to 24, "AT" to 20, "AZ" to 28,
        "BH" to 22, "BE" to 16, "BA" to 20, "BR" to 29,
        "BG" to 22, "CR" to 22, "HR" to 21, "CY" to 28,
        "CZ" to 24, "DK" to 18, "DO" to 28, "EG" to 29,
        "EE" to 20, "FO" to 18, "FI" to 18, "FR" to 27,
        "GE" to 22, "DE" to 22, "GI" to 23, "GR" to 27,
        "GL" to 18, "GT" to 28, "HU" to 28, "IS" to 26,
        "IE" to 22, "IL" to 23, "IT" to 27, "JO" to 30,
        "KZ" to 20, "XK" to 20, "KW" to 30, "LV" to 21,
        "LB" to 28, "LI" to 21, "LT" to 20, "LU" to 20,
        "MT" to 31, "MR" to 27, "MU" to 30, "MD" to 24,
        "MC" to 27, "ME" to 22, "NL" to 18, "MK" to 19,
        "NO" to 15, "PK" to 24, "PS" to 29, "PL" to 28,
        "PT" to 25, "QA" to 29, "RO" to 24, "SM" to 27,
        "SA" to 24, "RS" to 22, "SK" to 24, "SI" to 19,
        "ES" to 24, "SE" to 24, "CH" to 21, "TN" to 24,
        "TR" to 26, "UA" to 29, "AE" to 23, "GB" to 22,
        "VG" to 24
    )

    val countryCode = iban.take(2)
    val length = countryLengths[countryCode]
    return length != null && iban.length == length
}