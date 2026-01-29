package org.solyton.solawi.bid.module.banking.data.application

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.fiscalyear.FiscalYear

@Lensify
data class BankingApplication(
    @ReadWrite val bankAccounts: List<BankAccount> = emptyList(),
    @ReadWrite val fiscalYears: List<FiscalYear> = emptyList()
)
