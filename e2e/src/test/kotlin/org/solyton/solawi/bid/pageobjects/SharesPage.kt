package org.solyton.solawi.bid.pageobjects

import CHECK_FALSE
import CHECK_TRUE
import checkIcon
import clickOn
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import getByDataId
import getByPartialDataId
import put

/**
 * Page object for the Share Management application's organization page
 * (`/app/management/organizations/{organizationId}/share_management`), covering share
 * type/offer/subscription creation and the "Bulk Edit" status-change dialog.
 */
class SharesPage(private val page: Page) {

    /**
     * Creates a share type with the given [name]/[key]/[description].
     * The key must have length <= 20.
     */
    fun createShareType(name: String, key: String, description: String) {
        page.clickOn("shares.page.share-type.chevron")
        page.clickOn("shares.page.share-type.create-button")

        val modal = page.getByDataId("shares.modal.upsert-share-type.modal")
        assertThat(modal).isVisible()

        page.put("shares.modal.upsert-share-type.name-input", name)
        page.put("shares.modal.upsert-share-type.key-input", key)
        page.put("shares.modal.upsert-share-type.description-input", description)

        // page.shot("dev/share-type-filled")

        page.clickOn("shares.modal.upsert-share-type.modal.submit-button")
        assertThat(modal).not().isVisible()
    }

    /**
     * Creates a fixed-price share offer for [shareTypeName] on the fiscal year named
     * [fiscalYearKey] (its `FiscalYear.format()`, e.g. "2026"), with the given [price].
     * [ahcAuthorizationRequired] controls whether a SEPA payment-mandate authorization is
     * required before the resulting subscription can become `Subscribed` (workflow B2),
     * or not (workflow B1).
     */
    fun createShareOffer(
        fiscalYearKey: String,
        shareTypeName: String,
        price: String,
        ahcAuthorizationRequired: Boolean = false
    ) {
        page.clickOn("shares.page.share-offer.chevron")
        page.clickOn("shares.page.share-offer.create-button")

        val modal = page.getByDataId("shares.modal.upsert-share-offer.modal")
        assertThat(modal).isVisible()

        page.clickOn("shares.modal.upsert-share-offer.fiscal-year-dropdown")
        page.clickOn("shares.modal.upsert-share-offer.fiscal-year-dropdown.option.$fiscalYearKey")

        page.clickOn("shares.modal.upsert-share-offer.share-type-dropdown")
        page.clickOn("shares.modal.upsert-share-offer.share-type-dropdown.option.$shareTypeName")

        page.clickOn("shares.modal.upsert-share-offer.pricing-type-dropdown")
        page.clickOn("shares.modal.upsert-share-offer.pricing-type-dropdown.option.FIXED")

        page.put("shares.modal.upsert-share-offer.price-input", price)

        val ahcOptionKey = if (ahcAuthorizationRequired) CHECK_TRUE else CHECK_FALSE
        page.clickOn("shares.modal.upsert-share-offer.ahc-authorization-required-dropdown")
        page.clickOn("shares.modal.upsert-share-offer.ahc-authorization-required-dropdown.option.$ahcOptionKey")

        page.clickOn("shares.modal.upsert-share-offer.modal.submit-button")

        assertThat(modal).not().isVisible()
    }

    /**
     * Attaches a SEPA collection to the (single) existing share offer, selecting the
     * creditor bank account by its `"<holder> / <description>"` dropdown key.
     */
    fun attachSepaCollectionToOffer(
        creditorAccountHolder: String,
        creditorAccountDescription: String,
        collectionKey: String,
        mandateReferencePrefix: String,
        remittanceInformation: String
    ) {
        val sepaCollectionButton = page.locator(
            "[data-id^='shares.page.share-offer.'][data-id\$='.sepa-collection-button']"
        ).first()
        assertThat(sepaCollectionButton).isVisible()
        sepaCollectionButton.click()

        page.clickOn("sepa-collection-form.bank-account-dropdown")
        page.clickOn("sepa-collection-form.bank-account-dropdown.option.$creditorAccountHolder / $creditorAccountDescription")

        page.locator("#collection-key-input").fill(collectionKey)
        page.locator("#mandate-reference-prefix").fill(mandateReferencePrefix)
        page.locator("#remittance-information").fill(remittanceInformation)

        page.clickOn("null.modal.submit-button")
    }

    /**
     * Attaches/creates a SEPA mandate for the (single) existing share subscription. The
     * "Upsert" dialog pre-fills every field with sensible defaults (debtor name, the
     * only available SEPA collection, today's date, status ACTIVE), but its internal
     * "selected mandate" state stays null - and submit silently no-ops - until at least
     * one field's change callback has fired, so the status dropdown is re-selected to
     * its own current value to trigger it.
     */
    fun attachSepaMandateToSubscription() {
        val sepaMandateButton = page.getByPartialDataId(".attach-or-upsert-sepa-mandate-button")
            /*
            page.locator(
            "[data-id^='shares.page.share-subscription.'][data-id\$='.sepa-mandate-button']"
        ).first()

             */
        assertThat(sepaMandateButton).isEnabled()
        sepaMandateButton.click()

        page.clickOn("sepa-mandate-form.status-dropdown")
        page.clickOn("sepa-mandate-form.status-dropdown.option.ACTIVE")


        page.clickOn("shares.modal.attach-or-upsert-sepa-mandate.modal.submit-button")
    }

    /**
     * Creates a subscription for [username] to the share offer keyed by
     * `"$shareTypeName - $fiscalYearKey"`, at the given [distributionPointName], with
     * [price] and [numberOfShares]. The fiscal year, SEPA-authorization and initial
     * status (PendingActivation) are left at their defaults.
     */
    fun createShareSubscription(
        username: String,
        fullName: String,
        fiscalYearKey: String,
        shareTypeName: String,
        distributionPointName: String,
        price: String,
        numberOfShares: String,
        ahcAuthorized: Boolean,
        coSubscribers: List<String> = emptyList()
    ) {
        page.clickOn("shares.page.share-subscription.chevron")
        page.clickOn("shares.page.share-subscription.create-button")

        val modal = page.getByDataId("shares.modal.upsert-share-subscription.modal")
        assertThat(modal).isVisible()

        val selectedUser = page.getByDataId("shares.modal.upsert-share-subscription.user-display-input")
        val userSelectionButton = page.getByDataId("shares.modal.upsert-share-subscription.user.$username.select-button")
        userSelectionButton.apply {
            assertThat(this).isVisible()
            assertThat(this).isEnabled()
            click()
        }
        assertThat(selectedUser).hasValue(fullName)

        page.clickOn("shares.modal.upsert-share-subscription.fiscal-year-dropdown")
        page.clickOn("shares.modal.upsert-share-subscription.fiscal-year-dropdown.option.$fiscalYearKey")

        page.clickOn("shares.modal.upsert-share-subscription.distribution-point-dropdown")
        page.clickOn("shares.modal.upsert-share-subscription.distribution-point-dropdown.option.$distributionPointName")

        page.clickOn("shares.modal.upsert-share-subscription.share-offer-dropdown")
        page.clickOn("shares.modal.upsert-share-subscription.share-offer-dropdown.option.$shareTypeName - $fiscalYearKey")

        page.put("shares.modal.upsert-share-subscription.price-per-share-input", price)
        assertThat(
            page.getByDataId("shares.modal.upsert-share-subscription.price-per-share-input")
        ).hasValue(price)

        page.put("shares.modal.upsert-share-subscription.number-of-shares-input", numberOfShares)
        assertThat(
            page.getByDataId("shares.modal.upsert-share-subscription.number-of-shares-input")
        ).hasValue(numberOfShares)

        page.clickOn("shares.modal.upsert-share-subscription.ahc-authorized-dropdown")
        page.clickOn("shares.modal.upsert-share-subscription.ahc-authorized-dropdown.option.${ahcAuthorized.checkIcon()}")

        page.put(
            "shares.modal.upsert-share-subscription.co-subscribers-textarea",
            coSubscribers.joinToString("\n") { it }
        )

        val submitButton = page.getByDataId("shares.modal.upsert-share-subscription.modal.submit-button")

        submitButton.apply {
            assertThat(this).isEnabled()
            click()
        }
        assertThat(modal).not().isVisible()
    }

    /**
     * Approves the (single, currently selected-by-default-none) subscription via the
     * "Bulk Edit" dialog, changing its status to [targetStatus] (e.g. "SUBSCRIBED").
     * Only safe for PROVIDER-permitted transitions: the dialog hardcodes
     * `ChangedBy.PROVIDER` and will throw a client-side error for USER-only
     * transitions (see progress doc).
     */
    fun changeStatusViaBulkEdit(targetStatus: String) {
        page.clickOn("shares.page.share-subscription.select-all-checkbox")
        page.clickOn("shares.page.share-subscription.bulk-edit-button")

        page.clickOn("shares.modal.bulk-edit.change-status-checkbox")
        page.clickOn("shares.modal.bulk-edit.status-dropdown")
        page.clickOn("shares.modal.bulk-edit.status-dropdown.option.$targetStatus")

        val submitButton = page.getByDataId("shares.modal.bulk-edit.modal.submit-button")
        submitButton.apply {
            assertThat(this).isEnabled()
            click()
        }

        // page.shot("dev/confirm-bulk-edit-share-subscriptions.png")

        // A second, generic confirmation dialog ("Are you sure...?") is the one that
        // actually dispatches the status change.
        page.clickOn("null.modal.submit-button")
    }
}
