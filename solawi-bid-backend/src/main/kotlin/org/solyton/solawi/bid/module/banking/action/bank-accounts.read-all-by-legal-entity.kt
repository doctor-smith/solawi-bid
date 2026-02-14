package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.banking.data.api.BankAccounts
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.repository.readBankAccountsByLegalEntity
import java.util.*


@MathDsl
@Suppress("FunctionName")
fun ReadBankAccountsByLegalEntity(): KlAction<Result<Contextual<String>>, Result<BankAccounts>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                readBankAccountsByLegalEntity(UUID.fromString(contextual.data)).toApiType()
            }
        } x database
    }
}

