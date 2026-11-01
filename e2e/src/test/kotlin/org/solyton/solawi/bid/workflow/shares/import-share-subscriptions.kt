package org.solyton.solawi.bid.workflow.shares

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.application.api.solawiApi
import org.solyton.solawi.bid.module.shares.data.api.ApiShareSubscription
import org.solyton.solawi.bid.module.shares.data.api.UpdateShareSubscription
import org.solyton.solawi.bid.pageobjects.SharesPage

data class ImportShareSubscriptionsInput(
    val subscriptionId: String,
    val username: String,
    val newNumberOfShares: Int,
    val newPricePerShare: Double,
    val filterStatus: String = "All Statuses",
    val filterAhcAuth: String = "All Authorized"
)

class ImportShareSubscriptionsWorkflow(
    override val context: TestUserContext
) : TestWorkflow<ImportShareSubscriptionsInput, ApiShareSubscription> {
    override fun TestUserContext.runWith(input: ImportShareSubscriptionsInput): ApiShareSubscription {
        val sharesPage = SharesPage(page)
        val shareSubscriptions = sharesPage.ShareSubscriptionManagement()
        shareSubscriptions.open()

        val statusFilter = SharesPage.ShareSubscriptionManagement.StatusFilter(
            locator = shareSubscriptions.locator.getByDataId("shares.page.share-subscription.status-dropdown.wrapper")
        )
        assertThat(statusFilter).isVisible()
        statusFilter.select(input.filterStatus)
        waitForLoadingToFinish()

        val ahcAuthFilter = SharesPage.ShareSubscriptionManagement.AhcAuthFilter(
            locator = shareSubscriptions.locator.getByDataId("shares.page.share-subscription.ahc-authorized-dropdown.wrapper")
        )
        assertThat(ahcAuthFilter).isVisible()
        ahcAuthFilter.select(input.filterAhcAuth)
        waitForLoadingToFinish()

        val list = shareSubscriptions.listOfSubscriptions()
        assertThat(list).isVisible()

        // Select all subscriptions via select-all checkbox
        val selectAllCheckbox = page.getByDataId("shares.page.share-subscription.select-all-checkbox")
        assertThat(selectAllCheckbox).isVisible()
        selectAllCheckbox.click()
        assertThat(selectAllCheckbox).isChecked()

        page.waitForTimeout(500.0)

        // Open import modal via PageObject abstraction
        val importModal = sharesPage.ImportShareSubscriptionsModal()
        assertThat(importModal.trigger).isVisible()
        importModal.trigger.click()
        assertThat(importModal.locator).isVisible()

        // Drop CSV content onto dropzone via PageObject method
        val csvContent = """
            username;pricePerShare;numberOfShares
            ${input.username};${input.newPricePerShare};${input.newNumberOfShares}
        """.trimIndent()
        importModal.dropCsv(csvContent)

        page.waitForTimeout(500.0)

        // Click submit on import modal and confirm
        importModal.submitButton().click()

        val confirmModal = sharesPage.ConfirmationModal()
        assertThat(confirmModal.submitButton()).isVisible()

        val updatedSubscription = page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = UpdateShareSubscription::class
        ) {
            confirmModal.submitButton().click()
        }.decodeBodyTo<ApiShareSubscription>()

        assertThat(confirmModal.locator).not().isVisible()
        assertThat(importModal.locator).not().isVisible()

        waitForLoadingToFinish()

        return updatedSubscription
    }
}
