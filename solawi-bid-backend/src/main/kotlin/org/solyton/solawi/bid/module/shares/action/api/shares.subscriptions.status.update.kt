package org.solyton.solawi.bid.module.shares.action.api

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.shares.data.api.ShareSubscription
import org.solyton.solawi.bid.module.shares.data.api.UpdateShareStatus
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.shares.data.toDomainType
import org.solyton.solawi.bid.module.shares.data.toInternalType
import org.solyton.solawi.bid.module.shares.repository.next
import org.solyton.solawil.bid.module.shares.data.toUUID
import java.util.*

@MathDsl
@Suppress("FunctionName")
fun UpdateShareStatus() = KlAction<Result<Contextual<UpdateShareStatus>>, Result<ShareSubscription>> { result ->
    DbAction { database -> result bindSuspend { contextual -> resultTransaction(database) {
        // val userId = contextual.userId
        val data = contextual.data
        next(
            shareSubscriptionId = data.shareSubscriptionId.toUUID(),
            nextState = data.nextState.toInternalType(),
            reason = data.reason.toDomainType(),
            changedBy = data.changedBy.toDomainType(),
            modifier = data.modifier?.let { UUID.fromString(it.value) },
            comment = data.comment
        ).toApiType()
    } }  x database
} }
