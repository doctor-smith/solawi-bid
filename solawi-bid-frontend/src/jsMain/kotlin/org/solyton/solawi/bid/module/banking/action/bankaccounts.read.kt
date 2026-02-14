package org.solyton.solawi.bid.module.banking.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.solyton.solawi.bid.module.banking.data.api.ApiBankAccounts
import org.solyton.solawi.bid.module.banking.data.api.ReadBankAccounts
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.bankAccounts
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.values.ProviderId

const val READ_BANK_ACCOUNTS = "ReadBankAccounts"

/**
 * Reads the bank accounts associated with a specific provider ID and updates the application state.
 *
 * @param providerId The identifier of the provider whose bank accounts are to be retrieved.
 * @param nameSuffix Optional suffix to append to the action name for identification purposes. Defaults to an empty string.
 */
fun readBankAccounts(providerId: ProviderId, nameSuffix: String = "") = Action<BankingApplication, ReadBankAccounts, ApiBankAccounts>(
    name = READ_BANK_ACCOUNTS.suffixed(nameSuffix),
    reader = {_ -> ReadBankAccounts(listOf("legal_entity" to providerId.value))},
    endPoint = ReadBankAccounts::class,
    writer = bankAccounts.set contraMap { bA -> bA.toDomainType() }
)
