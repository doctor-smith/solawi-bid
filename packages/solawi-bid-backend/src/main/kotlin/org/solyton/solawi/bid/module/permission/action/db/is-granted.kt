package org.solyton.solawi.bid.module.permission.action.db

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.result.*
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.util.Contextual
import org.evoleq.util.DbAction
import org.evoleq.util.KlAction
import org.jetbrains.exposed.sql.*
import org.solyton.solawi.bid.module.db.schema.*
import org.solyton.solawi.bid.module.permission.PermissionException
import java.util.UUID

@MathDsl
@Suppress("FunctionName")
fun <T> IsGranted(right: String): KlAction<Result<Contextual<T>>,Result<Contextual<T>>> = KlAction {
    result -> DbAction { database -> result bindSuspend  { data ->
        resultTransaction(database) {
            when {
                isGranted(data.userId, data.context, right) -> data
                else -> throw PermissionException.AccessDenied
            }
        }
    } x database }
}

fun Transaction.isGranted(userId: UUID, context: String, right: String): Boolean {

    val contextEntity = ContextEntity.find { Contexts.name eq context }.firstOrNull()?: throw PermissionException.NoSuchContext(context)
    val rightEntity = RightEntity.find{ Rights.name eq right }.firstOrNull()?: throw PermissionException.NoSuchRight(right)
    val roleIds = contextEntity.roles.filter { it.rights.contains(rightEntity) }.map { it.id }

    return UserRoleContext.selectAll().where {
        UserRoleContext.userId eq userId and
        ( UserRoleContext.contextId eq contextEntity.id ) and
        ( UserRoleContext.roleId inList roleIds )
    }.toList().isNotEmpty()
}
