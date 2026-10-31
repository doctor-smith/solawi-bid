package org.solyton.solawi.bid.workflow.shares

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.application.api.solawiApi
import org.solyton.solawi.bid.module.shares.data.api.ShareSubscription
import org.solyton.solawi.bid.module.shares.data.api.TransferOwnership
import org.solyton.solawi.bid.pageobjects.SharesPage

data class TransferSubscriptionOwnershipInput(
    val subscriptionId: String,
    val newOwnerUsername: String,
    val newOwnerFullname: String,
)

class TransferSubscriptionOwnershipWorkflow(
    override val context: TestUserContext
) : TestWorkflow<TransferSubscriptionOwnershipInput, ShareSubscription> {

    override fun TestUserContext.runWith(input: TransferSubscriptionOwnershipInput): ShareSubscription {
        val sharesPage = SharesPage(page)

        val shareSubscriptionManagement = sharesPage.ShareSubscriptionManagement()
        shareSubscriptionManagement.open()

        val statusFilter = SharesPage.ShareSubscriptionManagement.StatusFilter(
            locator = shareSubscriptionManagement.locator.getByDataId("shares.page.share-subscription.status-dropdown.wrapper")
        )
        assertThat(statusFilter).isVisible()
        statusFilter.select("All Statuses")

        val ahcAuthFilter = SharesPage.ShareSubscriptionManagement.AhcAuthFilter(
            locator = shareSubscriptionManagement.locator.getByDataId("shares.page.share-subscription.ahc-authorized-dropdown.wrapper")
        )
        assertThat(ahcAuthFilter).isVisible()
        ahcAuthFilter.select("All Authorized")

        val transferButton = page.getByDataId("shares.page.share-subscription.${input.subscriptionId}.transfer-ownership-button")
        transferButton.click()

        val modal = page.getByDataId("shares.modal.transfer-ownership.modal")
        assertThat(modal).isVisible()

        // Benutzer im Modal suchen und auswählen
        val searchInput = modal.locator("[data-id='shares.modal.transfer-ownership.user-search-input']")
        if (searchInput.isVisible) {
            searchInput.fill(input.newOwnerUsername)
        }

        // Select-Button für den neuen Benutzer anklicken
        val selectButton = modal.locator("[data-id='shares.modal.transfer-ownership.user.${input.newOwnerUsername}.select-button']")
            .or(modal.locator("[data-id*='select-button']"))
            .first()
        selectButton.click()

        val userDisplay = page.getByDataId("shares.modal.transfer-ownership.user-display-input")
        assertThat(userDisplay).hasValue(input.newOwnerFullname)

        // Bestätigen
        val confirmButton = modal.locator("[data-id='shares.modal.transfer-ownership.modal.submit-button']")
        val response = page.waitForApi(
            solawiApi,
            TransferOwnership::class
        ) {
            confirmButton.click()
        }.decodeBodyTo<ShareSubscription>()

        assertThat(modal).isHidden()

        return response
    }
}