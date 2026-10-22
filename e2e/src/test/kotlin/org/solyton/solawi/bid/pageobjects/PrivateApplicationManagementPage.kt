package org.solyton.solawi.bid.pageobjects

import TestConfig
import clickIfPresent
import clickOn
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.WaitForSelectorState
import dismissStrayErrorDialogIfPresent
import getByDataId
import waitForLoadingToFinish

/**
 * Page object for `/app/management/private`, where the current user personally
 * activates applications (register -> start trial -> subscribe) and books them to
 * organizations.
 */
class PrivateApplicationManagementPage(private val page: Page) {

    /**
     * Ensures [applicationName] is personally `Active` for the current user (TEST_USER),
     * which is a prerequisite for the "connect to organization" button to even render on
     * `/app/management/private`. Walks through whichever of the lifecycle steps
     * (register -> start trial -> subscribe) still apply; already-completed or
     * not-yet-reached steps are harmlessly skipped by `clickIfPresent`.
     */
    fun ensureApplicationActiveForTestUser(applicationName: String, hasToNavigate: Boolean = true) {
        if (hasToNavigate) {
            page.navigate("${TestConfig.BASE_URL}/app/management/private")
            page.dismissStrayErrorDialogIfPresent()
            page.waitForLoadingToFinish()
        }
        page.clickIfPresent("private-application-management.page.available-application.$applicationName.register-button")
        page.clickIfPresent("private-application-management.page.application.$applicationName.start-trial-button")
        page.clickIfPresent("private-application-management.page.application.$applicationName.subscribe-button")
        page.getByDataId("private-application-management.page.application.$applicationName.connect-button")
            .first()
            .waitFor(Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE))
    }

    /**
     * Books [applicationName] (e.g. "BANKING", "DISTRIBUTION", "SHARE_MANAGEMENT") for the
     * organization called [organizationName] via the private application-management
     * "connect" flow (`/app/management/private`). Ensures the application is personally
     * `Active` for the current user first, since the connect button is only rendered then.
     */
    fun bookApplication(applicationName: String, organizationName: String, hasToNavigate: Boolean = true) {
        ensureApplicationActiveForTestUser(applicationName, hasToNavigate)

        page.clickOn("private-application-management.page.application.$applicationName.connect-button")
        page.clickOn("connect-application-to-organization.modal.organization-dropdown")
        page.clickOn("connect-application-to-organization.modal.organization-dropdown.option.$organizationName")
        page.clickOn("null.modal.submit-button")

        // Confirm the booking actually persisted before moving on: the organization name
        // shows up in the "linked organizations" column of the application's row.
        page.waitForSelector("text=$organizationName")
    }
}
