package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.banking.data.api.SepaMessages
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.repository.readSepaMessagesByLegalEntity
import java.util.*


@MathDsl
@Suppress("FunctionName")
fun ReadSepaMessagesByLegalEntityId(): KlAction<Result<Contextual<String>>, Result<SepaMessages>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val legalEntity = UUID.fromString(contextual.data)
                readSepaMessagesByLegalEntity(legalEntity).toApiType()
            }
        } x database
    }
}
