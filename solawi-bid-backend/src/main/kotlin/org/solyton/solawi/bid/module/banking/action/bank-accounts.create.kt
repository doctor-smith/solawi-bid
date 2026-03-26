package org.solyton.solawi.bid.module.banking.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.language.of
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.banking.data.api.BankAccount
import org.solyton.solawi.bid.module.banking.data.api.CreateBankAccount
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.repository.createBankAccount
import org.solyton.solawi.bid.module.banking.schema.AccountType
import org.solyton.solawil.bid.module.bid.data.api.toUUID


@MathDsl
@Suppress("FunctionName")
fun CreateBankAccount(): KlAction<Result<Contextual<CreateBankAccount>>, Result<BankAccount>> = KlAction { result ->
    DbAction { database ->
        result bindSuspend { contextual ->
            resultTransaction(database) {
                val creator = contextual.userId
                val userId = contextual.data.userId
                val iban = contextual.data.iban
                val bic = contextual.data.bic
                val accountHolder = contextual.data.accountHolder
                val isActive = contextual.data.isActive
                val accountType = when (contextual.data.accessType) {
                    org.solyton.solawi.bid.module.banking.data.api.AccountType.CREDITOR -> AccountType.CREDITOR
                    org.solyton.solawi.bid.module.banking.data.api.AccountType.DEBTOR -> AccountType.DEBTOR
                }

                createBankAccount(
                    userId.toUUID(),
                    iban,
                    bic,
                    accountHolder.orEmpty(),
                    isActive,
                    accountType,
                    creator

                ).toApiType()
            }
        } x database
    }
}
