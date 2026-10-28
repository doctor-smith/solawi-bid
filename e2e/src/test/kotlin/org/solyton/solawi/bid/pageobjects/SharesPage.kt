package org.solyton.solawi.bid.pageobjects

import CHECK_FALSE
import CHECK_TRUE
import checkIcon
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.application.api.solawiApi
import org.solyton.solawi.bid.module.banking.data.api.*
import org.solyton.solawi.bid.module.shares.data.api.*

/**
 * Page object for the Share Management application's organization page
 * (`/app/management/organizations/{organizationId}/share_management`), covering share
 * type/offer/subscription creation and the "Bulk Edit" status-change dialog.
 */
class SharesPage(private val page: Page) {

    class ShareTypeManagement(override val locator: Locator) : DefaultTestAccordion(locator) {
        companion object {
            const val BASE_ID = "shares.page.share-type"
        }


        override fun trigger(): Locator = locator.getByDataId("$BASE_ID.chevron")

        fun createNewShareTypeButton(): Locator = locator.getByDataId("$BASE_ID.create-button")

    }

    class ShareOfferManagement(override val locator: Locator) : DefaultTestAccordion(locator) {
        companion object {
            const val BASE_ID = "shares.page.share-offer"
        }


        override fun trigger(): Locator = locator.getByDataId("$BASE_ID.chevron")

        fun createNewShareOfferButton(): Locator = locator.getByDataId("$BASE_ID.create-button")
    }

    class ShareSubscriptionManagement(override val locator: Locator) : DefaultTestAccordion(locator) {

        companion object {
            const val BASE_ID = "shares.page.share-subscription"
        }

        override fun trigger(): Locator = locator.getByDataId("$BASE_ID.chevron")


        fun createNewShareSubscriptionButton(): Locator = locator.getByDataId("$BASE_ID.create-button")

        fun selectAllButton(): Locator = locator.getByDataId("$BASE_ID.select-all-checkbox")
        fun deselectAll() {
            if (selectAllButton().isChecked) {
                selectAllButton().click()
                assertThat(selectAllButton()).not().isChecked()
            }
        }
        fun selectAll() {
            deselectAll()
            selectAllButton().click()
            assertThat(selectAllButton()).isChecked()
        }

        class ListOfShareSubscriptions(
            override val locator: Locator
        ): TestComponent(locator) {
            fun item(shareSubscriptionId: String): Locator = locator.getByDataId("$BASE_ID.list.item.$shareSubscriptionId")
        }

        class StatusFilter(
            override val locator: Locator
        ) : DefaultTestDropdown(
            locator,
            with(locator) { mapOf(
                option("All Statuses"),
                option("PENDING_ACTIVATION"),
                option("CLEARED_FOR_AUCTION"),
                option("SUBSCRIBED"),
                option("SUSPENDED"),
                option("AWAITING_AHC_AUTHORIZATION"),
                option("ACTIVATION_REJECTED"),
                option("CANCELLED"),
                option("ROLLING_OVER"),
                option("ROLLED_OVER"),
            ) }
        )

        class AhcAuthFilter(
            override val locator: Locator
        ) : DefaultTestDropdown(
            locator,
            with(locator) { mapOf(
                option("All Authorized"),
                option(CHECK_FALSE),
                option(CHECK_TRUE),
            ) }
        )

        class BulkEditModal(
            override val locator: Locator,
            override val trigger: Locator,
        ) : TestModal(
            locator, trigger
        ) {
            companion object {
                const val BASE_ID = "shares.modal.bulk-edit"
            }

            override fun submitButton(): Locator = locator.getByDataId("$BASE_ID.modal.submit-button")
            override fun cancelButton(): Locator {
                TODO("Not yet implemented")
            }

            override fun closeButton(): Locator {
                TODO("Not yet implemented")
            }

            fun changeStatusCheckbox(): Locator = locator.getByDataId("$BASE_ID.change-status-checkbox")
            fun statusDropdown(): Locator = locator.getByDataId("$BASE_ID.status-dropdown")
            fun statusOption(status: String): Locator = locator.getByDataId("$BASE_ID.status-dropdown.option.$status")
            fun rolloverCheckbox(): Locator = locator.getByDataId("$BASE_ID.rollover-checkbox")
            fun rolloverShareOfferDropdown(): Locator = locator.getByDataId("$BASE_ID.rollover-share-offer-dropdown")
            fun rolloverShareOfferOption(optionText: String): Locator = locator.getByDataId("$BASE_ID.rollover-share-offer-dropdown.option.$optionText")
        }

        fun BulkEditModal(page: Page): BulkEditModal =
            BulkEditModal(
                page.getByDataId("${BulkEditModal.BASE_ID}.modal"),
                page.getByDataId("${BASE_ID}.bulk-edit-button")
            )
    }

    // API

    @Suppress("FunctionName")
    fun ShareTypeManagement(): ShareTypeManagement =
        ShareTypeManagement(page.getByDataId(ShareTypeManagement.BASE_ID))

    @Suppress("FunctionName")
    fun ShareOfferManagement(): ShareOfferManagement =
        ShareOfferManagement(page.getByDataId(ShareOfferManagement.BASE_ID))

    @Suppress("FunctionName")
    fun ShareSubscriptionManagement(): ShareSubscriptionManagement =
        ShareSubscriptionManagement(page.getByDataId(ShareSubscriptionManagement.BASE_ID))


    /**
     * Creates a share type with the given [name]/[key]/[description].
     * The key must have length <= 20.
     */
    fun createShareType(name: String, key: String, description: String): ShareType {
        with(ShareTypeManagement()) {
            open()
            createNewShareTypeButton().click()
        }
        // page.clickOn("shares.page.share-type.chevron")
        // page.clickOn("shares.page.share-type.create-button")

        val modal = page.getByDataId("shares.modal.upsert-share-type.modal")
        assertThat(modal).isVisible()

        page.put("shares.modal.upsert-share-type.name-input", name)
        page.put("shares.modal.upsert-share-type.key-input", key)
        page.put("shares.modal.upsert-share-type.description-input", description)

        // page.shot("dev/share-type-filled")

        val shareType = page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = CreateShareType::class
        ) {
            page.clickOn("shares.modal.upsert-share-type.modal.submit-button")
        }.decodeBodyTo<ShareType>()


        assertThat(modal).not().isVisible()

        return shareType
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
    ) : ApiShareOffer = with(ShareOfferManagement()) {
        assertThat(this).isVisible()
        if(!isOpened()) open()
        assertThat(createNewShareOfferButton()).isVisible()
        createNewShareOfferButton().click()

        // page.clickOn("shares.page.share-offer.chevron")
        // page.clickOn("shares.page.share-offer.create-button")

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


        val shareOffer = page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = CreateShareOffer::class
        ) {
            page.clickOn("shares.modal.upsert-share-offer.modal.submit-button")
        }.decodeBodyTo<ApiShareOffer>()

        assertThat(modal).not().isVisible()

        return shareOffer
    }

    /**
     * Attaches a SEPA collection to the (single) existing share offer, selecting the
     * creditor bank account by its `"<holder> / <description>"` dropdown key.
     */
    fun attachSepaCollectionToOffer(
        shareOffer: ApiShareOffer,
        creditorAccountHolder: String,
        creditorAccountDescription: String,
        collectionKey: String,
        mandateReferencePrefix: String,
        remittanceInformation: String
    ): SepaCollection = with(ShareOfferManagement()){
        assertThat(this).isVisible()
        if(!isOpened()) open()

        val sepaCollectionButton = page.getByDataId("shares.page.share-offer.${shareOffer.id}.sepa-collection-button")

        assertThat(sepaCollectionButton).isVisible()
        sepaCollectionButton.click()

        page.clickOn("sepa-collection-form.bank-account-dropdown")
        page.clickOn("sepa-collection-form.bank-account-dropdown.option.$creditorAccountHolder / $creditorAccountDescription")

        page.locator("#collection-key-input").fill(collectionKey)
        page.locator("#mandate-reference-prefix").fill(mandateReferencePrefix)
        page.locator("#remittance-information").fill(remittanceInformation)

        // page.shot("dev/attach-collection-$collectionKey.png")

        val submitButton = page.getByDataId("null.modal.submit-button")
        assertThat(submitButton).isEnabled()
        val collection = page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = CreateSepaCollection::class
        ) {
            submitButton.click()
        }.decodeBodyTo<ApiSepaCollection>()

        return collection
    }

    /**
     * Attaches/creates a SEPA mandate for the (single) existing share subscription. The
     * "Upsert" dialog pre-fills every field with sensible defaults (debtor name, the
     * only available SEPA collection, today's date, status ACTIVE), but its internal
     * "selected mandate" state stays null - and submit silently no-ops - until at least
     * one field's change callback has fired, so the status dropdown is re-selected to
     * its own current value to trigger it.
     */
    fun attachSepaMandateToSubscription(): ApiSepaMandate = with(ShareSubscriptionManagement()) {
        open()
        val statusFilter = ShareSubscriptionManagement.StatusFilter(
            locator = locator.getByDataId("shares.page.share-subscription.status-dropdown.wrapper")
        )
        if (statusFilter.locator.isVisible) {
            statusFilter.select("All Statuses")
            page.waitForLoadingToFinish()
        }

        val sepaMandateButton = page.getByPartialDataId(".attach-or-upsert-sepa-mandate-button").first()
        assertThat(sepaMandateButton).isEnabled()
        sepaMandateButton.click()

        page.clickOn("sepa-mandate-form.status-dropdown")
        page.clickOn("sepa-mandate-form.status-dropdown.option.ACTIVE")

        val sepaMandate = page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = CreateSepaMandate::class
        ) {
            page.clickOn("shares.modal.attach-or-upsert-sepa-mandate.modal.submit-button")
        }.decodeBodyTo<ApiSepaMandate>()

        return sepaMandate
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
    ): ShareSubscription {
        with(ShareSubscriptionManagement()) {
            open()
            createNewShareSubscriptionButton().click()
        }
        // page.clickOn("shares.page.share-subscription.chevron")
        // page.clickOn("shares.page.share-subscription.create-button")

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
        assertThat(submitButton).isEnabled()

        val subscription = page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = CreateShareSubscription::class
        ) {
            submitButton.click()
        }.decodeBodyTo<ShareSubscription>()

        assertThat(modal).not().isVisible()

        return subscription
    }

    /**
     * Approves the (single, currently selected-by-default-none) subscription via the
     * "Bulk Edit" dialog, changing its status to [targetStatus] (e.g. "SUBSCRIBED").
     * Only safe for PROVIDER-permitted transitions: the dialog hardcodes
     * `ChangedBy.PROVIDER` and will throw a client-side error for USER-only
     * transitions (see progress doc).
     */
    fun changeStatusViaBulkEdit(targetStatus: String): ApiShareSubscriptions = with(ShareSubscriptionManagement()){
        open()
        assertThat(selectAllButton()).isEnabled()

        selectAll()

        val bulkModal = BulkEditModal(page)
        assertThat(bulkModal.trigger).isEnabled()

        bulkModal.open()
        assertThat(bulkModal).isVisible()
        bulkModal.changeStatusCheckbox().click()
        bulkModal.statusDropdown().click()
        bulkModal.statusOption(targetStatus).click()


        val submitButton = bulkModal.submitButton()
        assertThat(submitButton).isEnabled()

        page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = UpdateShareStatuses::class,
            options = Page.WaitForResponseOptions().setTimeout(45_000.0)
        ) {
            submitButton.click()

            assertThat(bulkModal).not().isVisible()

            // A second, generic confirmation dialog ("Are you sure...?") is the one that
            // actually dispatches the status change.
            page.clickOn("null.modal.submit-button")
            assertThat(page.getByDataId("null.modal")).not().isVisible()
        }.decodeBodyTo<ApiShareSubscriptions>()
    }

    /**
     * Performs a rollover for the selected subscription(s) to the target share offer specified
     * by [targetOfferOptionText] (format: `"<FY-format> - <ShareTypeName>"`).
     */
    fun rolloverViaBulkEdit(targetOfferOptionText: String): ApiShareSubscriptions = with(ShareSubscriptionManagement()) {
        open()
        assertThat(selectAllButton()).isEnabled()

        selectAll()

        val bulkModal = BulkEditModal(page)
        assertThat(bulkModal.trigger).isEnabled()

        bulkModal.open()
        assertThat(bulkModal).isVisible()

        bulkModal.rolloverCheckbox().click()
        bulkModal.rolloverShareOfferDropdown().click()
        bulkModal.rolloverShareOfferOption(targetOfferOptionText).click()

        val submitButton = bulkModal.submitButton()
        assertThat(submitButton).isEnabled()

        val shareSubscriptions = page.waitForApi(
            api = solawiApi,
            options = Page.WaitForResponseOptions().setTimeout(45_000.0),
            keyOfEndpoint = RolloverShareSubscriptions::class,
        ){
            submitButton.click()

            assertThat(bulkModal).not().isVisible()

            // Confirmation dialog
            val confirmationButton = page.getByDataId("null.modal.submit-button")
            assertThat(confirmationButton).isVisible()
            confirmationButton.click()
            assertThat(page.getByDataId("null.modal")).not().isVisible()
        }.decodeBodyTo<ApiShareSubscriptions>()

        return shareSubscriptions
    }
}
