package org.solyton.solawi.bid.module.shares.action.api

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.shares.data.api.CreateShareSubscription
import org.solyton.solawi.bid.module.shares.data.api.ShareSubscription
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.shares.repository.createShareSubscription
import java.util.*


@MathDsl
@Suppress("FunctionName")
fun CreateShareSubscription() = KlAction<Result<Contextual<CreateShareSubscription>>, Result<ShareSubscription>> { result ->
    DbAction { database -> result bindSuspend { contextual -> resultTransaction(database) {
        val userId = contextual.userId
        val data = contextual.data
        createShareSubscription(
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
