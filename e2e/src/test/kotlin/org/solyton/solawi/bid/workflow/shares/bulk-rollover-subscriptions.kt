package org.solyton.solawi.bid.workflow.shares

import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.pageobjects.SharesPage

data class BulkRolloverSubscriptionsInput(
    val filterStatus: String = "All Statuses",
    val filterAhcAuth: String? = "All Authorized",
    val targetOfferOptionText: String
)

class BulkRolloverSubscriptionsWorkflow(
    override val context: TestUserContext
) : TestWorkflow<BulkRolloverSubscriptionsInput, SharesPage.ShareSubscriptionManagement.ListOfShareSubscriptions> {
    override fun TestUserContext.runWith(input: BulkRolloverSubscriptionsInput): SharesPage.ShareSubscriptionManagement.ListOfShareSubscriptions {
        val sharesPage = SharesPage(page)
        val shareSubscriptions = sharesPage.ShareSubscriptionManagement()
        shareSubscriptions.open()

        val statusFilter = SharesPage.ShareSubscriptionManagement.StatusFilter(
            locator = shareSubscriptions.locator.getByDataId("shares.page.share-subscription.status-dropdown.wrapper")
        )
        assertThat(statusFilter).isVisible()
        statusFilter.select(input.filterStatus)
        waitForLoadingToFinish()

        input.filterAhcAuth?.let { ahcAuth ->
            val ahcAuthFilter = SharesPage.ShareSubscriptionManagement.AhcAuthFilter(
                locator = shareSubscriptions.locator.getByDataId("shares.page.share-subscription.ahc-authorized-dropdown.wrapper")
            )
            assertThat(ahcAuthFilter).isVisible()
            ahcAuthFilter.select(ahcAuth)
            waitForLoadingToFinish()
        }

        sharesPage.rolloverViaBulkEdit(input.targetOfferOptionText)
        waitForLoadingToFinish()

        statusFilter.select("All Statuses")
        waitForLoadingToFinish()

        return SharesPage.ShareSubscriptionManagement.ListOfShareSubscriptions(
            locator = page.getByDataId("shares.page.share-subscription.list")
        )
    }
}
