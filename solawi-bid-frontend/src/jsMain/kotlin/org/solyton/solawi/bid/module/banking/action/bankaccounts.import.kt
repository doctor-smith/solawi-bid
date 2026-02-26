package org.solyton.solawi.bid.module.banking.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.solyton.solawi.bid.module.banking.data.api.ApiBankAccounts
import org.solyton.solawi.bid.module.banking.data.api.ImportBankAccount
import org.solyton.solawi.bid.module.banking.data.api.ImportBankAccounts
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.bankAccounts
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.values.AccessorId

const val IMPORT_BANK_ACCOUNTS = "ImportBankAccounts"

/**
 * Imports a list of bank accounts into the banking application, with an optional override flag.
 *
 * @param override Specifies whether the imported bank accounts should override existing ones. Default is false.
 * @param accessorId The unique identifier used to access or associate the bank accounts.
 * @param bankAccountsToImport A list of `CreateBankAccount` objects representing the bank accounts to be imported.
 * @param nameSuffix An optional suffix to append to the name of the action for identification. Default is an empty string.
 * @return An `Action` configured to import the provided bank accounts into the `BankingApplication`.
 */
fun importBankAccounts(
    override: Boolean = false,
    accessorId: AccessorId,
    bankAccountsToImport: List<ImportBankAccount>,
    nameSuffix: String = ""
): Action<BankingApplication, ImportBankAccounts, ApiBankAccounts> = Action(
    name = IMPORT_BANK_ACCOUNTS.suffixed(nameSuffix),
    reader = {_: BankingApplication -> ImportBankAccounts(
        override = override,
        accessorId = accessorId,
        bankAccounts = bankAccountsToImport
    )},
    endPoint = ImportBankAccounts::class,
    writer = bankAccounts.set contraMap { apiBankAccounts -> apiBankAccounts.toDomainType() }
)
