package org.solyton.solawi.bid.pageobjects

import clickOn
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import getByDataId
import inputText
import waitForLoadingToFinish

/**
 * Page object for the Banking application's organization page
 * (`/app/management/organizations/{organizationId}/banking`), covering legal-entity +
 * creditor-id setup and fiscal-year creation.
 */
class BankingPage(private val page: Page) {

    /**
     * Creates a legal entity (type HUMAN) for the organization and, once it exists,
     * attaches a creditor identifier (the "Gläubiger-ID") to it - a two-step UI flow,
     * since the "create" modal only sets up the legal entity itself, while the creditor
     * id can only be set afterward via "edit" (confirmed to be the only supported
     * production flow). Both the legal entity and a creditor id are required before
     * SEPA-related Banking operations (e.g. fiscal years) are permitted for the
     * organization.
     */
    fun createLegalEntityWithCreditorId(name: String, legalForm: String, creditorId: String) {
        page.evaluate("console.log('MARKER_BEFORE_CREATE_LEGAL_ENTITY_CLICK')")
        page.clickOn("banking.page.legal-entity.create-button")
        page.evaluate("console.log('MARKER_AFTER_CREATE_LEGAL_ENTITY_CLICK')")

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

        page.clickOn("banking.modal.upsert-legal-entity.modal.submit-button")

        page.waitForLoadingToFinish()
        page.waitForSelector("text=$name")

        // Step 2: now that the legal entity exists, re-open the form via the "edit"
        // button and set the creditor id there, matching the only supported production
        // flow.
        page.clickOn("banking.page.legal-entity.edit-button")

        page.locator("#creditor-id").fill(creditorId)

        page.clickOn("banking.modal.upsert-legal-entity.modal.submit-button")

        page.waitForLoadingToFinish()
        page.waitForSelector("text=$creditorId")
    }

    /** Creates a fiscal year running from [start] to [end] (format `YYYY-MM-DD`). */
    fun createFiscalYear(start: String, end: String) {
        page.clickOn("banking.page.fiscal-years.chevron")
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

        submitButton.apply {
            assertThat(this@apply).isEnabled()
            click()
        }

        assertThat(modal).not().isVisible()
    }
}
