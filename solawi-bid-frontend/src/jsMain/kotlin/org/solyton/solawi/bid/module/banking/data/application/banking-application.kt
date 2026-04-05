package org.solyton.solawi.bid.module.banking.data.application

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.evoleq.compose.modal.Modals
import org.evoleq.device.data.Device
import org.evoleq.optics.storage.ActionDispatcher
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.creditor.identifier.CreditorIdentifier
import org.solyton.solawi.bid.module.banking.data.environment.Environment
import org.solyton.solawi.bid.module.banking.data.fiscalyear.FiscalYear
import org.solyton.solawi.bid.module.banking.data.legalentity.LegalEntity
import org.solyton.solawi.bid.module.banking.data.user.User
import org.solyton.solawi.bid.module.i18n.data.I18N

@Lensify
data class BankingApplication(
    @ReadOnly val environment: Environment,
    @ReadOnly val actions: ActionDispatcher<BankingApplication>,
    @ReadWrite val modals: Modals<Int> = mapOf(),
    @ReadOnly val deviceData: Device = Device(),
    @ReadWrite val i18N: I18N = I18N(),
    @ReadWrite val user: User = User(),
    @ReadWrite val legalEntity: LegalEntity = LegalEntity.default,
    @ReadWrite val creditorIdentifier: CreditorIdentifier? = null,
    @ReadWrite val bankAccounts: List<BankAccount> = emptyList(),
    @ReadWrite val fiscalYears: List<FiscalYear> = emptyList()
)
