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
import org.solyton.solawi.bid.module.banking.data.api.ImportBankAccounts
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.data.toUUID
import org.solyton.solawi.bid.module.banking.repository.importBankAccounts


@MathDsl
@Suppress("FunctionName")
fun ImportBankAccounts(): KlAction<Result<Contextual<ImportBankAccounts>>, Result<BankAccounts>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val bankAccounts = contextual.data.bankAccounts
                val accessorId = contextual.data.accessorId 
                importBankAccounts(
                    accessorId.toUUID(), 
                    bankAccounts
                ).toApiType()
            }
        } x database
    }
}
