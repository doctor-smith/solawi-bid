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
import org.solyton.solawi.bid.module.banking.data.api.UpdateCreditorIdentifier
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.repository.updateCreditorIdentifier

@MathDsl
@Suppress("FunctionName")
fun UpdateCreditorIdentifier(): KlAction<Result<Contextual<UpdateCreditorIdentifier>>, Result<CreditorIdentifier>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val userId = contextual.userId
                val data = contextual.data
                updateCreditorIdentifier(
                    userId,
                    data.creditorIdentifierId,
                    data.creditorId,
                    data.legalEntityId,
                    data.validFrom,
                    data.validUntil,
                    data.isActive
                ).toApiType()
            }
        } x database
    }
}
