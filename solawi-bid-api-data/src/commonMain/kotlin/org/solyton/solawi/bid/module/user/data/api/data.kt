package org.solyton.solawi.bid.module.user.data.api

import kotlinx.serialization.Serializable

@Serializable
data class CreateUser(
    val username: String,
    val password: String
)

@Serializable
data class User(
    val id: String,
    val username: String,
)

@Serializable
data class Users(
    val all: List<User>
)

@Serializable
data object GetUsers