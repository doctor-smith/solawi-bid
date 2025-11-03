package org.solyton.solawi.bid.module.application.repository

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.solyton.solawi.bid.module.application.schema.ApplicationEntity
import org.solyton.solawi.bid.module.application.schema.ModuleEntity
import org.solyton.solawi.bid.module.permission.exception.ContextException
import org.solyton.solawi.bid.module.permission.exception.PermissionException
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.permission.schema.RoleEntity
import org.solyton.solawi.bid.module.permission.schema.RolesTable
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import java.util.*


fun getDefaultContext(defaultContextId: UUID?): ContextEntity = when(defaultContextId) {
    null -> ContextEntity.find { ContextsTable.rootId eq null and (ContextsTable.name eq "EMPTY") }.firstOrNull()
        ?: throw ContextException.NoSuchContext("EMPTY")
    else -> ContextEntity.find { ContextsTable.id eq defaultContextId }.firstOrNull()
        ?: throw ContextException.NoSuchContext(defaultContextId.toString())
}

fun ApplicationEntity.buildUserApplicationContextName(userId: UUID): String =
    "$name.${id.value}.$userId"

fun ModuleEntity.buildUserModuleContextName(userId: UUID): String =
    "$name.${id.value}.$userId"

fun ApplicationEntity.buildOrganizationApplicationContextName(userId: UUID): String =
    "$name.${id.value}.$userId"

fun ModuleEntity.buildOrganizationModuleContextName(userId: UUID): String =
    "$name.${id.value}.$userId"

/**
 * Drop user and application info
 * Recall: context_name ~ <name>|<name>.<app_id|module_id>.<user_id>
 * Result will return the name
 */
fun String.reduceContextName(): String = with(split(".")) { when{
    size <= 1 -> this@reduceContextName
    size == 2 -> this[0]
    else -> take(size-2).joinToString(".") { it }
} }

fun createUserRoleContext(userId: UUID, role: String, contextId: UUID): UUID {
    val ownerRoleId = RoleEntity.find { RolesTable.name eq role }.firstOrNull()?.id?.value
        ?: throw PermissionException.NoSuchRole(role)
    return UserRoleContext.insertAndGetId {
        it[UserRoleContext.userId] = userId
        it[UserRoleContext.roleId] = ownerRoleId
        it[UserRoleContext.contextId] = contextId
    }.value
}
