package org.solyton.solawi.bid.module.banking.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.update
import org.solyton.solawi.bid.module.banking.data.api.ApiBankAccount
import org.solyton.solawi.bid.module.banking.data.api.UpdateBankAccount
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.bankAccounts
import org.solyton.solawi.bid.module.banking.data.toDomainType


/**
 * Updates an existing bank account in the banking application.
 *
 * @param bankAccountId The unique identifier of the bank account to be updated.
 * @param iban The International Bank Account Number (IBAN) of the bank account.
 * @param bic The Bank Identifier Code (BIC) of the bank account.
 * @param nameSuffix Optional suffix to append to the action name for identification purposes. Defaults to an empty string.
 * @return An `Action` object that performs the update operation within the `BankingApplication` context, modifying the state with the provided bank account details.
 */
fun updateBankAccount(
    bankAccountId: String,

    userId: String,
    iban: String,
    bic: String,
    nameSuffix: String = ""
): Action<BankingApplication, UpdateBankAccount, ApiBankAccount> = Action(
    name = "UpdateBankAccount$nameSuffix",
    reader = { _ -> UpdateBankAccount(
        bankAccountId,
        userId,
        bic,
        iban,
    ) },
    endPoint = UpdateBankAccount::class,
    writer = bankAccounts.update{
            p, q -> p.bankAccountId == q.bankAccountId
    } contraMap {
            fY: ApiBankAccount -> fY.toDomainType()
    }
)
