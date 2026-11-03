package org.solyton.solawi.bid.workflow.shares

import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.module.shares.data.api.ApiShareSubscriptions
import org.solyton.solawi.bid.pageobjects.module.shares.SharesPage

data class BulkChangeSubscriptionStatusInput(
    val filterStatus: String = "All Statuses",
    val filterAhcAuth: String? = "All Authorized",
    val newStatus: String
)

class BulkChangeSubscriptionStatusWorkflow(
    override val context: TestUserContext
) : TestWorkflow<BulkChangeSubscriptionStatusInput, ApiShareSubscriptions> {
    override fun TestUserContext.runWith(input: BulkChangeSubscriptionStatusInput): ApiShareSubscriptions {
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

        val subscriptions = sharesPage.changeStatusViaBulkEdit(input.newStatus)
        waitForLoadingToFinish()

        statusFilter.select("All Statuses")
        waitForLoadingToFinish()

        return subscriptions
    }
}
