package org.solyton.solawi.bid.module.banking.data.mappings

import org.evoleq.axioms.definition.Lensify
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.values.LegalEntityId
import org.solyton.solawi.bid.module.values.UserId

@Lensify
data class BankingMappings(
    val override: Boolean = false,
    val legalEntityId: LegalEntityId,
    val bankAccounts: Map<UserId, BankAccount>
)
