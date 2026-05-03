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
import org.solyton.solawi.bid.module.banking.repository.readBankAccounts
import org.solyton.solawi.bid.module.values.UserId


@MathDsl
@Suppress("FunctionName")
fun ReadPersonalBankAccounts(): KlAction<Result<Contextual<String>>, Result<BankAccounts>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                readBankAccounts(UserId(contextual.userId.toString())).toApiType()
            }
        } x database
    }
}

