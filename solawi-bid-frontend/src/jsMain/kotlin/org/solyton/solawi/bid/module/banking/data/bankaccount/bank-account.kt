package org.solyton.solawi.bid.module.banking.data.bankaccount

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.banking.data.BIC
import org.solyton.solawi.bid.module.banking.data.BankAccountId
import org.solyton.solawi.bid.module.banking.data.IBAN
import org.solyton.solawi.bid.module.values.UserId

@Lensify
data class BankAccount(
    @ReadOnly val bankAccountId: BankAccountId,
    @ReadWrite val userId: UserId,
    @ReadWrite val iban: IBAN,
    @ReadWrite val bic: BIC
)
