package org.solyton.solawi.bid.module.user.data.api

import kotlinx.serialization.Serializable


@Serializable
data class ChangePassword(
    val username: String,
    val password: String
)
