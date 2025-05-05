package org.solyton.solawi.bid.module.user.data.api

import kotlinx.serialization.Serializable

typealias ApiUser = User
typealias ApiUsers = Users

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
data class CreateUser(
    val username: String,
    val password: String
)

@Serializable
data class ChangePassword(
    val username: String,
    val password: String
)

@Serializable
data object GetUsers