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

        val transferModal = sharesPage.TransferOwnershipModal(input.subscriptionId)
        transferModal.trigger().click()

        assertThat(transferModal.locator).isVisible()

        transferModal.searchUser(input.newOwnerUsername)
        transferModal.selectUser(input.newOwnerUsername)

        assertThat(transferModal.userDisplayInput()).hasValue(input.newOwnerFullname)

        val response = page.waitForApi(
            solawiApi,
            TransferOwnership::class
        ) {
            transferModal.submitButton().click()
        }.decodeBodyTo<ShareSubscription>()

        assertThat(transferModal.locator).isHidden()

        return response
    }
}