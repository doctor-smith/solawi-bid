package org.solyton.solawi.bid.module.permission.repository

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.solyton.solawi.bid.module.permission.schema.RoleRightContexts
import java.util.*

/**
 * Updates the role-right-context associations in the database. This method deletes existing associations
 * for a specific role and context and inserts new associations for the specified rights.
 *
 * @param roleId The unique identifier of the role to which the rights are being updated.
 * @param rightIds A list of unique identifiers representing the rights to be associated with the role.
 * @param contextId The unique identifier of the context in which the role-right association applies.
 */
fun Transaction.putRoleRightContext(roleId: UUID, rightIds: List<UUID>, contextId: UUID) {
    RoleRightContexts.deleteWhere {
        RoleRightContexts.roleId eq roleId and (RoleRightContexts.contextId eq contextId)
    }
    RoleRightContexts.batchInsert(rightIds.map { it to (contextId to roleId) }) { (rightId, roleContext) ->
        this[RoleRightContexts.rightId] = rightId
        this[RoleRightContexts.contextId] = roleContext.first
        this[RoleRightContexts.roleId] = roleContext.second
    }
}
