package org.solyton.solawi.bid.module.permission.repository

import org.jetbrains.exposed.sql.*
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.RightEntity
import org.solyton.solawi.bid.module.permission.schema.RoleEntity
import org.solyton.solawi.bid.module.permission.schema.RoleRightContexts
import org.solyton.solawi.bid.module.permission.schema.RolesTable
import java.util.*

fun grant(
    context: ContextEntity,
    role: RoleEntity,
    vararg rights: RightEntity
) {
    rights.forEach { right ->
        RoleRightContexts.insert {
            it[contextId] = context.id
            it[roleId] = role.id
            it[rightId] = right.id
        }
    }
}

data class RoleInContext(
    val role: RoleEntity, val context: ContextEntity
)

infix fun RoleEntity.of(context: ContextEntity): RoleInContext = RoleInContext(this, context)

fun RoleInContext.grant(vararg rights: RightEntity) = grant(
    context, role, *rights
)

data class RightRoleContextIds(
    val id: UUID,
    val rightId: UUID,
    val roleId: UUID,
    val contextId: UUID,
)

fun Transaction.cloneRightRoleContext(oldContextId: UUID, newContextId: UUID): List<RightRoleContextIds> {
    val rightRoleContexts = RoleRightContexts.selectAll().where {
        RoleRightContexts.contextId eq oldContextId
    }.map { row -> RightRoleContextIds(
        row[RoleRightContexts.id].value,
        row[RoleRightContexts.rightId].value,
        row[RoleRightContexts.roleId].value ,
        newContextId
    ) }.map{ rrc ->
        // todo:dev only insert those rrc which are not present; compare [cloneRightRoleContextsWrtRoles]
        val id = RoleRightContexts.insertAndGetId {
            it[contextId] = rrc.contextId
            it[roleId] = rrc.roleId
            it[rightId] = rrc.rightId
        }
        RightRoleContextIds(
            id.value,
            rrc.rightId,
            rrc.roleId,
            rrc.contextId
        )
    }
    return rightRoleContexts
}

fun Transaction.cloneRightRoleContextWrtRoles(
    oldContextId: UUID,
    newContextId: UUID,
    vararg roleNames: String
): List<RightRoleContextIds> {
    val existingInTarget = getRoleRightContextsByRoleNames(newContextId,*roleNames)
    val rightRoleContexts = getRoleRightContextsByRoleNames(
        oldContextId,
        *roleNames
    ).map { it.copy(
        contextId = newContextId
    ) }.filter {
        it !in existingInTarget
    }.map{ rrc ->
        val id = RoleRightContexts.insertAndGetId {
            it[contextId] = rrc.contextId
            it[roleId] = rrc.roleId
            it[rightId] = rrc.rightId
        }
        RightRoleContextIds(
            id.value,
            rrc.rightId,
            rrc.roleId,
            rrc.contextId
        )
    }
    return rightRoleContexts
}

fun getRoleRightContextsByRoleNames(
    contextId: UUID,
    vararg roleNames: String
): List<RightRoleContextIds> {
    val roleIds = RoleEntity.find { RolesTable.name inList listOf(*roleNames) }.map { it.id.value }
    val rightRoleContexts = RoleRightContexts.selectAll().where {
        RoleRightContexts.contextId eq contextId and (RoleRightContexts.rightId inList roleIds)
    }.map { row -> RightRoleContextIds(
        row[RoleRightContexts.id].value,
        row[RoleRightContexts.rightId].value,
        row[RoleRightContexts.roleId].value ,
        contextId
    ) }
    return rightRoleContexts
}
