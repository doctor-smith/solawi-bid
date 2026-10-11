package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.uuid.toUuid
import org.solyton.solawi.bid.module.banking.data.api.FiscalYears
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.repository.readFiscalYears

@MathDsl
@Suppress("FunctionName")
fun ReadFiscalYearsByLegalEntity(): KlAction<Result<Contextual<List<String>>>, Result<FiscalYears>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val legalEntityIds = contextual.data.map{it.toUuid()}
                readFiscalYears(legalEntityIds).toApiType()
            }
        } x database
    }
}
