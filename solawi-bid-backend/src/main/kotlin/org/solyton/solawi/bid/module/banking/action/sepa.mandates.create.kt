package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.joda.toJoda
import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.banking.data.api.CreateSepaMandate
import org.solyton.solawi.bid.module.banking.data.api.SepaMandate
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.banking.repository.createSepaMandateWithRetry
import org.solyton.solawi.bid.module.banking.repository.validatedCreditorIdentifier
import java.util.*

@MathDsl
@Suppress("FunctionName")
fun CreateSepaMandate(): KlAction<Result<Contextual<CreateSepaMandate>>, Result<SepaMandate>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val data = contextual.data
                val userId = contextual.userId

                val creditor = validatedCreditorIdentifier(data.creditorId)
                val creditorIdentifierId = creditor.id.value
                val debtorBankAccountId = UUID.fromString(data.debtorBankAccountId.value)
                val collectionId = data.collectionId?.let { UUID.fromString(it.value) }

                createSepaMandateWithRetry(
                    creatorId = userId,
                    creditorIdentifierId = creditorIdentifierId,
                    debtorBankAccountId = debtorBankAccountId,
                    debtorName = data.debtorName,
                    signedAt = data.signedAt.toJoda(),
                    validFrom = data.validFrom.toJoda(),
                    validUntil = data.validUntil?.toJoda(),
                    mandateReference = data.mandateReference,
                    mandateReferencePrefix = data.mandateReferencePrefix?.value,
                    referenceData = data.sepaMandateReferenceData,
                    status = data.status.toDomainType(),
                    collectionId = collectionId,
                    maxRetries = 5,

                ).toApiType()
            }
        } x database
    }
}
