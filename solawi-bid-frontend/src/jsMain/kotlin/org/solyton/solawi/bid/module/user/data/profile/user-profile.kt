package org.solyton.solawi.bid.module.user.data.profile

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.user.data.address.Address

@Lensify data class UserProfile(
    @ReadOnly val userProfileId: String = "",
    @ReadWrite val firstname: String = "",
    @ReadWrite val lastname: String = "",
    @ReadWrite val title: String? = null,
    @ReadWrite val phoneNumber: String? = null,
    @ReadWrite val addresses: List<Address> = listOf()
) {
    init {
        require(isValidPhoneNumber(phoneNumber ?: "", false)) { "Invalid phone number" }
    }

    companion object {
        fun default(): UserProfile = UserProfile(
            "",
            "",
            "",
            null,
            null,
            listOf()
        )}
}

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
