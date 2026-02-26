package org.solyton.solawi.bid.application.data.transform.banking

import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.ActionDispatcher
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.banking.data.BIC
import org.solyton.solawi.bid.module.banking.data.BankAccountId
import org.solyton.solawi.bid.module.banking.data.IBAN
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.bankAccounts
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.environment.Environment
import org.solyton.solawi.bid.module.values.UserId
import org.solyton.solawi.bid.test.UUID_1
import kotlin.test.Test
import kotlin.test.assertEquals

class BankingApplicationIsoTest {

    @Test fun getterTest() {
        val application = Application(
            org.solyton.solawi.bid.application.data.env.Environment()
        )
        val bankApplication = BankingApplication(
            Environment(),
            ActionDispatcher {  }, bankAccounts = listOf())
        val iso = bankingApplicationIso


        assertEquals(bankApplication.bankAccounts, iso.get(application).bankAccounts)
        assertEquals(bankApplication.fiscalYears, iso.get(application).fiscalYears)
        assertEquals(bankApplication.environment, iso.get(application).environment)
        assertEquals(bankApplication.deviceData, iso.get(application).deviceData)
        assertEquals(bankApplication.i18N, iso.get(application).i18N)

    }

    @Test fun setterTest() {
        val application = Application(
            org.solyton.solawi.bid.application.data.env.Environment()
        )
        val bankApplication = BankingApplication(
            Environment(),
            ActionDispatcher {  }, bankAccounts = listOf())
        val iso = bankingApplicationIso

        val bic = BIC("DEUTDEFF500")
        val iban = IBAN("DE89370400440532013000")
        val account = BankAccount(
            BankAccountId(UUID_1),
            UserId(UUID_1),
            iban,
            bic
        )

        val updatedApplication = (iso * bankAccounts).set(listOf(account))(application)
        assertEquals(listOf(account), (iso * bankAccounts).get(updatedApplication))
    }
}
