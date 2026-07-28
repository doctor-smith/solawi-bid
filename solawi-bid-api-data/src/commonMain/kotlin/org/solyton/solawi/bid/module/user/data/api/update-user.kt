package org.solyton.solawi.bid.module.user.data.api

import kotlinx.serialization.Serializable
import org.solyton.solawi.bid.module.values.Password
import org.solyton.solawi.bid.module.values.Username

@Serializable
data class UpdateUser(
    val oldUsername: Username,
    val newUsername: Username,
    val oldPassword: Password,
    val newPassword: Password,
)
