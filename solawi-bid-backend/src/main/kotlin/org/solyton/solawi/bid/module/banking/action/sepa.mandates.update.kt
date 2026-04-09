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
import org.solyton.solawi.bid.module.banking.data.api.SepaMandate
import org.solyton.solawi.bid.module.banking.data.api.UpdateSepaMandate
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.banking.repository.updateSepaMandate
import java.util.*


@MathDsl
@Suppress("FunctionName")
fun UpdateSepaMandate(): KlAction<Result<Contextual<UpdateSepaMandate>>, Result<SepaMandate>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val data = contextual.data
                val userId = contextual.userId

                val sepaMandateId = UUID.fromString(data.sepaMandateId.value)
                val creditorId = UUID.fromString(data.creditorId.value)
                val debtorBankAccountId = UUID.fromString(data.debtorBankAccountId.value)

                updateSepaMandate(
                    modifierId = userId,
                    sepaMandateId = sepaMandateId,
                    creditorIdentifierId = creditorId,
                    debtorBankAccountId = debtorBankAccountId,
                    debtorName = data.debtorName,
                    signedAt = data.signedAt.toJoda(),
                    validFrom = data.validFrom.toJoda(),
                    validUntil = data.validUntil?.toJoda(),
                    lastUsedAt = data.lastUsedAt?.toJoda(),
                    status = data.status.toDomainType(),
                //    isActive = data.isActive,
                //    amendmentOf = data.amendmentOf

                ).toApiType()
            }
        } x database
    }
}
