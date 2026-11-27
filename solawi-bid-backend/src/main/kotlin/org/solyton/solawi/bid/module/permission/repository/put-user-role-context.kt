package org.solyton.solawi.bid.module.permission.repository

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.solyton.solawi.bid.module.application.permission.delegableOrganizationApplicationRoles
import org.solyton.solawi.bid.module.application.schema.OrganizationApplicationContextEntity
import org.solyton.solawi.bid.module.application.schema.OrganizationApplicationContextsTable
import org.solyton.solawi.bid.module.permission.PermissionException
import org.solyton.solawi.bid.module.permission.action.db.isGranted
import org.solyton.solawi.bid.module.permission.data.api.Context
import org.solyton.solawi.bid.module.permission.data.api.Right
import org.solyton.solawi.bid.module.permission.data.api.Role
import org.solyton.solawi.bid.module.permission.data.api.UserContext
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import org.solyton.solawi.bid.module.permission.schema.rightsInContext
import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import java.util.*

fun Transaction.updateUserRoleContext(actorId: UUID, userId: UUID, contextId: UUID, roleIds: Set<UUID>): UserContext {
    if (ContextsTable.selectAll().where { ContextsTable.id eq contextId }.forUpdate().empty()) {
        throw PermissionException.AccessDenied
    }
    val bookings = OrganizationApplicationContextEntity.find {
        OrganizationApplicationContextsTable.contextId eq contextId
    }.toList()
    if (bookings.size > 1) throw PermissionException.AccessDenied
    val booking = bookings.singleOrNull()
    if (booking == null) {
        if (!isGranted(actorId, contextId, "MANAGE_USER_ROLE_CONTEXTS")) throw PermissionException.AccessDenied
        return putUserRoleContext(userId, contextId, roleIds.toList())
    }

    return updateOrganizationApplicationRoles(actorId, userId, booking, roleIds)
}

private fun Transaction.updateOrganizationApplicationRoles(
    actorId: UUID,
    userId: UUID,
    booking: OrganizationApplicationContextEntity,
    roleIds: Set<UUID>
): UserContext {
    val contextId = booking.context.id.value
    val organization = OrganizationEntity.findById(booking.organizationId) ?: throw PermissionException.AccessDenied
    val members = organization.members.map { it.id.value }.toSet()
    if (actorId !in members || userId !in members) throw PermissionException.AccessDenied
    val actorRoles = getRolesByUserAndContext(actorId, contextId)
    if (actorRoles.none { it.name == "OWNER" }) throw PermissionException.AccessDenied

    val approvedNames = delegableOrganizationApplicationRoles(booking.application.name)
    val delegableIds = booking.context.roles.filter { it.name in approvedNames }.map { it.id.value }.toSet()
    val assignedIds = getRolesByUserAndContext(userId, contextId).map { it.id.value }.toSet()
    if ((roleIds - assignedIds - delegableIds).isNotEmpty()) throw PermissionException.AccessDenied

    val removed = (assignedIds - roleIds) intersect delegableIds
    UserRoleContext.deleteWhere {
        (UserRoleContext.userId eq userId) and (UserRoleContext.contextId eq contextId) and
            (UserRoleContext.roleId inList removed)
    }
    insertUserRoles(userId, contextId, roleIds - assignedIds)
    return readUserRoleContext(userId, booking.context)
}

private fun Transaction.insertUserRoles(userId: UUID, contextId: UUID, roles: Iterable<UUID>) {
    roles.forEach { role ->
        UserRoleContext.insert {
            it[UserRoleContext.userId] = userId
            it[UserRoleContext.contextId] = contextId
            it[roleId] = role
        }
    }
}


/**
 * Updates the roles of a user within a specific context by clearing existing roles and adding new ones.
 *
 * @param userId The unique identifier of the user whose roles are being updated.
 * @param contextId The unique identifier of the context for which the roles are being updated.
 * @param roleIds A list of unique identifiers of roles to assign to the user within the context.
 * @return A UserContext object representing the updated user-role-context relationship with detailed information.
 * @throws NoSuchElementException If no context is found for the given contextId.
 */
// todo:test add unit tests
fun Transaction.putUserRoleContext(userId: UUID, contextId: UUID, roleIds: List<UUID>): UserContext{

    UserRoleContext.deleteWhere { (UserRoleContext.userId eq userId) and (UserRoleContext.contextId eq contextId) }
    insertUserRoles(userId, contextId, roleIds)
    val context = ContextEntity.findById(contextId) ?: throw NoSuchElementException("Context with id $contextId not found")
    return readUserRoleContext(userId, context)
}

private fun Transaction.readUserRoleContext(userId: UUID, context: ContextEntity): UserContext {
    val contextId = context.id.value
    val roles = getRolesByUserAndContext(userId, contextId).map{ role ->
        Role(
            id = role.id.value.toString(),
            name =role.name,
            description = role.description,
            rights = role.rightsInContext(contextId).map { right -> Right(
                right.id.value.toString(),
                right.name,
                right.description
            ) }
        )
    }


    return UserContext(
        userId.toString(),
        Context(
            contextId.toString(),
            context.name,
            roles
        )
    )
}
