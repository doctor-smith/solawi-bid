package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.banking.data.api.FiscalYears
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.repository.readFiscalYears
import java.util.UUID

@MathDsl
@Suppress("FunctionName")
fun ReadFiscalYearsByProvider(): KlAction<Result<Contextual<String>>, Result<FiscalYears>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val provider = UUID.fromString(contextual.data)
                readFiscalYears(provider).toApiType()
            }
        } x database
    }
}
