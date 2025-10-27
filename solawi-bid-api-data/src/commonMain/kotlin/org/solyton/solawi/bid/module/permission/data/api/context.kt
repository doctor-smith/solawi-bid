package org.solyton.solawi.bid.module.permission.data.api

import kotlinx.serialization.Serializable

typealias ApiContext = Context
typealias ApiContexts = Contexts
typealias ApiRole = Role
typealias ApiRight = Right


interface ContextId {
    val contextId: String
}

@Serializable
data class Contexts(
    val list: List<Context>
)

/**
 * Maps uuids to a list of contexts
 * uuids are given as [String]s
 */
@Serializable
data class UserToContextsMap(
    val map: Map<String, List<Context>>
)

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
data class ReadRightRoleContexts(
    val contextIds: List<String>
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

@Serializable
data class ReadParentChildRelationsOfContexts(
    val contextIds: List<String>
)

@Serializable
data class ParentChildRelationsOfContexts(
    val list: List<ParentChildRelationsOfContext>
)

@Serializable
data class ParentChildRelationsOfContext(
    val contextId: String,
    val name: String,
    val rootId: String?,
    val children: List<String>
)
