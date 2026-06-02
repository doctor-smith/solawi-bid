package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaPayments
import org.solyton.solawi.bid.module.banking.data.api.UpdateSepaPaymentExecutionStatuses
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.banking.repository.updateSepaPaymentExecutionStatuses
import java.util.*



@MathDsl
@Suppress("FunctionName")
fun UpdateSepaPaymentExecutionStatuses(): KlAction<Result<Contextual<UpdateSepaPaymentExecutionStatuses>>, Result<ApiSepaPayments>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val data = contextual.data
                val userId = contextual.userId
                val payments = updateSepaPaymentExecutionStatuses(
                    userId,
                    data.paymentIds.map { UUID.fromString(it.value) },
                    data.newStatus.toDomainType()
                )
                ApiSepaPayments(payments.map {
                    it.toApiType()
                })
            }
        } x database
    }
}

