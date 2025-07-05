package org.solyton.solawi.bid.module.user.data.api

import kotlinx.serialization.Serializable

@Serializable
data class CreateUser(
    val username: String,
    val password: String
)
