package org.solyton.solawi.bid.module.banking.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.upsertAll
import org.solyton.solawi.bid.module.banking.data.api.ApiBankAccounts
import org.solyton.solawi.bid.module.banking.data.api.ReadPersonalBankAccounts
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.bankAccounts
import org.solyton.solawi.bid.module.banking.data.toDomainType


const val READ_PERSONAL_BANK_ACCOUNTS = "READ_PERSONAL_BANK_ACCOUNTS"

/**
 * Reads the personal bank accounts associated with the current user and updates the application state.
 */
fun readPersonalBankAccounts(nameSuffix: String? = null) = Action<BankingApplication, ReadPersonalBankAccounts, ApiBankAccounts>(
    name = READ_PERSONAL_BANK_ACCOUNTS.suffixed(nameSuffix),
    reader = {_ -> ReadPersonalBankAccounts()},
    endPoint = ReadPersonalBankAccounts::class,
    writer = bankAccounts.upsertAll {
            p, q  -> p.bankAccountId == q.bankAccountId
    } contraMap { bankAccounts -> bankAccounts.toDomainType() }
)
