package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.banking.data.api.SepaCollections
import org.solyton.solawi.bid.module.banking.repository.readSepaCollectionsByLegalEntity
import java.util.*

@MathDsl
@Suppress("FunctionName")
fun ReadSepaCollectionsByLegalEntity(): KlAction<Result<Contextual<String>>, Result<SepaCollections>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val legalEntity = UUID.fromString(contextual.data)
                readSepaCollectionsByLegalEntity(legalEntity)
            }
        } x database
    }
}
