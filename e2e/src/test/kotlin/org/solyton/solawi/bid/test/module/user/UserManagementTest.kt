package org.solyton.solawi.bid.test.module.user

import com.microsoft.playwright.*
import com.microsoft.playwright.options.WaitForSelectorState
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.*
import java.nio.file.Files

/**
 * Convenient E2E tests for the user module, covering:
 * - creating a new user via the admin "user management" page
 * - logging in as that newly created user
 * - the logged-in user seeing their own username on the private account page
 */
class UserManagementTest {

    companion object {
        const val STORAGE_PATH = "UserManagementTest"
        private lateinit var playwright: Playwright
        private lateinit var browser: Browser

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            playwright = Playwright.create()
            browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))
            // Logs in once as TEST_USER (the application owner, who may manage users)
            // and stores the resulting session, so every test here can reuse it.
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

    // A fresh context, already logged in as TEST_USER via the stored session.
    private fun adminContext(): BrowserContext {
        val path = TestUtils().storageStatePath(STORAGE_PATH)
        check(Files.exists(path)) { "Missing storage state: $path" }
        val storedState = Files.readString(path)
        return browser.newContext(
            Browser.NewContextOptions().setStorageState(storedState)
        )
    }

    /**
     * Creates a new user via the admin "user management" page using [page], which must
     * already be logged in as a user allowed to manage users (e.g. via [adminContext]).
     * Returns the generated username/password of the created user.
     */
    private fun createUserViaAdminUi(page: Page): Pair<String, String> {
        // Usernames must be valid email addresses.
        val username = "e2e-user-${System.currentTimeMillis()}@example.com"
        val password = "Test-Pass-123"

        page.navigate("${TestConfig.BASE_URL}/app/management/users")
        dismissStrayErrorDialogIfPresent(page)
        page.clickOn("user-management.create-user-button")

        page.put("create-user-modal.input.username", username)
        page.put("create-user-modal.input.password", password)
        page.put("create-user-modal.input.repeat-password", password)
        page.clickOn("create-user-modal.modal.submit-button")

        return username to password
    }

    /**
     * Navigating straight to the user management page can briefly flash an unrelated
     * "coroutine scope left the composition" error dialog on first mount, which covers
     * the whole page. If it shows up, dismiss it so the page below becomes interactable
     * again, just like a real user clicking it away would.
     */
    private fun dismissStrayErrorDialogIfPresent(page: Page) {
        val errorOk = page.getByDataId("null.modal.submit-button")
        try {
            errorOk.first().waitFor(
                Locator.WaitForOptions().setTimeout(3000.0).setState(WaitForSelectorState.VISIBLE)
            )
            errorOk.first().click()
        } catch (e: TimeoutError) {
            // No stray error dialog appeared - nothing to dismiss.
        }
    }

    @Test
    fun test_createUser_appearsInUserList() {
        val context = adminContext()
        try {
            val page = context.newPage()
            val (username, _) = createUserViaAdminUi(page)

            val createdUserRow = page.waitForSelector("text=$username")

            Assertions.assertTrue(createdUserRow.isVisible, "The newly created user should show up in the user list.")
        } finally {
            context.close()
        }
    }

    @Test
    fun test_newlyCreatedUser_canLogIn() {
        val username: String
        val password: String
        val adminContext = adminContext()
        try {
            val page = adminContext.newPage()
            val credentials = createUserViaAdminUi(page)
            username = credentials.first
            password = credentials.second
            // Make sure the user was actually created before trying to log in as it.
            page.waitForSelector("text=$username")
        } finally {
            adminContext.close()
        }

        val context = browser.newContext()
        try {
            val page = context.newPage()
            page.navigate("${TestConfig.BASE_URL}/login")
            page.clickOn("cookie-disclaimer.modal.submit-button")

            page.put("login-form.input.username", username)
            page.put("login-form.input.password", password)
            page.clickOn("login-form.submit-button")

            page.waitForURL("**/dashboard")

            Assertions.assertTrue(
                page.url().contains("/dashboard"),
                "The newly created user was not redirected to the dashboard after login."
            )
        } finally {
            context.close()
        }
    }

    @Test
    fun test_privateUserPage_showsOwnUsername() {
        val context = adminContext()
        try {
            val page = context.newPage()
            val user = System.getenv("TEST_USER")

            page.navigate("${TestConfig.BASE_URL}/app/private/data")

            val usernameProperty = page.waitForSelector("text=$user")

            Assertions.assertTrue(
                usernameProperty.isVisible,
                "The private user page should display the logged-in user's own username."
            )
        } finally {
            context.close()
        }
    }
}