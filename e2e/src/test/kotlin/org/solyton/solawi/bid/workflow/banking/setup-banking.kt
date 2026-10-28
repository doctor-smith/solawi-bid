package org.solyton.solawi.bid.workflow.banking

import org.solyton.solawi.bid.TestUserContext
import org.solyton.solawi.bid.TestWorkflow
import org.solyton.solawi.bid.navigateToOrganizationApplicationPage
import org.solyton.solawi.bid.pageobjects.BankingPage
import org.solyton.solawi.bid.pageobjects.application.dashboard.DashboardPage
import org.solyton.solawi.bid.pageobjects.shared.NavBar
import org.solyton.solawi.bid.waitForLoadingToFinish

data class FiscalYearPeriod(
    val startDate: String,
    val endDate: String
)

data class CreditorBankAccountInput(
    val iban: String = "DE02100100100006820101",
    val bic: String = "PBNKDEFFXXX",
    val description: String = "e2e-creditor-account"
)

data class DebtorBankAccountInput(
    val iban: String = "DE89370400440532013000",
    val bic: String = "COBADEFFXXX",
    val accountHolder: String? = null
)

data class SetupBankingInput(
    val organizationName: String,
    val legalEntitySuffix: String = "e.V.",
    val creditorId: String = "DE98ZZZ09999999999",
    val fiscalYears: List<FiscalYearPeriod>,
    val creditorBankAccount: CreditorBankAccountInput? = CreditorBankAccountInput(),
    val debtorBankAccount: DebtorBankAccountInput? = DebtorBankAccountInput(),
    val debtorName: String? = null
)

class SetupBankingWorkflow(
    override val context: TestUserContext
) : TestWorkflow<SetupBankingInput, Unit> {
    override fun TestUserContext.runWith(input: SetupBankingInput) {
        val navBar = NavBar(page)
        val dashboardPage = DashboardPage(page)
        val bankingPage = BankingPage(page)

        navBar.moveToDashboard()
        dashboardPage.bankingOrganizationManagementLink().click()
        page.navigateToOrganizationApplicationPage("banking", input.organizationName, false)

        bankingPage.createLegalEntityWithCreditorId(input.organizationName, input.legalEntitySuffix, input.creditorId)
        waitForLoadingToFinish()

        input.fiscalYears.forEach { period ->
            bankingPage.createFiscalYear(period.startDate, period.endDate)
            waitForLoadingToFinish()
        }

        input.creditorBankAccount?.let { creditor ->
            bankingPage.createCreditorBankAccount(
                accountHolder = input.organizationName,
                iban = creditor.iban,
                bic = creditor.bic,
                description = creditor.description
            )
            waitForLoadingToFinish()
        }

        input.debtorBankAccount?.let { debtor ->
            val accountHolder = debtor.accountHolder ?: input.debtorName ?: user.username.value
            bankingPage.createDebtorBankAccount(
                username = user.username.value,
                accountHolder = accountHolder,
                iban = debtor.iban,
                bic = debtor.bic
            )
            waitForLoadingToFinish()
        }
    }
}
