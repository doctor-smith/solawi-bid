package org.solyton.solawi.bid.workflow.organization

import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.pageobjects.OrganizationManagementPage
import org.solyton.solawi.bid.pageobjects.PrivateApplicationManagementPage
import org.solyton.solawi.bid.pageobjects.application.dashboard.DashboardPage
import org.solyton.solawi.bid.pageobjects.shared.NavBar

sealed class ApplicationBookingConfig(open val name: String) {
    data class AllRoles(
        override val name: String
    ) : ApplicationBookingConfig(name)
    data class SpecificRoles(
        override val name: String,
        val roles: List<String>
    ) : ApplicationBookingConfig(name)

    companion object {
        fun from(vararg applications: String): List<ApplicationBookingConfig> = applications.map {
            AllRoles(it)
        }
    }
}

data class CreateOrganizationInput(
    val organizationName: String,
    val appsToBook: List<ApplicationBookingConfig> = ApplicationBookingConfig.from(
        "BANKING", "DISTRIBUTION", "SHARE_MANAGEMENT"
    )
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

        input.appsToBook.forEach { app ->
            privateApplicationManagementPage.bookApplication(app.name, input.organizationName, false)
        }

        input.appsToBook.forEach { app ->
            when(app) {
                is ApplicationBookingConfig.AllRoles ->
                    organizationManagementPage.grantAllPermissions(
                        input.organizationName,
                        app.name,
                        user.username.value
                    )
                is ApplicationBookingConfig.SpecificRoles ->
                    organizationManagementPage.grantPermissions(
                        input.organizationName,
                        app.name,
                        user.username.value,
                        app.roles
                    )
            }
        }
    }
}
