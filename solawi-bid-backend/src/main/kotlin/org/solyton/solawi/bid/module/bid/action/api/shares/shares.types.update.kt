package org.solyton.solawi.bid.module.bid.action.api.shares

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.bid.data.api.ShareType
import org.solyton.solawi.bid.module.bid.data.api.UpdateShareType
import org.solyton.solawi.bid.module.bid.data.toApiType
import org.solyton.solawi.bid.module.bid.repository.updateShareType
import java.util.*


@MathDsl
@Suppress("FunctionName")
fun UpdateShareType() = KlAction<Result<Contextual<UpdateShareType>>, Result<ShareType>> { result -> DbAction {
    database -> result bindSuspend  { contextual -> resultTransaction(database) {
        val userId = contextual.userId
        val data = contextual.data
        updateShareType(
            UUID.fromString(data.id),
            UUID.fromString(data.providerId),
            data.name,
            data.key,
            data.description,
            userId
        ).toApiType()
    } }  x database
} }
