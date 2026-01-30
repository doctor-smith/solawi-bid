package org.solyton.solawi.bid.module.shares.action.api

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.shares.data.api.CreateShareType
import org.solyton.solawi.bid.module.shares.data.api.ShareType
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.shares.repository.createShareType
import java.util.*

@MathDsl
@Suppress("FunctionName")
fun CreateShareType() = KlAction<Result<Contextual<CreateShareType>>, Result<ShareType>> { result -> DbAction {
    database -> result bindSuspend  { contextual -> resultTransaction(database) {
        val userId = contextual.userId
        val data = contextual.data
        createShareType(
            UUID.fromString(data.providerId),
            data.name,
            data.key,
            data.description,
            userId
        ).toApiType()
    } }  x database
} }
