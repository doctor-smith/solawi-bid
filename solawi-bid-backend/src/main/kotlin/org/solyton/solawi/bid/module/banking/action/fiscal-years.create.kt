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
import org.solyton.solawi.bid.module.banking.data.api.CreateFiscalYear
import org.solyton.solawi.bid.module.banking.data.api.FiscalYear
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.repository.createFiscalYear
import java.util.*


@MathDsl
@Suppress("FunctionName")
fun CreateFiscalYear(): KlAction<Result<Contextual<CreateFiscalYear>>, Result<FiscalYear>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val data = contextual.data
                val userId = contextual.userId
                createFiscalYear(
                    UUID.fromString(data.legalEntityId),
                    data.start.toDateTime().toJoda(),
                    data.end.toDateTime().toJoda(),
                    userId
                ).toApiType()
            }
        } x database
    }
}
