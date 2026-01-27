package org.solyton.solawi.bid.module.bid.action.api.shares

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.bid.data.api.CreateShareOffer
import org.solyton.solawi.bid.module.bid.data.api.ShareOffer
import org.solyton.solawi.bid.module.bid.data.toApiType
import org.solyton.solawi.bid.module.bid.data.toDomainType
import org.solyton.solawi.bid.module.bid.repository.createShareOffer
import java.util.*

@MathDsl
@Suppress("FunctionName")
fun CreateShareOffer() = KlAction<Result<Contextual<CreateShareOffer>>, Result<ShareOffer>> { result ->
    DbAction { database -> result bindSuspend { contextual -> resultTransaction(database) {
        val userId = contextual.userId
        val data = contextual.data
        createShareOffer(
            UUID.fromString(data.shareTypeId),
            UUID.fromString(data.fiscalYearId),
            data.price,
            data.pricingType.toDomainType(),
            data.ahcAuthorizationRequired,
            userId
        ).toApiType()
    } }  x database
} }
