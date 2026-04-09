package org.solyton.solawi.bid.module.shares.action.api

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.shares.data.api.ShareOffers
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.shares.repository.readShareOffersByProvider
import java.util.*


data class ReadShareOffersByProvider(
    val providerId: UUID,
    val fiscalYearIds: Set<UUID>
)

@MathDsl
@Suppress("FunctionName")
fun ReadShareOffersByProvider() = KlAction<Result<Contextual<ReadShareOffersByProvider>>, Result<ShareOffers>> { result -> DbAction {
    database -> result bindSuspend  { contextual -> resultTransaction(database) {
        // val userId = contextual.userId
        val data = contextual.data
        readShareOffersByProvider(
            data.providerId,
            data.fiscalYearIds
        ).toApiType()
    } } x database
} }
