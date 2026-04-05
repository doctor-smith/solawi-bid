package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.banking.data.api.CreditorIdentifier
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.schema.CreditorIdentifierEntity
import org.solyton.solawi.bid.module.banking.schema.CreditorIdentifiersTable
import java.util.*


@MathDsl
@Suppress("FunctionName")
fun ReadCreditorIdentifierByLegalEntity(): KlAction<Result<Contextual<String>>, Result<CreditorIdentifier>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val legalEntityId = UUID.fromString(contextual.data)
                CreditorIdentifierEntity.find {
                    CreditorIdentifiersTable.legalEntityId eq legalEntityId
                }.toList().first().toApiType()
            }
        } x database
    }
}
