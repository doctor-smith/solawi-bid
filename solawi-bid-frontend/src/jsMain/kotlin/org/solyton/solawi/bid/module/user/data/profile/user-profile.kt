package org.solyton.solawi.bid.module.user.data.profile

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.evoleq.uuid.NIL_UUID
import org.solyton.solawi.bid.module.user.data.address.Address
import org.solyton.solawi.bid.module.values.UserId

@Lensify data class UserProfile(
    @ReadOnly val userId: UserId = UserId(NIL_UUID),
    @ReadOnly val userProfileId: String = "",
    @ReadWrite val firstname: String = "",
    @ReadWrite val lastname: String = "",
    @ReadWrite val title: String? = null,
    @ReadWrite val phoneNumber: String? = null,
    @ReadWrite val phoneNumber1: String? = null,
    @ReadWrite val addresses: List<Address> = emptyList(),
) {
    init {
        require(isValidPhoneNumber(phoneNumber ?: "", false)) { "Invalid phone number: $phoneNumber" }
        require(isValidPhoneNumber(phoneNumber1 ?: "", false)) { "Invalid phone number: $phoneNumber1" }
    }

    companion object {
        val default: UserProfile = UserProfile(
            UserId(NIL_UUID),
            "",
            "",
            "",
            null,
            null,
            null,
            emptyList()
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
