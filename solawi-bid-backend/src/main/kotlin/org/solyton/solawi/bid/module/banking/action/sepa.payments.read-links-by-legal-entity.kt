package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.banking.data.api.SepaPaymentLinks
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.repository.readSepaPaymentLinksByLegalEntity
import java.util.*

@MathDsl
@Suppress("FunctionName")
fun ReadSepaPaymentLinksByLegalEntity(): KlAction<Result<Contextual<String>>, Result<SepaPaymentLinks>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val data = contextual.data
                val legalEntityId = UUID.fromString(data)
                SepaPaymentLinks(readSepaPaymentLinksByLegalEntity(legalEntityId).toApiType())
            }
        } x database
    }
}
