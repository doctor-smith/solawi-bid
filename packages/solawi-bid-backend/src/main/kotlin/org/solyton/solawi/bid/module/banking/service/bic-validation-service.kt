package org.solyton.solawi.bid.module.banking.service

fun isValidBic(bic: String): Boolean {
    // Regular expression for BIC validation
    val bicRegex = Regex("^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$")

    // Check if BIC matches the regex
    if (!bicRegex.matches(bic)) {
        return false
    }

    // Validate country code
    val countryCode = bic.substring(4, 6)
    return isValidCountryCode(countryCode)
}

private fun isValidCountryCode(countryCode: String): Boolean {
    // List of valid ISO 3166-1 alpha-2 country codes (shortened for brevity)
    val validCountryCodes = setOf(
        "DE", "GB", "FR", "CH", "DK", "US", "ES", "IT", "NL", "SE", "FI", "NO", "BE", "AU", "JP", "CA"
    )
    return countryCode in validCountryCodes
}