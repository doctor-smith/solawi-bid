package org.solyton.solawi.bid.workflow.admin

import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.module.authentication.service.generateSecurePassword
import org.solyton.solawi.bid.module.values.Password
import org.solyton.solawi.bid.module.values.Username
import org.solyton.solawi.bid.pageobjects.module.user.UserManagementPage
import org.solyton.solawi.bid.pageobjects.shared.NavBar

data class CreateTestUserInput(
    val username: Username,
    val password: Password = Password(generateSecurePassword()),
    val useSuffixInUserName: Boolean = true,
    val suffix: String,
    val reportDirectory: String,
)

class CreateTestUserWorkflow(
    override val context: TestUserContext
) : TestWorkflow<CreateTestUserInput, StdTestUser> {
    override fun TestUserContext.runWith(input: CreateTestUserInput): StdTestUser =
        withFailureDiagnostics(input.reportDirectory) {

            // Create a test user and log out
            val username = input.username
            val password = input.password
            val useTimestampInUserName = input.useSuffixInUserName

            val navbar = NavBar(page)
            val userManagementPage = UserManagementPage(page)

            // TODO Want to navigate to userManagementPage by clicking on the right link. But the link is missing!
            //     Fix: Add rights and roles to the db by migration, so that the test user gets owner rights
            //     Then: uncomment these lines
            //
            // val dashboardPage = DashboardPage(page)
            // dashboardPage.load()
            // dashboardPage.userManagementLink().click()

            userManagementPage.load()
            // Known bug on UsermanagementPage -> CoroutineScope left composition
            dismissStrayErrorDialogIfPresent(true)

            // Create user to perform the actions
            val testUser = userManagementPage.createUserViaAdminUi(
                username,
                password,
                useTimestampInUserName
            )

            navbar.logout()

            page.waitForURL("**/login")
            // dismiss error message: 'Token is not valid or expired'
            dismissStrayErrorDialogIfPresent(true)

            testUser
        }
}
