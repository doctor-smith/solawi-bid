package org.solyton.solawi.bid.workflow.organization

import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.module.application.permission.delegableOrganizationApplicationRoles
import org.solyton.solawi.bid.module.user.data.api.organization.Organization
import org.solyton.solawi.bid.pageobjects.application.dashboard.DashboardPage
import org.solyton.solawi.bid.pageobjects.module.application.PrivateApplicationManagementPage
import org.solyton.solawi.bid.pageobjects.module.user.OrganizationManagementPage
import org.solyton.solawi.bid.pageobjects.shared.NavBar

data class ApplicationBookingConfig(val name: String, val roles: Set<String>) {
    companion object {
        fun from(vararg applications: String): List<ApplicationBookingConfig> = applications.map {
            ApplicationBookingConfig(it, delegableOrganizationApplicationRoles(it))
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
) : TestWorkflow<CreateOrganizationInput, Organization> {
    override fun TestUserContext.runWith(input: CreateOrganizationInput): Organization {
        val navBar = NavBar(page)
        val dashboardPage = DashboardPage(page)
        val organizationManagementPage = OrganizationManagementPage(page)
        val privateApplicationManagementPage = PrivateApplicationManagementPage(page)

        val organizationCard = dashboardPage.organizationsCard()
        assertThat(organizationCard).isVisible()
        organizationCard.click()
        dismissStrayErrorDialogIfPresent()
        waitForLoadingToFinish()

        val organization = organizationManagementPage.createOrganization(input.organizationName)
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
            if (app.roles.isNotEmpty()) {
                organizationManagementPage.grantPermissions(
                    input.organizationName,
                    app.name,
                    user.username.value,
                    app.roles.toList()
                )
            }
        }

        return organization
    }
}
