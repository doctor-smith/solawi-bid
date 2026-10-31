package org.solyton.solawi.bid.test.module.organization

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.module.authentication.service.generateSecurePassword
import org.solyton.solawi.bid.module.values.Password
import org.solyton.solawi.bid.module.values.Username
import org.solyton.solawi.bid.pageobjects.module.user.UserManagementPage
import org.solyton.solawi.bid.pageobjects.shared.NavBar
import org.solyton.solawi.bid.workflow.organization.AddMemberToOrganizationInput
import org.solyton.solawi.bid.workflow.organization.AddMemberToOrganizationWorkflow
import org.solyton.solawi.bid.workflow.organization.CreateOrganizationInput
import org.solyton.solawi.bid.workflow.organization.CreateOrganizationWorkflow
import org.solyton.solawi.bid.workflow.user.UserLoginInput
import org.solyton.solawi.bid.workflow.user.UserLoginWorkflow

class AddMemberToOrganizationWorkflowTest {

    companion object {
        const val STORAGE_PATH = "AddMemberToOrganizationWorkflowTest"
        private lateinit var playwright: Playwright
        private lateinit var browser: Browser

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            val headless = TestConfig.HEADLESS
            val slowMo = TestConfig.SLOW_MO
            playwright = Playwright.create()
            browser = playwright.chromium().launch(
                BrowserType.LaunchOptions()
                    .setHeadless(headless)
                    .setSlowMo(slowMo)
            )
            TestUtils().getCookie(browser, STORAGE_PATH)
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            browser.close()
            playwright.close()
            TestUtils().deleteStorageStateFile(STORAGE_PATH)
        }
    }

    @Workflow
    @Test
    fun `provider adds a new member to an organization via the members modal`() {
        val reportDirectory = "AddMemberToOrganizationWorkflowTest"
        val suffix = System.currentTimeMillis()

        val adminUsername = requireNotNull(System.getenv("TEST_USER")) { "TEST_USER must be set" }
        val adminPassword = requireNotNull(System.getenv("TEST_USER_PASSWORD")) { "TEST_USER_PASSWORD must be set" }
        val adminUser = AdminTestUser(Username(adminUsername), Password(adminPassword), STORAGE_PATH)

        // 1. Admin creates Provider / Test-User
        val providerUser = withUser(adminUser on browser) {
            withFailureDiagnostics("$reportDirectory/admin-actions") {
                val userManagementPage = UserManagementPage(page)
                userManagementPage.load()
                dismissStrayErrorDialogIfPresent(true)

                val provider = userManagementPage.createUserViaAdminUi(
                    Username("e2e-orga-provider-$suffix@solyton.org"),
                    Password(generateSecurePassword()),
                    useTimestamp = false
                )

                NavBar(page).logout()
                page.waitForURL("**/login")
                dismissStrayErrorDialogIfPresent(true)

                provider
            }
        }

        // 2. Perform actions as Provider: Create organization and add member
        withUser(providerUser on browser) {
            withFailureDiagnostics("$reportDirectory/user-actions") {
                run(UserLoginWorkflow(this)) withInput UserLoginInput()

                val organizationName = "e2e-add-member-org-$suffix"
                run(CreateOrganizationWorkflow(this)) withInput CreateOrganizationInput(
                    organizationName = organizationName
                )

                val memberUsername = "e2e-orga-member-$suffix@solyton.org"
                val memberFirstname = "John"
                val memberLastname = "Doe"

                run(AddMemberToOrganizationWorkflow(this)) withInput AddMemberToOrganizationInput(
                    organizationName = organizationName,
                    username = memberUsername,
                    firstname = memberFirstname,
                    lastname = memberLastname
                )

                // Verification: new member appears in member list
                val membersCard = org.solyton.solawi.bid.pageobjects.OrganizationManagementPage(page).MembersCard()
                assertThat(membersCard.memberRow(memberUsername)).isVisible()
            }
        }
    }
}
