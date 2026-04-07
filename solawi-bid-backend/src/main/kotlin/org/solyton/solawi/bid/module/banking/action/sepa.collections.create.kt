package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.banking.data.api.CreateSepaCollection
import org.solyton.solawi.bid.module.banking.data.api.SepaCollection
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.banking.repository.createSepaCollection


@MathDsl
@Suppress("FunctionName")
fun CreateSepaCollection(): KlAction<Result<Contextual<CreateSepaCollection>>, Result<SepaCollection>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val data = contextual.data
                val userId = contextual.userId
                createSepaCollection(
                    creator = userId,
                    creditorIdentifierId = data.creditorIdentifierId,
                    creditorAccountId = data.creditorBankAccountId,
                    mandateReferencePrefix = data.mandateReferencePrefix,
                    remittanceInformation = data.remittanceInformation,
                    sepaSequenceType = data.sepaSequenceType.toDomainType(),
                    localInstrument = data.localInstrument,
                    chargeBearer = data.chargeBearer,
                    leadTimesDays = data.leadTimeDays,
                    purposeCode = data.purposeCode,
                    retryOnFailure = data.retryOnFailure,
                    maxRetries = data.maxRetries,
                    isActive = data.isActive,
                    referenceIds = data.referenceIds
                ).toApiType()
            }
        } x database
    }
}
