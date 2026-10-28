package org.solyton.solawi.bid.workflow.user

import org.solyton.solawi.bid.TestUserContext
import org.solyton.solawi.bid.TestWorkflow
import org.solyton.solawi.bid.clickOn
import org.solyton.solawi.bid.pageobjects.module.authentication.LoginPage
import org.solyton.solawi.bid.waitForLoadingToFinish

data class UserLoginInput(
    val acceptCookieDisclaimer: Boolean = true
)

class UserLoginWorkflow(
    override val context: TestUserContext
) : TestWorkflow<UserLoginInput, Unit> {
    override fun TestUserContext.runWith(input: UserLoginInput) {
        val loginPage = LoginPage(page)
        loginPage.load()
        waitForLoadingToFinish()
        if (input.acceptCookieDisclaimer) {
            page.clickOn("cookie-disclaimer.modal.submit-button")
        }
        loginPage.login(user)
        page.waitForURL("**/dashboard")
    }
}
