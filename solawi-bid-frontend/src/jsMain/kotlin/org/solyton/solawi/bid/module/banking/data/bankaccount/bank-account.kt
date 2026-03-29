package org.solyton.solawi.bid.module.banking.data.bankaccount

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.evoleq.change.data.Change
import org.solyton.solawi.bid.module.banking.data.BIC
import org.solyton.solawi.bid.module.banking.data.BankAccountId
import org.solyton.solawi.bid.module.banking.data.IBAN
import org.solyton.solawi.bid.module.values.LegalEntityId
import org.solyton.solawi.bid.module.values.UserId

@Lensify
data class BankAccount(
    @ReadOnly val bankAccountId: BankAccountId,
    @ReadWrite val userId: UserId,
    @ReadWrite val iban: IBAN,
    @ReadWrite val bic: BIC,
    @ReadWrite val bankAccountHolder: String = "",
    @ReadWrite val isActive: Boolean = true,
    @ReadWrite val bankAccountType: AccountType = AccountType.DEBTOR,
    @ReadWrite val description: String? = null
)

data class BankAccountChange(
    val bankAccountId: BankAccountId,
    val legalEntityId: Change<LegalEntityId>,
    val iban: Change<String>,
    val bic: Change<String>,
    val bankAccountHolder: Change<String>,
    val isActive: Change<Boolean>,
    val bankAccountType: Change<AccountType>,
    val description: Change<String?>
)
