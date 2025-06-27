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
data object GetUsers
