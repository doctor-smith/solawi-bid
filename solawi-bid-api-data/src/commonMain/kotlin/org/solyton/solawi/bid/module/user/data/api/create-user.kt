package org.solyton.solawi.bid.module.user.data.api

import kotlinx.serialization.Serializable
import org.solyton.solawi.bid.module.values.isValidEmail

@Serializable
data class CreateUser(
    val username: String,
    val password: String?,
    val status: UserStatus = UserStatus.ACTIVE
) {
    init {
        val isValid = isValidEmail(username) && (
                (password == null && status in listOf(UserStatus.PENDING, UserStatus.INVITED)) ||
                (password != null && status in listOf(UserStatus.ACTIVE, UserStatus.REGISTERED, UserStatus.DISABLED))
        )
        require(isValid) { "Invalid user data: $this" }
    }
}
