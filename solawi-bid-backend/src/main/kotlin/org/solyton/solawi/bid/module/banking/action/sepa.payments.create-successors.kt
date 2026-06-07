package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.joda.toJoda
import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.kotlinx.date.toDateTime
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaPayments
import org.solyton.solawi.bid.module.banking.data.api.CreateSepaPaymentSuccessors
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.repository.createSuccessorsOfPayments
import java.util.*

@MathDsl
@Suppress("FunctionName")
fun CreateSepaPaymentSuccessors(): KlAction<Result<Contextual<CreateSepaPaymentSuccessors>>, Result<ApiSepaPayments>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val data = contextual.data
                val userId = contextual.userId
                val payments = createSuccessorsOfPayments(
                    creator = userId,
                    executionDate = data.executionDate.toDateTime().toJoda().toLocalDate(),
                    paymentIds = data.paymentIds.map { UUID.fromString(it.value) },
                )
                ApiSepaPayments(payments.map {
                    it.toApiType()
                })
            }
        } x database
    }
}
