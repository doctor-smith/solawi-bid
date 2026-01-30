package org.solyton.solawi.bid.module.shares.action.api

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.uuid.toUuidOrNull
import org.solyton.solawi.bid.module.shares.data.api.ImportShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.api.ShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.shares.data.toInternalType
import org.solyton.solawi.bid.module.shares.service.ShareToImport
import org.solyton.solawi.bid.module.shares.service.importShareSubscriptions
import java.util.*

@MathDsl
@Suppress("FunctionName")
fun ImportShareSubscriptions() = KlAction<Result<Contextual<ImportShareSubscriptions>>, Result<ShareSubscriptions>> { result ->
    DbAction { database -> result bindSuspend { contextual -> resultTransaction(database) {
        val userId = contextual.userId
        val data = contextual.data
        importShareSubscriptions(
            data.override,
            UUID.fromString(data.providerId),
            UUID.fromString(data.fiscalYearId),
            data.shareSubscriptions.map { subscription ->
                ShareToImport(
                    UUID.fromString(subscription.shareOfferId),
                    UUID.fromString(subscription.userProfileId),
                    subscription.distributionPointId.toUuidOrNull(),
                    subscription.numberOfShares,
                    subscription.pricePerShare,
                    subscription.ahcAuthorized,
                    subscription.status.toInternalType(),
                    subscription.coSubscribers
                )
            },
            userId
        ).toApiType()
    } }  x database
} }
