package org.solyton.solawi.bid.workflow.distribution

import org.solyton.solawi.bid.TestUserContext
import org.solyton.solawi.bid.TestWorkflow
import org.solyton.solawi.bid.navigateToOrganizationApplicationPage
import org.solyton.solawi.bid.pageobjects.DistributionPage
import org.solyton.solawi.bid.pageobjects.application.dashboard.DashboardPage
import org.solyton.solawi.bid.pageobjects.shared.NavBar
import org.solyton.solawi.bid.waitForLoadingToFinish

data class SetupDistributionInput(
    val organizationName: String,
    val distributionPointName: String
)

class SetupDistributionWorkflow(
    override val context: TestUserContext
) : TestWorkflow<SetupDistributionInput, Unit> {
    override fun TestUserContext.runWith(input: SetupDistributionInput) {
        val navBar = NavBar(page)
        val dashboardPage = DashboardPage(page)
        val distributionPage = DistributionPage(page)

        navBar.moveToDashboard()
        dashboardPage.distributionOrganizationManagementLink().click()

        page.navigateToOrganizationApplicationPage("distribution", input.organizationName, false)
        distributionPage.createDistributionPoint(input.distributionPointName)
        waitForLoadingToFinish()
    }
}
