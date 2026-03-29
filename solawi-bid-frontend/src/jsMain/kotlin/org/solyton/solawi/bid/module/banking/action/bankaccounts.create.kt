package org.solyton.solawi.bid.module.banking.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.add
import org.solyton.solawi.bid.module.banking.data.BIC
import org.solyton.solawi.bid.module.banking.data.IBAN
import org.solyton.solawi.bid.module.banking.data.api.ApiBankAccount
import org.solyton.solawi.bid.module.banking.data.api.CreateBankAccount
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.bankAccounts
import org.solyton.solawi.bid.module.banking.data.bankaccount.AccountType
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.values.AccessorId
import org.solyton.solawi.bid.module.values.UserId

const val CREATE_BANK_ACCOUNT = "CreateBankAccount"

/**
 * Creates a bank account action for a specified user in the banking application.
 *
 * @param userId The identifier of the user for whom the bank account is being created.
 * @param iban The IBAN (International Bank Account Number) of the bank account.
 * @param bic The BIC (Bank Identifier Code) of the bank account.
 * @param nameSuffix Optional suffix to append to the action name for identification purposes. Defaults to an empty string.
 * @return An `Action` object that defines the creation process for the bank account within the `BankingApplication` context.
 */
fun createBankAccount(
    userId: UserId,
    iban: IBAN,
    bic: BIC,
    accountHolder: String = "",
    isActive: Boolean = true,
    accountType: AccountType = AccountType.DEBTOR,
    description: String? = null,
    accessors: List<AccessorId> = emptyList(),
    nameSuffix: String = ""
): Action<BankingApplication, CreateBankAccount, ApiBankAccount> = Action(
    name = CREATE_BANK_ACCOUNT.suffixed(nameSuffix),
    reader = {_ -> CreateBankAccount(
        userId,
        bic,
        iban,
        accountHolder,
        isActive,
        accountType.toApiType(),
        accessors,
        description,
    ) },
    endPoint = CreateBankAccount::class,
    writer = bankAccounts.add() contraMap {bankAccount -> bankAccount.toDomainType()}
)
