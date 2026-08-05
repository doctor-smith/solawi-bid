package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.banking.data.api.CreateCreditorIdentifier
import org.solyton.solawi.bid.module.banking.data.api.CreditorIdentifier
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.repository.createCreditorIdentifier


@MathDsl
@Suppress("FunctionName")
fun CreateCreditorIdentifier(): KlAction<Result<Contextual<CreateCreditorIdentifier>>, Result<CreditorIdentifier>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val userId = contextual.userId
                val data = contextual.data
                createCreditorIdentifier(
                    userId,
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
