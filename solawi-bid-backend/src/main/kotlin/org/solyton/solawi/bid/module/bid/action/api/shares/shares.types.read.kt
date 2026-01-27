package org.solyton.solawi.bid.module.bid.action.api.shares

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.bid.data.api.ShareTypes
import org.solyton.solawi.bid.module.bid.data.toApiType
import org.solyton.solawi.bid.module.bid.repository.readShareTypesByProvider
import java.util.*


data class ReadShareTypesByProvider(
    val providerId: UUID
)

@MathDsl
@Suppress("FunctionName")
fun ReadShareTypesByProvider() = KlAction<Result<Contextual<ReadShareTypesByProvider>>, Result<ShareTypes>> { result -> DbAction {
    database -> result bindSuspend  { contextual -> resultTransaction(database) {
        val userId = contextual.userId
        val data = contextual.data
        readShareTypesByProvider(data.providerId).toApiType()
    } }  x database
} }
