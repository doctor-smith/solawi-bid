package org.solyton.solawi.bid.pageobjects.module.application

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.WaitForSelectorState
import org.solyton.solawi.bid.*

/**
 * Page object for `/app/management/private`, where the current user personally
 * activates applications (register -> start trial -> subscribe) and books them to
 * organizations.
 */
class PrivateApplicationManagementPage(private val page: Page) {

    class ConnectApplicationModal(
        override val locator: Locator,
        override val trigger: Locator,
    ) : TestModal(locator, trigger) {
        companion object {
            const val BASE_ID = "connect-application-to-organization.modal"
        }

        override fun submitButton(): Locator = locator.page().getByDataId("null.modal.submit-button")
        override fun cancelButton(): Locator = locator.page().getByDataId("null.modal.cancel-button")
        override fun closeButton(): Locator = locator.page().getByDataId("null.modal.close-button")

        fun organizationDropdown(): Locator = locator.page().getByDataId("$BASE_ID.organization-dropdown")
        fun organizationOption(orgName: String): Locator = locator.page().getByDataId("$BASE_ID.organization-dropdown.option.$orgName")
    }

    /**
     * Ensures [applicationName] is personally `Active` for the current user (TEST_USER),
     * which is a prerequisite for the "connect to organization" button to even render on
     * `/app/management/private`. Walks through whichever of the lifecycle steps
     * (register -> start trial -> subscribe) still apply; already-completed or
     * not-yet-reached steps are harmlessly skipped by `clickIfPresent`.
     */
    fun ensureApplicationActiveForTestUser(applicationName: String, hasToNavigate: Boolean = true) {
        // TODO wait for responses
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

        val modal = ConnectApplicationModal(
            page.getByDataId("null.modal"),
            page.getByDataId("private-application-management.page.application.$applicationName.connect-button")
        )
        modal.trigger.click()

        modal.organizationDropdown().click()
        modal.organizationOption(organizationName).click()
        modal.submitButton().click()

        // Confirm the booking actually persisted before moving on: the organization name
        // shows up in the "linked organizations" column of the application's row.
        page.waitForSelector("text=$organizationName")
    }
}