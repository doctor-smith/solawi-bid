package org.solyton.solawi.bid.module.user.data.address

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite

@Lensify
data class Address(
    @ReadOnly val addressId: String,
    @ReadWrite val recipientName: String,
    @ReadWrite val organizationName: String?,
    @ReadWrite val addressLine1: String,
    @ReadWrite val addressLine2: String,
    @ReadWrite val city: String,
    @ReadWrite val stateOrProvince: String,
    @ReadWrite val postalCode: String,
    @ReadWrite val countryCode: String
) {
    companion object {
        fun default(): Address = Address(
            "",
            "",
            null,
            "",
            "",
            "",
            "DE-BW",
            "",
            "DE"
        )}
}

fun Address.isValid(): Boolean = when {
    recipientName.isBlank() -> false
    addressLine1.isBlank() -> false
    city.isBlank() -> false
    stateOrProvince.isBlank() -> false
    postalCode.isBlank() -> false
    countryCode.isBlank() -> false
    !isValidCountryCode(countryCode) -> false
    !isValidPostalCode(postalCode, countryCode) -> false
    else -> true
}

fun isValidCountryCode(code: String): Boolean = when (code.uppercase()) {
    "AF", "AX", "AL", "DZ", "AS", "AD", "AO", "AI", "AQ", "AG", "AR", "AM", "AW", "AU",
    "AT", "AZ", "BS", "BH", "BD", "BB", "BY", "BE", "BZ", "BJ", "BM", "BT", "BO", "BQ",
    "BA", "BW", "BV", "BR", "IO", "BN", "BG", "BF", "BI", "CV", "KH", "CM", "CA", "KY",
    "CF", "TD", "CL", "CN", "CX", "CC", "CO", "KM", "CG", "CD", "CK", "CR", "HR", "CU",
    "CW", "CY", "CZ", "DK", "DJ", "DM", "DO", "EC", "EG", "SV", "GQ", "ER", "EE", "SZ",
    "ET", "FK", "FO", "FJ", "FI", "FR", "GF", "PF", "TF", "GA", "GM", "GE", "DE", "GH",
    "GI", "GR", "GL", "GD", "GP", "GU", "GT", "GG", "GN", "GW", "GY", "HT", "HM", "VA",
    "HN", "HK", "HU", "IS", "IN", "ID", "IR", "IQ", "IE", "IM", "IL", "IT", "JM", "JP",
    "JE", "JO", "KZ", "KE", "KI", "KP", "KR", "KW", "KG", "LA", "LV", "LB", "LS", "LR",
    "LY", "LI", "LT", "LU", "MO", "MG", "MW", "MY", "MV", "ML", "MT", "MH", "MQ", "MR",
    "MU", "YT", "MX", "FM", "MD", "MC", "MN", "ME", "MS", "MA", "MZ", "MM", "NA", "NR",
    "NP", "NL", "NC", "NZ", "NI", "NE", "NG", "NU", "NF", "MK", "MP", "NO", "OM", "PK",
    "PW", "PS", "PA", "PG", "PY", "PE", "PH", "PN", "PL", "PT", "PR", "QA", "RO", "RU",
    "RW", "RE", "BL", "SH", "KN", "LC", "MF", "PM", "VC", "WS", "SM", "ST", "SA", "SN",
    "RS", "SC", "SL", "SG", "SX", "SK", "SI", "SB", "SO", "ZA", "GS", "SS", "ES", "LK",
    "SD", "SR", "SJ", "SE", "CH", "SY", "TW", "TJ", "TZ", "TH", "TL", "TG", "TK", "TO",
    "TT", "TN", "TR", "TM", "TC", "TV", "UG", "UA", "AE", "GB", "US", "UM", "UY", "UZ",
    "VU", "VE", "VN", "VG", "VI", "WF", "EH", "YE", "ZM", "ZW" -> true

    else -> false
}

/**
 * Validates the given postal code based on the specified country code.
 *
 * The validation uses predefined formats for various countries. If the country code
 * does not match any recognized code, the method checks if the postal code is not blank.
 *
 * @param postalCode the postal code to validate
 * @param countryCode the country code used to determine the postal code format
 * @return true if the postal code matches the format for the given country code, or if the postal code
 *         is not blank for unrecognized country codes; false otherwise
 *
 * The following country codes are explicitly validated:
 * - US (United States)
 * - CA (Canada)
 * - DE (Germany)
 * - FR (France)
 * - GB (United Kingdom)
 * - ES (Spain)
 * - IT (Italy)
 * - NL (Netherlands)
 * - AT (Austria)
 * - CH (Switzerland)
 * - BE (Belgium)
 * - PL (Poland)
 * - PT (Portugal)
 * - SE (Sweden)
 * For any unrecognized country codes, the postal code must simply not be blank.
 */
fun isValidPostalCode(postalCode: String, countryCode: String): Boolean = when (countryCode.uppercase()) {
    "US" -> postalCode.matches(Regex("^[0-9]{5}(?:-[0-9]{4})?$"))
    "CA" -> postalCode.matches(Regex("^[A-Z][0-9][A-Z] [0-9][A-Z][0-9]$"))
    "DE" -> postalCode.matches(Regex("^[0-9]{5}$"))
    "FR" -> postalCode.matches(Regex("^[0-9]{5}$"))
    "GB" -> postalCode.matches(Regex("^(GIR\\s?0AA|[A-Z]{1,2}[0-9][0-9A-Z]?\\s?[0-9][A-Z]{2})$"))
    "ES" -> postalCode.matches(Regex("^[0-9]{5}$"))
    "IT" -> postalCode.matches(Regex("^[0-9]{5}$"))
    "NL" -> postalCode.matches(Regex("^[0-9]{4}\\s?[A-Z]{2}$"))
    "AT" -> postalCode.matches(Regex("^[0-9]{4}$"))
    "CH" -> postalCode.matches(Regex("^[0-9]{4}$"))
    "BE" -> postalCode.matches(Regex("^[0-9]{4}$"))
    "PL" -> postalCode.matches(Regex("^[0-9]{2}-[0-9]{3}$"))
    "PT" -> postalCode.matches(Regex("^[0-9]{4}-[0-9]{3}$"))
    "SE" -> postalCode.matches(Regex("^[0-9]{3}\\s?[0-9]{2}$"))
    else -> postalCode.isNotBlank()
}
