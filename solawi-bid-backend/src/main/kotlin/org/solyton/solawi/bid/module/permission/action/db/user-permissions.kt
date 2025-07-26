package org.solyton.solawi.bid.module.permission.action.db

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.util.Contextual
import org.evoleq.util.DbAction
import org.evoleq.util.KlAction
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.solyton.solawi.bid.module.db.schema.*
import org.solyton.solawi.bid.module.permission.data.api.*
import org.solyton.solawi.bid.module.permission.data.api.Context
import org.solyton.solawi.bid.module.permission.data.api.Right
import org.solyton.solawi.bid.module.permission.data.api.Role
import java.util.UUID

@MathDsl
@Suppress("FunctionName")
val GetRoleRightContexts: KlAction<Result<Contextual<ReadRightRoleContextsOfUser>>, Result<List<Context>>> = KlAction {
    result ->  DbAction { database -> result bindSuspend { data: Contextual<ReadRightRoleContextsOfUser> ->
        resultTransaction(database){
            getRoleRightContexts(UUID.fromString(data.data.userId))
    } } x database }
}
@MathDsl
@Suppress("FunctionName")
val GetRoleRightContextsOfUsers: KlAction<Result<Contextual<ReadRightRoleContextsOfUsers>>, Result<Map<String,List<Context>>>> = KlAction {
    result ->  DbAction { database -> result bindSuspend { data: Contextual<ReadRightRoleContextsOfUsers> ->
        resultTransaction(database){
            getRoleRightContexts(data.data.userIds.map { UUID.fromString(it) }).mapKeys { it.value.toString() }
    } } x database }
}

fun Transaction.getRoleRightContexts(userId: UUID): List<Context> {
    val userRoleContexts = UserRoleContext.selectAll().where {
        UserRoleContext.userId eq userId
    }.map { Pair(it[UserRoleContext.roleId].value, it[UserRoleContext.contextId].value)  }.toList()

    val contextIds = userRoleContexts.map { it.second }.distinct()
    val roleIds = userRoleContexts.map { it.first }.distinct()

    val roleRightContexts = RoleRightContexts.selectAll().where {
        (RoleRightContexts.contextId inList contextIds) and
        (RoleRightContexts.roleId inList roleIds)
    }.map {
        Triple(it[RoleRightContexts.roleId].value, it[RoleRightContexts.contextId].value,  it[RoleRightContexts.rightId].value)
    }.filter { Pair(it.first, it.second) in userRoleContexts }

    val rightIds = roleRightContexts.map { it.third }.distinct()

    val rights = RightEntity.find{
        RightsTable.id inList rightIds
    }.toList()

    val roles: List<RoleEntity> = RoleEntity.find {
        RolesTable.id inList roleIds
    }.toList()

    val contexts =  ContextEntity
        .find { ContextsTable.id inList contextIds }
        .map { context -> Context(
            context.id.value.toString(),
            context.name,
            roles.filter{ role: RoleEntity ->
                userRoleContexts.any {
                    pair -> pair.second == context.id.value && pair.first == role.id.value
                }
            }.map { role ->
                Role(
                    role.id.value.toString(),
                    role.name,
                    role.description,
                    rights.filter { right -> roleRightContexts.any{ triple ->
                        triple.first == role.id.value &&
                        triple.second == context.id.value &&
                        triple.third == right.id.value
                    }  }.map { right ->
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
    return contexts
}

fun Transaction.getRoleRightContexts(userIds: List<UUID>): Map<UUID, List<Context>> {
    val userRoleContexts = UserRoleContext.selectAll().where {
        UserRoleContext.userId inList  userIds
    }.map { Triple(it[UserRoleContext.roleId].value, it[UserRoleContext.contextId].value, it[UserRoleContext.userId].value)  }.toList()

    val roleContexts = userRoleContexts.map { Pair(it.first, it.second) }
    val contextIds = userRoleContexts.map { it.second }.distinct()
    val roleIds = userRoleContexts.map { it.first }.distinct()

    val roleRightContexts = RoleRightContexts.selectAll().where {
        (RoleRightContexts.contextId inList contextIds) and
            (RoleRightContexts.roleId inList roleIds)
    }.map {
        Triple(it[RoleRightContexts.roleId].value, it[RoleRightContexts.contextId].value,  it[RoleRightContexts.rightId].value)
    }.filter { Pair(it.first, it.second) in roleContexts }

    val rightIds = roleRightContexts.map { it.third }.distinct()

    val rights = RightEntity.find{
        RightsTable.id inList rightIds
    }.toList()

    val roles: List<RoleEntity> = RoleEntity.find {
        RolesTable.id inList roleIds
    }.toList()

    val contexts =  ContextEntity
        .find { ContextsTable.id inList contextIds }
        .map { context -> Context(
            context.id.value.toString(),
            context.name,
            roles.filter{ role: RoleEntity ->
                userRoleContexts.any {
                        pair -> pair.second == context.id.value && pair.first == role.id.value
                }
            }.map { role ->
                Role(
                    role.id.value.toString(),
                    role.name,
                    role.description,
                    rights.filter { right -> roleRightContexts.any{ triple ->
                        triple.first == role.id.value &&
                            triple.second == context.id.value &&
                            triple.third == right.id.value
                    }  }.map { right ->
                        Right(
                            right.id.value.toString(),
                            right.name,
                            right.description
                        )
                    }
                )
            }
        ) }

    return userIds.associateWith { userId ->
        contexts.filter { context -> userRoleContexts.any { it.second == UUID.fromString(context.id) && it.third == userId } }
    }
}

fun Transaction.getRolesAndRights(userIds: List<UUID>, contextId: UUID): Map<String, List<Role>> {
    val userRoles = UserRoleContext.selectAll().where {
        UserRoleContext.contextId eq contextId and
        (UserRoleContext.userId inList  userIds)
    }.map { Pair( it[UserRoleContext.userId].value, it[UserRoleContext.roleId].value)  }.toList()


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
    }.map { Pair( it[UserRoleContext.userId].value, it[UserRoleContext.roleId].value)  }.toList()


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

