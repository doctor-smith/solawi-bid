package org.solyton.solawi.bid.module.permission.action.db

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.*
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.value.StringValueWithDescription
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.solyton.solawi.bid.module.permission.PermissionException
import org.solyton.solawi.bid.module.permission.schema.*
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.RightEntity
import java.util.*

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

@MathDsl
@Suppress("FunctionName")
fun <T> IsGrantedOneOf(vararg rights: String, accessCheckNeeded: (Contextual<T>)->Boolean = {true}): KlAction<Result<Contextual<T>>,Result<Contextual<T>>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                when {
                    !accessCheckNeeded(contextual) -> contextual
                    isGrantedOneOf(
                        contextual.userId,
                        UUID.fromString(contextual.context),
                        listOf(*rights)
                    ) -> contextual

                    else -> throw PermissionException.AccessDenied
                }
            }
        } x database
    }
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

fun Transaction.isGranted(userId: UUID, contextId: UUID, right: StringValueWithDescription): Boolean {
    val rightId = RightEntity.find { RightsTable.name eq right.value }.firstOrNull()?.id
        ?: throw PermissionException.NoSuchRight(right.value)

    return isGranted(userId, contextId, rightId.value)
}

/**
 * Check if the user is granted at least on of the presented rights in a certain context
 */
fun Transaction.isGrantedOneOf(userId: UUID, contextId: UUID, rightIds: List<String>): Boolean {
    return !UserRoleContext
        .join(
            RoleRightContexts,
            JoinType.INNER,
            onColumn = UserRoleContext.roleId,
            otherColumn = RoleRightContexts.roleId
        ).join(
            Rights,
            JoinType.INNER,
            onColumn = RoleRightContexts.rightId,
            otherColumn = RightsTable.id
        )
        .select(UserRoleContext.userId)
        .adjustWhere {
            (UserRoleContext.userId eq userId) and
            (RoleRightContexts.contextId eq contextId) and
            (Rights.name inList rightIds)
        }
        .limit(1)
        .empty()
}
