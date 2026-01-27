package org.solyton.solawi.bid.module.bid.action.api.shares

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.bid.data.api.ShareSubscription
import org.solyton.solawi.bid.module.bid.data.api.UpdateShareSubscription
import org.solyton.solawi.bid.module.bid.data.toApiType
import org.solyton.solawi.bid.module.bid.repository.updateShareSubscription
import java.util.*


@MathDsl
@Suppress("FunctionName")
fun UpdateShareSubscription() = KlAction<Result<Contextual<UpdateShareSubscription>>, Result<ShareSubscription>> { result ->
    DbAction { database -> result bindSuspend { contextual -> resultTransaction(database) {
        val userId = contextual.userId
        val data = contextual.data
        updateShareSubscription(
            UUID.fromString(data.id),
            UUID.fromString(data.shareOfferId),
            UUID.fromString(data.userProfileId),
            UUID.fromString(data.distributionPointId),
            UUID.fromString(data.fiscalYearId),
            data.numberOfShares,
            data.pricePerShare,
            data.ahcAuthorized,
            userId
        ).toApiType()
    } }  x database
} }
