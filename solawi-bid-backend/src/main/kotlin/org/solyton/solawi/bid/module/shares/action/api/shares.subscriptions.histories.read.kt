package org.solyton.solawi.bid.module.shares.action.api

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.uuid.toUuid
import org.solyton.solawi.bid.module.shares.data.api.ShareSubscriptionStatusHistories
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId
import org.solyton.solawi.bid.module.shares.repository.readShareSubscriptionStatusHistories

data class ReadShareSubscriptionStatusHistories(
    val all: List<ShareSubscriptionId>
)


@MathDsl
@Suppress("FunctionName")
fun ReadShareSubscriptionStatusHistories() = KlAction<Result<Contextual<ReadShareSubscriptionStatusHistories>>, Result<ShareSubscriptionStatusHistories>> { result -> DbAction {
        database -> result bindSuspend  { contextual -> resultTransaction(database) {
            readShareSubscriptionStatusHistories(contextual.data.all.map{it.value.toUuid()}).toApiType()
    } }  x database
} }
