package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.banking.data.SepaPaymentId
import org.solyton.solawi.bid.module.banking.data.api.DeleteSepaPayment
import org.solyton.solawi.bid.module.banking.data.api.DeleteSepaPayments
import org.solyton.solawi.bid.module.banking.data.api.SepaPaymentIds
import org.solyton.solawi.bid.module.banking.repository.deletePayment
import org.solyton.solawi.bid.module.banking.repository.deletePayments
import java.util.*


@MathDsl
@Suppress("FunctionName")
fun DeleteSepaPayment(): KlAction<Result<Contextual<DeleteSepaPayment>>, Result<SepaPaymentId>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val data = contextual.data
                deletePayment(
                    UUID.fromString(data.id.value)
                ).let { SepaPaymentId(it.toString()) }
            }
        } x database
    }
}

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

