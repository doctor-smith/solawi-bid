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
import org.solyton.solawi.bid.module.banking.data.api.CreateAdHocSepaPayment
import org.solyton.solawi.bid.module.banking.data.api.SepaPayment
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.repository.createAdHocPayment
import org.solyton.solawil.bid.module.banking.data.toUUID


@MathDsl
@Suppress("FunctionName")
fun CreateAdHocSepaPayment(): KlAction<Result<Contextual<CreateAdHocSepaPayment>>, Result<SepaPayment>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val userId = contextual.userId
                val data = contextual.data
                createAdHocPayment(
                    userId,
                    data.sepaMandateId.toUUID(),
                    data.sepaCollectionId.toUUID(),
                    data.amount,
                    data.executionDate.toDateTime().toJoda().toLocalDate(),
                    data.predecessorId?.toUUID()
                ).toApiType()
            }
        } x database
    }
}
