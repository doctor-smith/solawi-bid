package org.solyton.solawi.bid.module.permission.repository

import org.evoleq.math.x
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.solyton.solawi.bid.module.permission.data.api.Context
import org.solyton.solawi.bid.module.permission.data.api.Right
import org.solyton.solawi.bid.module.permission.data.api.Role
import org.solyton.solawi.bid.module.permission.schema.*
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.RightEntity
import org.solyton.solawi.bid.module.permission.schema.RoleEntity
import java.util.*


/**
 * Retrieves a list of contexts with their associated roles and rights for a specific user.
 *
 * The method processes the user's role assignments across different contexts, fetches the associated
 * rights for those roles within the relevant contexts, and constructs a hierarchical model of
 * contexts, roles, and rights.
 *
 * But Note:
 * The returned structure is flat: no parent-child structure of contexts is established
 *
 * @param userId The unique identifier of the user for whom to retrieve role and right contexts.
 * @return A list of Context objects, each containing roles with their associated rights for the user.
 */
fun Transaction.getRoleRightContexts(userId: UUID): List<Context> {
    // 1. Get all role-context assignments of the user
    val userAssignments = UserRoleContext.selectAll()
        .where { UserRoleContext.userId eq userId }
        .map { it[UserRoleContext.roleId].value to it[UserRoleContext.contextId].value }

    if (userAssignments.isEmpty()) return emptyList()

    val uniqueContextIds = userAssignments.map { it.second }.distinct()
    val uniqueRoleIds = userAssignments.map { it.first }.distinct()

    // 2. Get all relevant relations between roles, rights and contexts
    // -> Structure of the returned triples: (RoleId, ContextId, RightId)
    val roleRightContextRelations = RoleRightContexts.selectAll()
        .where { (RoleRightContexts.contextId inList uniqueContextIds) and (RoleRightContexts.roleId inList uniqueRoleIds) }
        .map {
            Triple(it[RoleRightContexts.roleId].value, it[RoleRightContexts.contextId].value, it[RoleRightContexts.rightId].value)
        }
        .filter { (roleId, contextId, _) -> (roleId to contextId) in userAssignments }

    // 3. Prepare data models and Lookup-Maps
    val rightIds = roleRightContextRelations.map { it.third }.distinct()
    val rightModelsById = RightEntity.find { RightsTable.id inList rightIds }
        .associate { it.id.value to Right(it.id.value.toString(), it.name, it.description) }

    val roleEntitiesById = RoleEntity.find { RolesTable.id inList uniqueRoleIds }
        .associateBy { it.id.value }

    // Group rights w.r.t. roles and contexts: Map<(ContextId, RoleId), List<Right>>
    val rightsMap = roleRightContextRelations.groupBy(
        keySelector = { (roleId, contextId, _) -> contextId to roleId },
        valueTransform = { (_, _, rightId) -> rightModelsById[rightId] }
    ).mapValues { it.value.filterNotNull() }

    // 4. Build the final structure
    return ContextEntity.find { ContextsTable.id inList uniqueContextIds }.map { contextEntity ->
        val currentContextId = contextEntity.id.value

        // Find all context-specific roles of the user
        val rolesForThisContext = userAssignments
            .filter { (_, contextId) -> contextId == currentContextId }
            .mapNotNull { (roleId, _) -> roleEntitiesById[roleId] }
            .map { roleEntity ->
                val currentRoleId = roleEntity.id.value
                Role(
                    id = currentRoleId.toString(),
                    name = roleEntity.name,
                    description = roleEntity.description,
                    rights = rightsMap[currentContextId to currentRoleId].orEmpty()
                )
            }

        Context(
            id = currentContextId.toString(),
            name = contextEntity.name,
            roles = rolesForThisContext
        )
    }
}

/**
 * Returns a complete map of cumulated user-right-role-contexts [Context]
 * Note:
 * - The structure is flat: no parent-child structure of contexts is created
 */
fun Transaction.getRoleRightContexts(userIds: List<UUID>): Map<UUID, List<Context>> {
    // 1) Load all (user, role, context) assignments in one go
    val assignments: List<Triple<UUID, UUID, UUID>> = UserRoleContext
        .selectAll()
        .where { UserRoleContext.userId inList userIds }
        .map {
            Triple(
                it[UserRoleContext.userId],
                it[UserRoleContext.roleId].value,
                it[UserRoleContext.contextId].value
            )
        }

    if (assignments.isEmpty()) return userIds.associateWith { emptyList() }

    val contextIds = assignments.map { it.third }.distinct()
    val roleIds = assignments.map { it.second }.distinct()

    // 2) Load role entities once (for lookup)
    val roleById: Map<UUID, RoleEntity> = RoleEntity
        .find { RolesTable.id inList roleIds }
        .associateBy { it.id.value }

    // 3) Load all relevant (role, context, right) relations in one go
    //    and filter based on actually existing (role, context) pairs from assignments.
    val roleContextPairs: Set<Pair<UUID, UUID>> = assignments
        .map { it.second to it.third }
        .toSet()

    val rrcTriples: List<Triple<UUID, UUID, UUID>> = RoleRightContexts
        .selectAll()
        .where {
            (RoleRightContexts.contextId inList contextIds) and
                (RoleRightContexts.roleId inList roleIds)
        }
        .map {
            Triple(
                it[RoleRightContexts.roleId].value,
                it[RoleRightContexts.contextId].value,
                it[RoleRightContexts.rightId].value
            )
        }
        .filter { (roleId, contextId, _) -> (roleId to contextId) in roleContextPairs }

    // 4) Load rights once (for lookup) and group as (context, role) -> rights
    val rightIds = rrcTriples.map { it.third }.distinct()

    val rightModelById: Map<UUID, Right> = RightEntity
        .find { RightsTable.id inList rightIds }
        .associate { r ->
            r.id.value to Right(
                id = r.id.value.toString(),
                name = r.name,
                description = r.description
            )
        }

    val rightsByContextRole: Map<Pair<UUID, UUID>, List<Right>> = rrcTriples
        .groupBy(
            keySelector = { (roleId, contextId, _) -> contextId to roleId },
            valueTransform = { (_, _, rightId) -> rightModelById[rightId] }
        )
        .mapValues { (_, list) -> list.filterNotNull().distinctBy { it.id } }

    // 5) Load contexts once (for lookup, e.g. name, etc.)
    val contextById: Map<UUID, ContextEntity> = ContextEntity
        .find { ContextsTable.id inList contextIds }
        .associateBy { it.id.value }

    // 6) Result: For each user, only their contexts, and for each context, only their roles (not "all in the group")
    //    Additionally, without N+1 rights queries, because rightsByContextRole is already prepared.
    val rolesByUserContext: Map<Pair<UUID, UUID>, List<UUID>> = assignments
        .groupBy(
            keySelector = { (userId, _, contextId) -> userId to contextId },
            valueTransform = { (_, roleId, _) -> roleId }
        )
        .mapValues { (_, list) -> list.distinct() }

    return userIds.associateWith { userId ->
        val contextIdsOfUser: List<UUID> = rolesByUserContext.keys
            .asSequence()
            .filter { (u, _) -> u == userId }
            .map { (_, c) -> c }
            .distinct()
            .toList()

        contextIdsOfUser.mapNotNull { contextId ->
            val contextEntity = contextById[contextId] ?: return@mapNotNull null

            val roleIdsInThisContext: List<UUID> =
                rolesByUserContext[userId to contextId].orEmpty()

            val roleModels: List<Role> = roleIdsInThisContext.mapNotNull { roleId ->
                val roleEntity = roleById[roleId] ?: return@mapNotNull null
                Role(
                    id = roleId.toString(),
                    name = roleEntity.name,
                    description = roleEntity.description,
                    rights = rightsByContextRole[contextId to roleId].orEmpty()
                )
            }

            Context(
                id = contextId.toString(),
                name = contextEntity.name,
                roles = roleModels
            )
        }
    }
}


fun Transaction.getRolesAndRights(userIds: List<UUID>, contextId: UUID): Map<String, List<Role>> {
    val userRoles = UserRoleContext.selectAll().where {
        UserRoleContext.contextId eq contextId and
                (UserRoleContext.userId inList  userIds)
    }.map { Pair( it[UserRoleContext.userId], it[UserRoleContext.roleId].value)  }.toList()


    val roleIds = userRoles.map { it.second }.distinct()

    val roleRightContexts = RoleRightContexts.selectAll().where {
        (RoleRightContexts.contextId eq contextId) and
                (RoleRightContexts.roleId inList roleIds)
    }.map {
        Triple(it[RoleRightContexts.roleId].value, it[RoleRightContexts.contextId].value,  it[RoleRightContexts.rightId].value)
    }

    val rightIds = roleRightContexts.map { it.third }.distinct()

    val rights = RightEntity.find{
        RightsTable.id inList rightIds
    }.toList()

    val roles = RoleEntity.find {
        RolesTable.id inList roleIds
    }

    val userRolesMap = userRoles.associate { pair -> Pair(
        pair.first.toString(),
        roles.filter { role -> userRoles.contains(Pair(pair.first, role.id.value)) }
            .map { role ->
                Role(
                    role.id.value.toString(),
                    role.name,
                    role.description,
                    rights.filter { right ->
                        roleRightContexts.any { triple ->
                            triple.first == role.id.value &&
                                    triple.second == contextId &&
                                    triple.third == right.id.value
                        }
                    }.map { right ->
                        Right(
                            right.id.value.toString(),
                            right.name,
                            right.description
                        )
                    }
                )
            }
    )
    }
    return userRolesMap
}

/**
 * Get all users in a context together with their rights and roles
 */
fun Transaction.getUserRolesAndRights(contextId: UUID): Map<String, List<Role>> {
    val userRoles = UserRoleContext.selectAll().where {
        UserRoleContext.contextId eq contextId
    }.map { Pair( it[UserRoleContext.userId], it[UserRoleContext.roleId].value)  }.toList()


    val roleIds = userRoles.map { it.second }.distinct()

    val roleRightContexts = RoleRightContexts.selectAll().where {
        (RoleRightContexts.contextId eq contextId) and
                (RoleRightContexts.roleId inList roleIds)
    }.map {
        Triple(it[RoleRightContexts.roleId].value, it[RoleRightContexts.contextId].value,  it[RoleRightContexts.rightId].value)
    }

    val rightIds = roleRightContexts.map { it.third }.distinct()

    val rights = RightEntity.find{
        RightsTable.id inList rightIds
    }.toList()

    val roles = RoleEntity.find {
        RolesTable.id inList roleIds
    }

    val userRolesMap = userRoles.associate { pair -> Pair(
        pair.first.toString(),
        roles.filter { role -> userRoles.contains(Pair(pair.first, role.id.value)) }
            .map { role ->
                Role(
                    role.id.value.toString(),
                    role.name,
                    role.description,
                    rights.filter { right ->
                        roleRightContexts.any { triple ->
                            triple.first == role.id.value &&
                                    triple.second == contextId &&
                                    triple.third == right.id.value
                        }
                    }.map { right ->
                        Right(
                            right.id.value.toString(),
                            right.name,
                            right.description
                        )
                    }
                )
            }
    )
    }
    return userRolesMap
}

/**
 * Give a list of context ids return the associated contexts together with roles and rights
 */
fun Transaction.getRightRoleContexts(contextsIds: List<UUID>): List<Context> {
    val contexts = ContextEntity
        .find { ContextsTable.id inList contextsIds }
        .map { contextEntity -> Context(
            id = contextEntity.id.value.toString(),
            name = contextEntity.name,
            roles = contextEntity.roles
                .distinctBy { role -> role.id.value }
                .map { role -> Role(
                    id = role.id.value.toString(),
                    name = role.name,
                    description = role.description,
                    rights = role.rightsInContext(contextEntity.id.value)
                        .distinctBy { right -> right.id.value }
                        .map{right -> Right(
                            id = right.id.value.toString(),
                            name = right.name,
                            description = right.description
                        )
                    }
                )
                }
        )
        }
    return contexts
}

fun Transaction.getUserRightContexts(userId: UUID, rights: List<String>): List<UUID> {
    val rightIds = RightEntity.find { RightsTable.name inList rights }.map { it.id.value }.distinct()
    val userRoleContexts = UserRoleContext.selectAll().where{
        UserRoleContext.userId eq userId
    }.map {
        it[UserRoleContext.roleId] x it[UserRoleContext.contextId]
    }
    val relevantUserRoleIds = RoleEntity.find { RolesTable.id inList userRoleContexts.map { it.first } }.distinctBy {
            role -> role.id.value
    }.filter {
            role -> role.rights.toList().map { it.id.value }.any { rightIds.contains(it) }
    }.map {
            role -> role.id.value
    }
    return userRoleContexts.filter {
            pair -> relevantUserRoleIds.contains(pair.first.value)
    }.map { pair -> pair.second.value }
}

fun Transaction.getRolesByUserAndContext(userId: UUID, contextId: UUID): List<RoleEntity> {
    val roleIds = UserRoleContext.selectAll().where{
        UserRoleContext.userId eq userId and (UserRoleContext.contextId eq contextId)
    }.map { it[UserRoleContext.roleId].value }
    return RoleEntity.find { RolesTable.id inList roleIds }.toList()
}
