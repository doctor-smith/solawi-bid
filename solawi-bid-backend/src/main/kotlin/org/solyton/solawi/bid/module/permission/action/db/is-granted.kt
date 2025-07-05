package org.solyton.solawi.bid.module.permission.action.db

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.result.*
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.jetbrains.exposed.sql.*
import org.solyton.solawi.bid.module.permission.schema.Contexts
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.permission.schema.Rights
import org.solyton.solawi.bid.module.permission.schema.RightsTable
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import org.solyton.solawi.bid.module.permission.PermissionException
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.RightEntity
import org.solyton.solawi.bid.shared.ValueWithDescription
import java.util.UUID

@MathDsl
@Suppress("FunctionName")
fun <T> IsGranted(right: String, accessCheckNeeded: (Contextual<T>)->Boolean = {true}): KlAction<Result<Contextual<T>>,Result<Contextual<T>>> = KlAction {
    result -> DbAction { database -> result bindSuspend  { contextual ->
        resultTransaction(database) {
            when {
                !accessCheckNeeded(contextual) -> contextual
                isGranted(contextual.userId, UUID.fromString(contextual.context), right) -> contextual
                else -> throw PermissionException.AccessDenied
            }
        }
    } x database }
}

@MathDsl
@Suppress("FunctionName")
fun <T> IsGranted(right: UUID): KlAction<Result<Contextual<T>>,Result<Contextual<T>>> = KlAction {
    result -> DbAction { database -> result bindSuspend  { data ->
        resultTransaction(database) {
            when {
                isGranted(data.userId, UUID.fromString(data.context), right) -> data
                else -> throw PermissionException.AccessDenied
            }
        }
    } x database }
}

fun Transaction.isGranted(userId: UUID, context: String, right: String): Boolean {

    val contextEntity = ContextEntity.find { Contexts.name eq context }.firstOrNull()
        ?: throw PermissionException.NoSuchContext(context)
    val rightEntity = RightEntity.find{ Rights.name eq right }.firstOrNull()
        ?: throw PermissionException.NoSuchRight(right)
    val roleIds = contextEntity.roles.filter { it.rights.contains(rightEntity) }.map { it.id }

    return UserRoleContext.selectAll().where {
        UserRoleContext.userId eq userId and
        ( UserRoleContext.contextId eq contextEntity.id ) and
        ( UserRoleContext.roleId inList roleIds )
    }.toList().isNotEmpty()
}

fun Transaction.isGranted(userId: UUID, contextId: UUID, rightId: UUID): Boolean {
    val context = ContextEntity.find { ContextsTable.id eq contextId }.firstOrNull()
        ?: throw PermissionException.NoSuchContext(contextId.toString())
    val roleIds = context.roles.filter { it.rights.map { r -> r.id.value }.contains(rightId) }.map { it.id }


    return !UserRoleContext.selectAll().where {
        UserRoleContext.userId eq userId and
        ( UserRoleContext.contextId eq contextId ) and
        ( UserRoleContext.roleId inList roleIds  )
    }.empty()
}

fun Transaction.isGranted(userId: UUID, contextId: UUID, right: String): Boolean {
    val rightId = RightEntity.find { RightsTable.name eq right }.firstOrNull()?.id
        ?: throw PermissionException.NoSuchRight(right)

    return isGranted(userId, contextId, rightId.value)
}

fun Transaction.isGranted(userId: UUID, contextId: UUID, right: ValueWithDescription): Boolean {
    val rightId = RightEntity.find { RightsTable.name eq right.value }.firstOrNull()?.id
        ?: throw PermissionException.NoSuchRight(right.value)

    return isGranted(userId, contextId, rightId.value)
}
