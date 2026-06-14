package org.solyton.solawi.bid.module.user.data.api

import kotlinx.serialization.Serializable
import org.evoleq.ktorx.client.EmptyParams

typealias ApiUser = User
typealias ApiUsers = Users

@Serializable
data class User(
    val id: String,
    val username: String,
    val status: UserStatus
)

@Serializable
data class Users(
    val all: List<User>
)



@Serializable
data object GetUsers : EmptyParams()
