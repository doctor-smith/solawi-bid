package org.solyton.solawi.bid.module.user.data.profile


private val NON_DIGIT_REGEX = Regex("[^0-9+]")

@Suppress("ReturnCount")
fun isValidPhoneNumber(input: String, strict: Boolean): Boolean {
    val cleaned = input
        .trim()
        .replace(NON_DIGIT_REGEX, "")

    // Only allow '+' at the beginning
    if (cleaned.count { it == '+' } > 1 || (cleaned.contains('+') && !cleaned.startsWith('+'))) {
        return false
    }

    if(!strict) return true

    val digits = cleaned.removePrefix("+")
    return digits.length in 10..15
}
