package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.joda.toJoda
import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.kotlinx.date.toDateTime
import org.evoleq.kotlinx.date.today
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.banking.data.api.SepaPayment
import org.solyton.solawi.bid.module.banking.data.api.UpdateSepaPayment
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.banking.repository.updatePayment
import java.util.*


@MathDsl
@Suppress("FunctionName")
fun UpdateSepaPayment(): KlAction<Result<Contextual<UpdateSepaPayment>>, Result<SepaPayment>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val userId = contextual.userId
                val data = contextual.data

                updatePayment(
                    userId,
                    UUID.fromString(data.sepaPaymentId.value),
                    data.amount,
                    data.executionDate.toDateTime().toJoda().toLocalDate(),
                    data.sequenceType.toDomainType(),
                    data.status.toDomainType(),
                    today().toDateTime().toJoda(),
                    data.failureReason,
                    data.endToEndId,
                    data.sepaMessageId,
                ).toApiType()

            }
        } x database
    }
}

/*
@MathDsl
@Suppress("FunctionName")
fun DeleteSepaPayments(): KlAction<Result<Contextual<DeleteSepaPayments>>, Result<SepaPaymentIds>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val data = contextual.data
                val paymentIds = data.paymentIds.map { UUID.fromString(it.value) }
                deletePayments(
                    paymentIds
                )
                SepaPaymentIds(data.paymentIds)
            }
        } x database
    }
}
 */
