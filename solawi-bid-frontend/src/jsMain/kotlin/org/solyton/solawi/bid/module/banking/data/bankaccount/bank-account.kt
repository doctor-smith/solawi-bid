package org.solyton.solawi.bid.module.banking.data.bankaccount

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite

@Lensify
data class BankAccount(
    @ReadOnly val bankAccountId: String,
    @ReadWrite val userId: String,
    @ReadWrite val iban: String,
    @ReadWrite val bic: String
)
