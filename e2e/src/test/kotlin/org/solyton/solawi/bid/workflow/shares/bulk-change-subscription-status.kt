package org.solyton.solawi.bid.workflow.shares

import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.pageobjects.SharesPage

data class BulkChangeSubscriptionStatusInput(
    val filterStatus: String = "All Statuses",
    val filterAhcAuth: String? = "All Authorized",
    val newStatus: String
)

class BulkChangeSubscriptionStatusWorkflow(
    override val context: TestUserContext
) : TestWorkflow<BulkChangeSubscriptionStatusInput, SharesPage.ShareSubscriptionManagement.ListOfShareSubscriptions> {
    override fun TestUserContext.runWith(input: BulkChangeSubscriptionStatusInput): SharesPage.ShareSubscriptionManagement.ListOfShareSubscriptions {
        val sharesPage = SharesPage(page)
        val shareSubscriptions = sharesPage.ShareSubscriptionManagement()
        shareSubscriptions.open()
        val list = SharesPage.ShareSubscriptionManagement.ListOfShareSubscriptions(
            locator = shareSubscriptions.locator.getByDataId("shares.page.share-subscription.list")
        )
        assertThat(list).isVisible()

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

        sharesPage.changeStatusViaBulkEdit(input.newStatus)
        waitForLoadingToFinish()

        statusFilter.select("All Statuses")
        waitForLoadingToFinish()

        return SharesPage.ShareSubscriptionManagement.ListOfShareSubscriptions(
            locator = page.getByDataId("shares.page.share-subscription.list")
        )
    }
}
