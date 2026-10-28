package org.solyton.solawi.bid.workflow.organization

import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.pageobjects.OrganizationManagementPage
import org.solyton.solawi.bid.pageobjects.PrivateApplicationManagementPage
import org.solyton.solawi.bid.pageobjects.application.dashboard.DashboardPage
import org.solyton.solawi.bid.pageobjects.shared.NavBar

sealed class ApplicationBookingConfig {
    data object AllRoles : ApplicationBookingConfig()
    data class SpecificRoles(val roles: List<String>) : ApplicationBookingConfig()
}

data class CreateOrganizationInput(
    val organizationName: String,
    val appsToBook: List<String> = listOf("BANKING", "DISTRIBUTION", "SHARE_MANAGEMENT")
)

class CreateOrganizationWorkflow(
    override val context: TestUserContext
) : TestWorkflow<CreateOrganizationInput, Unit> {
    override fun TestUserContext.runWith(input: CreateOrganizationInput) {
        val navBar = NavBar(page)
        val dashboardPage = DashboardPage(page)
        val organizationManagementPage = OrganizationManagementPage(page)
        val privateApplicationManagementPage = PrivateApplicationManagementPage(page)

        val organizationCard = dashboardPage.organizationsCard()
        assertThat(organizationCard).isVisible()
        organizationCard.click()
        dismissStrayErrorDialogIfPresent()
        waitForLoadingToFinish()

        organizationManagementPage.createOrganization(input.organizationName)
        waitForLoadingToFinish()

        navBar.moveToDashboard()
        assertThat(organizationCard).isVisible()

        dashboardPage.myAppsLink().click()
        waitForLoadingToFinish()
        dismissStrayErrorDialogIfPresent()
        waitForLoadingToFinish()

        input.appsToBook.forEach { appName ->
            privateApplicationManagementPage.bookApplication(appName, input.organizationName, false)
        }

        input.appsToBook.forEach { appName ->
            organizationManagementPage.grantAllPermissions(input.organizationName, appName, user.username.value)
        }
    }
}
