package org.solyton.solawi.bid.module.permission.repository

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.solyton.solawi.bid.module.permission.data.api.Context
import org.solyton.solawi.bid.module.permission.data.api.Right
import org.solyton.solawi.bid.module.permission.data.api.Role
import org.solyton.solawi.bid.module.permission.data.api.UserContext
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import java.util.*

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

    // delete all roles except the owner
    UserRoleContext.deleteWhere { (UserRoleContext.userId eq userId) and (UserRoleContext.contextId eq contextId) }
    roleIds.forEach { roleId -> UserRoleContext.insert { it[this.userId] = userId; it[this.contextId] = contextId; it[this.roleId] = roleId } }

    val context = ContextEntity.findById(contextId) ?: throw NoSuchElementException("Context with id $contextId not found")

    return UserContext(
        userId.toString(),
        Context(contextId.toString(),
            context.name,
            getRolesByUserAndContext(userId, contextId).map {
                    role -> Role(
                role.id.value.toString(),
                role.name,
                role.description,
                role.rights.map {
                        right -> Right(
                    right.id.value.toString(),
                    right.name,
                    right.description
                )
                } )
            }
        )
    )
}
