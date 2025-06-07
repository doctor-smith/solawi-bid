package org.solyton.solawi.bid.module.db.repository

import org.jetbrains.exposed.sql.*
import org.solyton.solawi.bid.module.db.schema.ContextEntity
import org.solyton.solawi.bid.module.db.schema.RightEntity
import org.solyton.solawi.bid.module.db.schema.RoleEntity
import org.solyton.solawi.bid.module.db.schema.RoleRightContexts
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

fun RoleInContext.grant(vararg rights: RightEntity) = org.solyton.solawi.bid.module.db.repository.grant(
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
