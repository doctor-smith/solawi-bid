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
import org.solyton.solawi.bid.module.shares.data.api.TransferOwnership
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.shares.repository.transfer
import org.solyton.solawi.bid.module.values.UserId

@MathDsl
@Suppress("FunctionName")
fun TransferOwnership() = KlAction<Result<Contextual<TransferOwnership>>, Result<ShareSubscription>> { result ->
    DbAction { database -> result bindSuspend { contextual -> resultTransaction(database) {
        val data = contextual.data
        val modifierId = UserId(contextual.userId.toString())
        transfer(
            modifier = modifierId,
            shareSubscriptionId = data.shareSubscriptionId,
            newOwnerId = data.newOwnerId,
            changedBy = data.changedBy,
        ).toApiType()
    } }  x database
} }
