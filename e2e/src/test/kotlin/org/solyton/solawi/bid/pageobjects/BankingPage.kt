package org.solyton.solawi.bid.pageobjects

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.application.api.solawiApi
import org.solyton.solawi.bid.module.banking.data.api.*

/**
 * Page object for the Banking application's organization page
 * (`/app/management/organizations/{organizationId}/banking`), covering legal-entity +
 * creditor-id setup and fiscal-year creation.
 */
class BankingPage(private val page: Page) {

    class LegalEntityManagement(override val locator: Locator): DefaultTestAccordion(locator) {

        companion object {
            const val BASE_ID = "banking.page.legal-entity"
        }

        override fun trigger(): Locator {
            return locator.getByDataId("${BASE_ID}.chevron")
        }
    }

    class CreditorBankAccountsManagement(override val locator: Locator): DefaultTestAccordion(locator) {

        companion object {
            const val BASE_ID = "banking.page.creditor-bank-accounts"
        }

        override fun trigger(): Locator {
            return locator.getByDataId("${BASE_ID}.chevron")
        }
    }

    class CustomerBankAccountsManagement(override val locator: Locator): DefaultTestAccordion(locator) {

        companion object {
            const val BASE_ID = "banking.page.customer-bank-accounts"
        }

        override fun trigger(): Locator {
            return locator.getByDataId("${BASE_ID}.chevron")
        }
    }

    class FiscalYearsManagement(override val locator: Locator): DefaultTestAccordion(locator) {

        companion object {
            const val BASE_ID = "banking.page.fiscal-years"
        }

        override fun trigger(): Locator {
            return locator.getByDataId("${BASE_ID}.chevron")
        }

    }

    class SepaManagement(override val locator: Locator): DefaultTestAccordion(locator) {

        companion object {
            const val BASE_ID = "banking.page.sepa-collections"
        }

        override fun trigger(): Locator {
            return locator.getByDataId("${BASE_ID}.chevron")
        }
    }

    //
    // API
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun LegalEntityManagement(): LegalEntityManagement =
        LegalEntityManagement(page.getByDataId(LegalEntityManagement.BASE_ID))


    fun CreditorBankAccountsManagement(): CreditorBankAccountsManagement =
        CreditorBankAccountsManagement(page.getByDataId(CreditorBankAccountsManagement.BASE_ID))

    fun CustomerBankAccountsManagement(): CustomerBankAccountsManagement =
        CustomerBankAccountsManagement(page.getByDataId(CustomerBankAccountsManagement.BASE_ID))

    fun FiscalYearsManagement(): FiscalYearsManagement =
        FiscalYearsManagement(page.getByDataId(FiscalYearsManagement.BASE_ID))

    fun SepaManagement(): SepaManagement =
        SepaManagement(page.getByDataId(SepaManagement.BASE_ID))

    /**
     * Creates a legal entity (type HUMAN) for the organization and, once it exists,
     * attaches a creditor identifier (the "Gläubiger-ID") to it - a two-step UI flow,
     * since the "create" modal only sets up the legal entity itself, while the creditor
     * id can only be set afterward via "edit" (confirmed to be the only supported
     * production flow). Both the legal entity and a creditor id are required before
     * SEPA-related Banking operations (e.g. fiscal years) are permitted for the
     * organization.
     */
    fun createLegalEntityWithCreditorId(
        name: String,
        legalForm: String,
        creditorId: String
    ): ApiLegalEntity = with(LegalEntityManagement()){
        open()

        page.clickOn("banking.page.legal-entity.create-button")

        // basic data
        page.locator("#name").fill(name)
        page.locator("#legal-form").fill(legalForm)
        page.clickOn("legal-entity-form.legal-entity-type-dropdown")
        // The dropdown's options are keyed by their displayed German label, not the enum
        // value (e.g. "Natürliche Person" -> LegalEntityType.HUMAN).
        page.clickOn("legal-entity-form.legal-entity-type-dropdown.option.Natürliche Person")

        // Deliberately NOT setting the creditor id here: the "create" dialog's onOk
        // handler only dispatches legal-entity creation, not creditor-identifier
        // creation (and doing so here would use a not-yet-known legal entity id). The
        // real app flow is to create the legal entity first, then set the creditor id
        // afterward via "edit", which is what the second step below does.

        // address
        page.locator("#recipientName").fill("Recipient-$name")
        page.locator("#organizationName").fill(name)
        page.locator("#addressLine1").fill("line 1")
        page.locator("#addressLine2").fill("line 2")
        page.locator("#postalCode").fill("12345")

        page.inputText("#city", "city")

        page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = CreateLegalEntity::class
        ) {
            page.clickOn("banking.modal.upsert-legal-entity.modal.submit-button")
        }.decodeBodyTo<ApiLegalEntity>()

        // page.clickOn("banking.modal.upsert-legal-entity.modal.submit-button")

        page.waitForLoadingToFinish()
        page.waitForSelector("text=$name")

        // Step 2: now that the legal entity exists, re-open the form via the "edit"
        // button and set the creditor id there, matching the only supported production
        // flow.
        page.clickOn("banking.page.legal-entity.edit-button")

        page.locator("#creditor-id").fill(creditorId)

        val legalEntity = page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = UpdateLegalEntity::class
        ) {
            page.clickOn("banking.modal.upsert-legal-entity.modal.submit-button")
        }.decodeBodyTo<ApiLegalEntity>()



        page.waitForLoadingToFinish()
        page.waitForSelector("text=$creditorId")

        legalEntity
    }

    /**
     * Creates a `CREDITOR` bank account for the organization itself, used as the
     * target account for SEPA collections attached to share offers.
     */
    fun createCreditorBankAccount(
        accountHolder: String,
        iban: String,
        bic: String,
        description: String
    ): ApiBankAccount = with(CreditorBankAccountsManagement()){
        open()
        page.clickOn("banking.page.creditor-bank-accounts.create-button")

        page.locator("#account-holder").fill(accountHolder)
        page.locator("#iban").fill(iban)
        page.locator("#bic").fill(bic)
        page.locator("#description").fill(description)

        // Account type defaults to DEBTOR - must explicitly switch to CREDITOR.
        // page.clickOn("bank-account-form.account-type-dropdown")
        // page.clickOn("bank-account-form.account-type-dropdown.option.CREDITOR")

        // isActive starts unset (null) until explicitly chosen once - required for submit.
        // page.clickOn("bank-account-form.is-active-dropdown")
        // page.clickOn("bank-account-form.is-active-dropdown.option.TRUE")

        val bankAccount = page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = CreateBankAccount::class
        ) {

            page.clickOn("banking.upsert-bank-account.modal.submit-button")
        }.decodeBodyTo<ApiBankAccount>()

        bankAccount
    }

    /**
     * Creates a `DEBTOR` bank account for [username] (the subscribing member), via the
     * customer bank-account card's user-search modal.
     */
    fun createDebtorBankAccount(
        username: String,
        accountHolder: String,
        iban: String,
        bic: String
    ): ApiBankAccount = with(CustomerBankAccountsManagement()){
        open()

        page.clickOn("banking.page.customer-bank-accounts.create-button")

        val modal = page.getByDataId("banking.upsert-bank-account.modal")
        assertThat(modal).isVisible()

        // fill search box
        page.getByDataId("banking.bank-account.upsert-with-user-search.form.search-input").fill(username)

        // find first matching element in the list and click it
        page.getByPartialDataId("banking.bank-account.upsert-with-user-search.form.search-list.select").first().click()


        // page.locator("span", Page.LocatorOptions().setHasText(username)).first().click()

        page.locator("#account-holder").fill(accountHolder)
        page.locator("#iban").fill(iban)
        page.locator("#bic").fill(bic)

        // Account type already defaults to DEBTOR, but isActive still needs an explicit pick.
        page.clickOn("bank-account-form.is-active-dropdown")
        page.clickOn("bank-account-form.is-active-dropdown.option.TRUE")

        val submitButton = page.getByDataId("banking.upsert-bank-account.modal.submit-button")
        assertThat(submitButton).isEnabled()


        val bankAccount = page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = CreateBankAccount::class
        ) {
            submitButton.click()
        }.decodeBodyTo<ApiBankAccount>()



        assertThat(modal).not().isVisible()

        bankAccount
    }

    /** Creates a fiscal year running from [start] to [end] (format `YYYY-MM-DD`). */
    fun createFiscalYear(start: String, end: String): ApiFiscalYear =
        with(FiscalYearsManagement()) {
            open()


            // page.clickOn("banking.page.fiscal-years.chevron")
            page.clickOn("banking.page.fiscal-years.create-button")

            val modal = page.getByDataId("banking.upsert-fiscal-years.modal")
            assertThat(modal).isVisible()

            val startDate = page.getByDataId("upsert-fiscal-year.form.input.date.start")
            val endDate = page.getByDataId("upsert-fiscal-year.form.input.date.end")
            val submitButton = page.getByDataId("banking.upsert-fiscal-years.modal.submit-button")

            startDate.apply {
                fill(start)
                assertThat(this).hasValue(start)
            }

            endDate.apply {
                fill(end)
                assertThat(this).hasValue(end)
            }

            assertThat(submitButton).isEnabled()

            val fiscalYear = page.waitForApi(
                api = solawiApi,
                keyOfEndpoint = CreateFiscalYear::class
            ) {
                submitButton.click()
            }.decodeBodyTo<ApiFiscalYear>()

            assertThat(modal).not().isVisible()

            fiscalYear
        }
}
