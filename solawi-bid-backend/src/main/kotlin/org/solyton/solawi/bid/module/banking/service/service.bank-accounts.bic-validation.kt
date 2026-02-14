package org.solyton.solawi.bid.module.banking.service

import org.solyton.solawi.bid.module.banking.exception.BankAccountsException

private val BIC_REGEX = Regex("^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}(?:[A-Z0-9]{3})?$")

private val EU_COUNTRIES = setOf(
    "AT","BE","BG","CY","CZ","DE","DK","EE","ES","FI","FR","GR","HR","HU",
    "IE","IT","LT","LU","LV","MT","NL","PL","PT","RO","SE","SI","SK"
)

fun normalizeBic(raw: String): String =
    raw.trim().replace("\\s+".toRegex(), "").uppercase()

fun validateBic(raw: String, euOnly: Boolean = true): String {
    val bic = normalizeBic(raw)

    if (!BIC_REGEX.matches(bic)) {
        throw BankAccountsException.InvalidBic(bic)
    }

    val country = bic.substring(4, 6)
    if (!isValidCountryCode(country)) {
        throw BankAccountsException.InvalidBicCountryCode(bic)
    }

    if (euOnly && country !in EU_COUNTRIES) {
        throw BankAccountsException.BicNotInEU(bic)
    }

    return bic
}

fun isValidBic(raw: String, euOnly: Boolean = true): Boolean {
    return try {
        validateBic(raw, euOnly)
        true
    } catch (e: BankAccountsException.InvalidBic) {
        false
    }
}

private fun isValidCountryCode(countryCode: String): Boolean {
    // List of valid ISO 3166-1 alpha-2 country codes (shortened for brevity)
    val validCountryCodes = setOf(
        "DE", "GB", "FR", "CH", "DK", "US", "ES", "IT", "NL", "SE", "FI", "NO", "BE", "AU", "JP", "CA"
    )
    return countryCode in validCountryCodes
}
