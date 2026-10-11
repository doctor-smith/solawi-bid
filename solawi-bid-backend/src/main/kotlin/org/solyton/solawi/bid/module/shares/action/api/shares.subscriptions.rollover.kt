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
import org.solyton.solawi.bid.module.shares.data.api.RolloverShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.api.ShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.shares.data.toDomainType
import org.solyton.solawi.bid.module.shares.repository.rollover


@MathDsl
@Suppress("FunctionName")
fun RolloverShareSubscription() = KlAction<Result<Contextual<RolloverShareSubscriptions>>, Result<ShareSubscriptions>> { result ->
    DbAction { database -> result bindSuspend { contextual -> resultTransaction(database) {
        val data = contextual.data
        val shareSubscriptionIds = data.shareSubscriptionIds.map { it.value.toUuid() }
        rollover(
            shareSubscriptionIds = shareSubscriptionIds,
            toShareOfferId = data.toShareOfferId.value.toUuid(),
            changedBy = data.changedBy.toDomainType(),
            modifiedBy = data.modifiedBy.value.toUuid()
        ).toApiType()
    } }  x database
} }
