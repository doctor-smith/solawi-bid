package org.solyton.solawi.bid.module.banking.action

import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.removeWhen
import org.solyton.solawi.bid.module.banking.data.BankAccountId
import org.solyton.solawi.bid.module.banking.data.api.DeleteBankAccount
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.bankAccounts

fun deleteBankAccount(
    bankAccountId: BankAccountId,
    nameSuffix: String = ""
): Action<BankingApplication, DeleteBankAccount, Boolean> = Action(
    name = CREATE_BANK_ACCOUNT.suffixed(nameSuffix),
    reader = {_ -> DeleteBankAccount(bankAccountId) },
    endPoint = DeleteBankAccount::class,
    writer = bankAccounts.removeWhen { bankAccount -> bankAccount.bankAccountId == bankAccountId }
)
