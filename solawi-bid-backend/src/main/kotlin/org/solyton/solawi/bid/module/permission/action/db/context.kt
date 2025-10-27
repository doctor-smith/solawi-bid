package org.solyton.solawi.bid.module.permission.action.db

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.map
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.permission.PermissionException
import org.solyton.solawi.bid.module.permission.data.ContextAware
import org.solyton.solawi.bid.module.permission.data.api.ContextId
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import java.util.UUID


@MathDsl
@Suppress("FunctionName")
fun <T : ContextId> ReadContext(): KlAction<Result<Contextual<T>>, Result<Contextual<ContextAware<T>>>> = KlAction {
    result -> DbAction { database -> result bindSuspend  { contextual: Contextual<T> ->
        resultTransaction(database) {
            val context = ContextEntity.find { ContextsTable.id eq UUID.fromString(contextual.data.contextId) }.firstOrNull()
                ?: throw PermissionException.NoSuchContext(contextual.data.contextId)

            contextual.map {data: T -> ContextAware(data, context) }
        }
    } x database }
}
