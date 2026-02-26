package org.solyton.solawi.bid.module.banking.action

import org.evoleq.optics.storage.ActionEnvelope
import org.solyton.solawi.bid.module.banking.data.api.ApiBankAccount
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount

fun upsertBankAccount(givenBankAccount: BankAccount?, bankAccount: BankAccount): ActionEnvelope<BankingApplication, Any, ApiBankAccount> =
    when(givenBankAccount) {
        null  -> ActionEnvelope(
            action = createBankAccount(
                bankAccount.userId,
                bankAccount.iban,
                bankAccount.bic
            ),
            id = CREATE_BANK_ACCOUNT,
        )
        else -> ActionEnvelope(
            action = updateBankAccount(
                givenBankAccount.bankAccountId,
                givenBankAccount.userId,
                givenBankAccount.iban,
                givenBankAccount.bic
            ),
            id = UPDATE_BANK_ACCOUNT,
            run = bankAccount != givenBankAccount
        )
    }
