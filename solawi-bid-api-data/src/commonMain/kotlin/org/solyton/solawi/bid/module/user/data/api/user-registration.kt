package org.solyton.solawi.bid.module.user.data.api

import kotlinx.serialization.Serializable

// Dummy data
@Serializable
data class RegisterUser(
    val username: String
)

// Dummy data
@Serializable
data class UserRegistered(
    val username: String
)
