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

    class UpsertShareTypeModal(
        override val locator: Locator,
        override val trigger: Locator,
    ) : TestModal(locator, trigger) {
        companion object {
            const val BASE_ID = "shares.modal.upsert-share-type"
        }

        override fun submitButton(): Locator = locator.getByDataId("$BASE_ID.modal.submit-button")
        override fun cancelButton(): Locator = locator.getByDataId("$BASE_ID.modal.cancel-button")
        override fun closeButton(): Locator = locator.getByDataId("$BASE_ID.modal.close-button")

        fun nameInput(): Locator = locator.getByDataId("$BASE_ID.name-input")
        fun keyInput(): Locator = locator.getByDataId("$BASE_ID.key-input")
        fun descriptionInput(): Locator = locator.getByDataId("$BASE_ID.description-input")
    }

    class ShareOfferManagement(override val locator: Locator) : DefaultTestAccordion(locator) {
        companion object {
            const val BASE_ID = "shares.page.share-offer"
        }

        override fun trigger(): Locator = locator.getByDataId("$BASE_ID.chevron")
        fun createNewShareOfferButton(): Locator = locator.getByDataId("$BASE_ID.create-button")
    }

    class UpsertShareOfferModal(
        override val locator: Locator,
        override val trigger: Locator,
    ) : TestModal(locator, trigger) {
        companion object {
            const val BASE_ID = "shares.modal.upsert-share-offer"
        }

        override fun submitButton(): Locator = locator.getByDataId("$BASE_ID.modal.submit-button")
        override fun cancelButton(): Locator = locator.getByDataId("$BASE_ID.modal.cancel-button")
        override fun closeButton(): Locator = locator.getByDataId("$BASE_ID.modal.close-button")

        fun fiscalYearDropdown(): Locator = locator.getByDataId("$BASE_ID.fiscal-year-dropdown")
        fun fiscalYearOption(key: String): Locator = locator.getByDataId("$BASE_ID.fiscal-year-dropdown.option.$key")
        fun shareTypeDropdown(): Locator = locator.getByDataId("$BASE_ID.share-type-dropdown")
        fun shareTypeOption(name: String): Locator = locator.getByDataId("$BASE_ID.share-type-dropdown.option.$name")
        fun pricingTypeDropdown(): Locator = locator.getByDataId("$BASE_ID.pricing-type-dropdown")
        fun pricingTypeOption(type: String): Locator = locator.getByDataId("$BASE_ID.pricing-type-dropdown.option.$type")
        fun priceInput(): Locator = locator.getByDataId("$BASE_ID.price-input")
        fun ahcAuthorizationRequiredDropdown(): Locator = locator.getByDataId("$BASE_ID.ahc-authorization-required-dropdown")
        fun ahcAuthorizationRequiredOption(key: String): Locator = locator.getByDataId("$BASE_ID.ahc-authorization-required-dropdown.option.$key")
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
            fun itemStatus(shareSubscriptionId: String, status: String): Locator = item(shareSubscriptionId).getByText(status)
            fun itemText(shareSubscriptionId: String, text: String): Locator = item(shareSubscriptionId).getByText(text)
            fun statusBadge(status: String): Locator = locator.getByText(status)
            fun mandateEntry(shareSubscriptionId: String, mandateText: String): Locator = item(shareSubscriptionId).getByText(mandateText)
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
            override fun cancelButton(): Locator = locator.getByDataId("$BASE_ID.modal.cancel-button")
            override fun closeButton(): Locator = locator.getByDataId("$BASE_ID.modal.close-button")

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

        fun listOfSubscriptions(): ListOfShareSubscriptions =
            ListOfShareSubscriptions(locator.getByDataId("$BASE_ID.list"))
    }

    class TransferOwnershipModal(
        override val locator: Locator,
        override val trigger: Locator,
    ) : TestModal(locator, trigger) {
        companion object {
            const val BASE_ID = "shares.modal.transfer-ownership"
        }

        override fun submitButton(): Locator = locator.getByDataId("$BASE_ID.modal.submit-button")
        override fun cancelButton(): Locator = locator.getByDataId("$BASE_ID.modal.cancel-button")
        override fun closeButton(): Locator = locator.getByDataId("$BASE_ID.modal.close-button")

        fun userSearchInput(): Locator = locator.getByDataId("$BASE_ID.user-search-input")
        fun userSelectButton(username: String): Locator = locator.getByDataId("$BASE_ID.user.$username.select-button")
        fun userDisplayInput(): Locator = locator.getByDataId("$BASE_ID.user-display-input")

        fun searchUser(username: String) {
            val input = userSearchInput()
            if (input.isVisible) {
                input.fill(username)
            }
        }

        fun selectUser(username: String) {
            userSelectButton(username).click()
        }
    }

    class UpsertShareSubscriptionModal(
        override val locator: Locator,
        override val trigger: Locator,
    ) : TestModal(locator, trigger) {
        companion object {
            const val BASE_ID = "shares.modal.upsert-share-subscription"
        }

        override fun submitButton(): Locator = locator.getByDataId("$BASE_ID.modal.submit-button")
        override fun cancelButton(): Locator = locator.getByDataId("$BASE_ID.modal.cancel-button")
        override fun closeButton(): Locator = locator.getByDataId("$BASE_ID.modal.close-button")

        fun userDisplayInput(): Locator = locator.getByDataId("$BASE_ID.user-display-input")
        fun userSelectButton(username: String): Locator = locator.getByDataId("$BASE_ID.user.$username.select-button")
        fun fiscalYearDropdown(): Locator = locator.getByDataId("$BASE_ID.fiscal-year-dropdown")
        fun fiscalYearOption(key: String): Locator = locator.getByDataId("$BASE_ID.fiscal-year-dropdown.option.$key")
        fun distributionPointDropdown(): Locator = locator.getByDataId("$BASE_ID.distribution-point-dropdown")
        fun distributionPointOption(name: String): Locator = locator.getByDataId("$BASE_ID.distribution-point-dropdown.option.$name")
        fun shareOfferDropdown(): Locator = locator.getByDataId("$BASE_ID.share-offer-dropdown")
        fun shareOfferOption(key: String): Locator = locator.getByDataId("$BASE_ID.share-offer-dropdown.option.$key")
        fun pricePerShareInput(): Locator = locator.getByDataId("$BASE_ID.price-per-share-input")
        fun numberOfSharesInput(): Locator = locator.getByDataId("$BASE_ID.number-of-shares-input")
        fun ahcAuthorizedDropdown(): Locator = locator.getByDataId("$BASE_ID.ahc-authorized-dropdown")
        fun ahcAuthorizedOption(key: String): Locator = locator.getByDataId("$BASE_ID.ahc-authorized-dropdown.option.$key")
        fun coSubscribersTextarea(): Locator = locator.getByDataId("$BASE_ID.co-subscribers-textarea")
        fun coSubscriberSelectButton(username: String): Locator = locator.getByDataId("$BASE_ID.user.$username.select-button")
    }

    class AttachOrUpsertSepaMandateModal(
        override val locator: Locator,
        override val trigger: Locator,
    ) : TestModal(locator, trigger) {
        companion object {
            const val BASE_ID = "shares.modal.attach-or-upsert-sepa-mandate"
        }

        override fun submitButton(): Locator = locator.getByDataId("$BASE_ID.modal.submit-button")
        override fun cancelButton(): Locator = locator.getByDataId("$BASE_ID.modal.cancel-button")
        override fun closeButton(): Locator = locator.getByDataId("$BASE_ID.modal.close-button")

        fun statusDropdown(): Locator = locator.page().getByDataId("sepa-mandate-form.status-dropdown")
        fun statusOption(status: String): Locator = locator.page().getByDataId("sepa-mandate-form.status-dropdown.option.$status")
    }

    class AttachSepaCollectionModal(
        override val locator: Locator,
        override val trigger: Locator,
    ) : TestModal(locator, trigger) {
        companion object {
            const val BASE_ID = "sepa-collection-form"
        }

        override fun submitButton(): Locator = locator.page().getByDataId("null.modal.submit-button")
        override fun cancelButton(): Locator = locator.page().getByDataId("null.modal.cancel-button")
        override fun closeButton(): Locator = locator.page().getByDataId("null.modal.close-button")

        fun bankAccountDropdown(): Locator = locator.getByDataId("$BASE_ID.bank-account-dropdown")
        fun bankAccountOption(accountKey: String): Locator = locator.getByDataId("$BASE_ID.bank-account-dropdown.option.$accountKey")
        fun collectionKeyInput(): Locator = locator.locator("#collection-key-input")
        fun mandateReferencePrefixInput(): Locator = locator.locator("#mandate-reference-prefix")
        fun remittanceInformationInput(): Locator = locator.locator("#remittance-information")
    }

    class ConfirmationModal(
        override val locator: Locator,
        override val trigger: Locator = locator,
    ) : TestModal(locator, trigger) {
        companion object {
            const val BASE_ID = "null.modal"
        }

        override fun submitButton(): Locator = locator.page().getByDataId("$BASE_ID.submit-button")
        override fun cancelButton(): Locator = locator.page().getByDataId("$BASE_ID.cancel-button")
        override fun closeButton(): Locator = locator.page().getByDataId("$BASE_ID.close-button")
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

    @Suppress("FunctionName")
    fun UpsertShareTypeModal(): UpsertShareTypeModal =
        UpsertShareTypeModal(
            page.getByDataId("${UpsertShareTypeModal.BASE_ID}.modal"),
            ShareTypeManagement().createNewShareTypeButton()
        )

    @Suppress("FunctionName")
    fun UpsertShareOfferModal(): UpsertShareOfferModal =
        UpsertShareOfferModal(
            page.getByDataId("${UpsertShareOfferModal.BASE_ID}.modal"),
            ShareOfferManagement().createNewShareOfferButton()
        )

    @Suppress("FunctionName")
    fun TransferOwnershipModal(subscriptionId: String): TransferOwnershipModal =
        TransferOwnershipModal(
            page.getByDataId("${TransferOwnershipModal.BASE_ID}.modal"),
            page.getByDataId("shares.page.share-subscription.$subscriptionId.transfer-ownership-button")
        )

    @Suppress("FunctionName")
    fun UpsertShareSubscriptionModal(): UpsertShareSubscriptionModal =
        UpsertShareSubscriptionModal(
            page.getByDataId("${UpsertShareSubscriptionModal.BASE_ID}.modal"),
            ShareSubscriptionManagement().createNewShareSubscriptionButton()
        )

    @Suppress("FunctionName")
    fun AttachOrUpsertSepaMandateModal(): AttachOrUpsertSepaMandateModal =
        AttachOrUpsertSepaMandateModal(
            page.getByDataId("${AttachOrUpsertSepaMandateModal.BASE_ID}.modal"),
            page.getByPartialDataId(".attach-or-upsert-sepa-mandate-button").first()
        )

    @Suppress("FunctionName")
    fun AttachSepaCollectionModal(shareOfferId: String): AttachSepaCollectionModal =
        AttachSepaCollectionModal(
            page.getByDataId("null.modal"),
            page.getByDataId("shares.page.share-offer.$shareOfferId.sepa-collection-button")
        )

    @Suppress("FunctionName")
    fun ConfirmationModal(): ConfirmationModal = ConfirmationModal(page.getByDataId("null.modal"))


    /**
     * Creates a share type with the given [name]/[key]/[description].
     * The key must have length <= 20.
     */
    fun createShareType(name: String, key: String, description: String): ShareType {
        with(ShareTypeManagement()) {
            open()
        }

        val modal = UpsertShareTypeModal()
        modal.trigger.click()
        assertThat(modal.locator).isVisible()

        modal.nameInput().fill(name)
        modal.keyInput().fill(key)
        modal.descriptionInput().fill(description)

        val shareType = page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = CreateShareType::class
        ) {
            modal.submitButton().click()
        }.decodeBodyTo<ShareType>()


        assertThat(modal.locator).not().isVisible()

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
    ) : ApiShareOffer {
        val management = ShareOfferManagement()
        assertThat(management).isVisible()
        if(!management.isOpened()) management.open()

        val modal = UpsertShareOfferModal()
        assertThat(modal.trigger).isVisible()
        modal.trigger.click()
        assertThat(modal.locator).isVisible()

        modal.fiscalYearDropdown().click()
        modal.fiscalYearOption(fiscalYearKey).click()

        modal.shareTypeDropdown().click()
        modal.shareTypeOption(shareTypeName).click()

        modal.pricingTypeDropdown().click()
        modal.pricingTypeOption("FIXED").click()

        modal.priceInput().fill(price)

        val ahcOptionKey = if (ahcAuthorizationRequired) CHECK_TRUE else CHECK_FALSE
        modal.ahcAuthorizationRequiredDropdown().click()
        modal.ahcAuthorizationRequiredOption(ahcOptionKey).click()

        val shareOffer = page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = CreateShareOffer::class
        ) {
            modal.submitButton().click()
        }.decodeBodyTo<ApiShareOffer>()

        assertThat(modal.locator).not().isVisible()

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

        val modal = AttachSepaCollectionModal(shareOffer.id)
        assertThat(modal.trigger).isVisible()
        modal.trigger.click()

        modal.bankAccountDropdown().click()
        modal.bankAccountOption("$creditorAccountHolder / $creditorAccountDescription").click()

        modal.collectionKeyInput().fill(collectionKey)
        modal.mandateReferencePrefixInput().fill(mandateReferencePrefix)
        modal.remittanceInformationInput().fill(remittanceInformation)

        val submitButton = modal.submitButton()
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

        val modal = AttachOrUpsertSepaMandateModal()
        assertThat(modal.trigger).isEnabled()
        modal.trigger.click()

        modal.statusDropdown().click()
        modal.statusOption("ACTIVE").click()

        val sepaMandate = page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = CreateSepaMandate::class
        ) {
            modal.submitButton().click()
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
        }

        val modal = UpsertShareSubscriptionModal()
        modal.trigger.click()
        assertThat(modal.locator).isVisible()

        val selectedUser = modal.userDisplayInput()
        val userSelectionButton = modal.userSelectButton(username)
        userSelectionButton.apply {
            assertThat(this).isVisible()
            assertThat(this).isEnabled()
            click()
        }
        assertThat(selectedUser).hasValue(fullName)

        modal.fiscalYearDropdown().click()
        modal.fiscalYearOption(fiscalYearKey).click()

        modal.distributionPointDropdown().click()
        modal.distributionPointOption(distributionPointName).click()

        modal.shareOfferDropdown().click()
        modal.shareOfferOption("$shareTypeName - $fiscalYearKey").click()

        modal.pricePerShareInput().fill(price)
        assertThat(modal.pricePerShareInput()).hasValue(price)

        modal.numberOfSharesInput().fill(numberOfShares)
        assertThat(modal.numberOfSharesInput()).hasValue(numberOfShares)

        modal.ahcAuthorizedDropdown().click()
        modal.ahcAuthorizedOption(ahcAuthorized.checkIcon()).click()

        modal.coSubscribersTextarea().fill(
            coSubscribers.joinToString("\n") { it }
        )

        val submitButton = modal.submitButton()
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
            val confirmationModal = ConfirmationModal()
            confirmationModal.submitButton().click()
            assertThat(confirmationModal.locator).not().isVisible()
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
            val confirmationModal = ConfirmationModal()
            assertThat(confirmationModal.submitButton()).isVisible()
            confirmationModal.submitButton().click()
            assertThat(confirmationModal.locator).not().isVisible()
        }.decodeBodyTo<ApiShareSubscriptions>()

        return shareSubscriptions
    }
}
