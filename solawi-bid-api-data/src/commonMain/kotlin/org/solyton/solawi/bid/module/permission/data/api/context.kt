package org.solyton.solawi.bid.module.permission.data.api

import kotlinx.serialization.Serializable

@Serializable
data class Context(
    val id: String,
    val name: String,
    val roles: List<Role>
)

@Serializable
data class Role(
    val id: String,
    val name: String,
    val description: String,
    val rights: List<Right>
)

@Serializable
data class Right(
    val id: String,
    val name: String,
    val description: String
)

@Serializable
data class ReadRightRoleContextsOfUser(
    // string representation of a UUID
    val userId: String
)

@Serializable
data class ReadRightRoleContextsOfUsers(
    // string representation of a UUID
    val userIds: List<String>
)

@Serializable
data class ReadRolesAndRightsOfUsers(
    val userIds: List<String>
)

