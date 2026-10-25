package org.solyton.solawi.bid.pageobjects

import com.microsoft.playwright.Page
import org.solyton.solawi.bid.TestConfig
import org.solyton.solawi.bid.clickOn
import org.solyton.solawi.bid.dismissStrayErrorDialogIfPresent
import org.solyton.solawi.bid.waitForLoadingToFinish

/**
 * Page object for the organization-management admin UI: the organization list/creation
 * page (`/app/management/organizations`) and an organization's own detail page (the
 * "connected applications" card used to manage per-application permissions).
 */
class OrganizationManagementPage(private val page: Page) {

    /**
     * Creates a brand-new root organization with a unique [name] via
     * `/app/management/organizations`. TEST_USER (the creator) automatically becomes a
     * member of it (`createRootOrganization` inserts the creator into `UserOrganization`),
     * so it can later subscribe to a share offer in this same organization without any
     * separate "add member" step.
     */
    fun createOrganization(name: String, hasToNavigate: Boolean = true) {
        if (hasToNavigate) page.navigate("${TestConfig.BASE_URL}/app/management/organizations")
        page.dismissStrayErrorDialogIfPresent()
        page.waitForLoadingToFinish()

        page.clickOn("organization-management-page.button.create-organization")
        page.locator("#organization-name").fill(name)
        page.clickOn("null.modal.submit-button")

        page.waitForSelector("text=$name")
    }

    /**
     * Grants the current user (TEST_USER) every available role on the given
     * [applicationName] context for the organization called [organizationName], via
     * the organization's own "connected applications" card -> "manage permissions"
     * dialog. Plain `OWNER` (auto-granted server side at booking time, see
     * `connectApplicationToOrganization`) alone is not sufficient for every action
     * (e.g. creating a legal entity), so every role checkbox is checked here; the user
     * will restrict this later once the exact required roles are known.
     */
    fun grantOwnerPermission(
        organizationName: String,
        applicationName: String,
        username: String,
        hasToNavigate: Boolean = true
    ) {
        if (hasToNavigate) page.navigate("${TestConfig.BASE_URL}/app/management/organizations")
        page.dismissStrayErrorDialogIfPresent()
        page.waitForLoadingToFinish()

        page.clickOn("organization-management-page.organization-list.organization.$organizationName.details-button")
        page.waitForLoadingToFinish()
        page.dismissStrayErrorDialogIfPresent()

        page.clickOn("organization.page.connected-applications.chevron")
        page.clickOn("organization.page.connected-applications.$applicationName.manage-permissions-button")
        page.waitForLoadingToFinish()
        page.dismissStrayErrorDialogIfPresent()

        page.clickOn("private-application-organization-management.page.member.$username.manage-permissions-button")

        // Check every available role checkbox (not just the already-checked, non-togglable
        // OWNER one), since OWNER alone is not always sufficient for every action.
        val roleCheckboxes = page.locator("input[type='checkbox'][data-id^='manage-user-permissions.modal.role.']")
        roleCheckboxes.first().waitFor()
        val count = roleCheckboxes.count()
        if (count <= 0) throw IllegalArgumentException("There should be at lease one role")
        for (i in 0 until count) {
            val checkbox = roleCheckboxes.nth(i)
            if (checkbox.isChecked.not()) checkbox.check()
        }
        page.clickOn("null.modal.submit-button")
    }
}
