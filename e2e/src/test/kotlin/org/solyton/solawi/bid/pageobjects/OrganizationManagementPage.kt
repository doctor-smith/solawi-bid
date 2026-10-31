package org.solyton.solawi.bid.pageobjects

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.application.api.solawiApi
import org.solyton.solawi.bid.module.permission.data.api.PutUserRoleContext
import org.solyton.solawi.bid.module.permission.data.api.UserContext
import org.solyton.solawi.bid.module.user.data.api.organization.CreateOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.ImportMembers
import org.solyton.solawi.bid.module.user.data.api.organization.Organization

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
    fun createOrganization(name: String, hasToNavigate: Boolean = true): Organization {
        if (hasToNavigate) page.navigate("${TestConfig.BASE_URL}/app/management/organizations")
        page.dismissStrayErrorDialogIfPresent()
        page.waitForLoadingToFinish()

        page.clickOn("organization-management-page.button.create-organization")
        page.locator("#organization-name").fill(name)

        val organization = page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = CreateOrganization::class
        ) {
            page.clickOn("null.modal.submit-button")
        }.decodeBodyTo<Organization>()

        page.waitForSelector("text=$name")

        return organization
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
    fun grantAllPermissions(
        organizationName: String,
        applicationName: String,
        username: String,
        hasToNavigate: Boolean = true
    ): UserContext {
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

        val response = page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = PutUserRoleContext::class,
        ) {
            page.clickOn("null.modal.submit-button")
        }.decodeBodyTo<UserContext>()

        return response
    }

    fun grantPermissions(
        organizationName: String,
        applicationName: String,
        username: String,
        roles: List<String>,
        hasToNavigate: Boolean = true
    ): UserContext {
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

        val roleCheckboxes = page.locator("input[type='checkbox'][data-id^='manage-user-permissions.modal.role.']")
        roleCheckboxes.first().waitFor()

        roles.forEach { role ->
            page.clickOn("manage-user-permissions.modal.role.$role.checkbox")
        }

        val response = page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = PutUserRoleContext::class,
        ) {
            page.clickOn("null.modal.submit-button")
        }.decodeBodyTo<UserContext>()

        return response
    }

    class AddMemberModal(
        override val locator: Locator,
        override val trigger: Locator,
    ) : TestModal(locator, trigger) {
        companion object {
            const val BASE_ID = "organization.member.update-modal"
        }

        override fun submitButton(): Locator = locator.getByDataId("$BASE_ID.modal.submit-button")
        override fun cancelButton(): Locator = locator.getByDataId("$BASE_ID.modal.cancel-button")
        override fun closeButton(): Locator = locator.getByDataId("$BASE_ID.modal.close-button")

        fun usernameInput(): Locator = locator.page().getByDataId("user-profile.form.username-input")
        fun firstnameInput(): Locator = locator.page().getByDataId("user-profile.form.firstname-input")
        fun lastnameInput(): Locator = locator.page().getByDataId("user-profile.form.lastname-input")
    }

    @Suppress("FunctionName")
    fun AddMemberModal(): AddMemberModal =
        AddMemberModal(
            page.getByDataId("${AddMemberModal.BASE_ID}.modal"),
            page.getByDataId("organization.page.members.add-button")
        )

    /**
     * Adds a new member to an existing organization via the members card modal
     * on the organization detail page.
     */
    fun addMember(
        organizationName: String,
        username: String,
        firstname: String = "New",
        lastname: String = "Member",
        hasToNavigate: Boolean = true
    ) {
        if (hasToNavigate) page.navigate("${TestConfig.BASE_URL}/app/management/organizations")
        page.dismissStrayErrorDialogIfPresent()
        page.waitForLoadingToFinish()

        page.clickOn("organization-management-page.organization-list.organization.$organizationName.details-button")
        page.waitForLoadingToFinish()
        page.dismissStrayErrorDialogIfPresent()

        val chevron = page.getByDataId("organization.page.members.chevron")
        assertThat(chevron).isVisible()
        chevron.click()

        val modal = AddMemberModal()
        assertThat(modal.trigger).isVisible()
        modal.trigger.click()

        assertThat(modal.locator).isVisible()

        modal.usernameInput().fill(username)
        modal.firstnameInput().fill(firstname)
        modal.lastnameInput().fill(lastname)

        page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = ImportMembers::class
        ) {
            modal.submitButton().click()
        }
        page.waitForLoadingToFinish()
    }
}
