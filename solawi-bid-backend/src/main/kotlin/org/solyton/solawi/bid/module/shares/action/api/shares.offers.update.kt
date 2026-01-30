package org.solyton.solawi.bid.module.shares.action.api

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.shares.data.api.ShareOffer
import org.solyton.solawi.bid.module.shares.data.api.UpdateShareOffer
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.shares.data.toDomainType
import org.solyton.solawi.bid.module.shares.repository.updateShareOffer
import java.util.*


@MathDsl
@Suppress("FunctionName")
fun UpdateShareOffer() = KlAction<Result<Contextual<UpdateShareOffer>>, Result<ShareOffer>> { result ->
    DbAction { database -> result bindSuspend { contextual -> resultTransaction(database) {
        val userId = contextual.userId
        val data = contextual.data
        updateShareOffer(
            UUID.fromString(data.id),
            UUID.fromString(data.shareTypeId),
            UUID.fromString(data.fiscalYearId),
            data.price,
            data.pricingType.toDomainType(),
            data.ahcAuthorizationRequired,
            userId
        ).toApiType()
    } }  x database
} }
