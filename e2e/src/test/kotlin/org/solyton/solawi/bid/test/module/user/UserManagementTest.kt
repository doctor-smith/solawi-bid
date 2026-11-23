package org.solyton.solawi.bid.test.module.user

import com.microsoft.playwright.*
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.module.authentication.service.generateSecurePassword
import org.solyton.solawi.bid.module.values.Password
import org.solyton.solawi.bid.module.values.Username
import org.solyton.solawi.bid.pageobjects.module.user.UserManagementPage
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
        val username = Username("e2e-user-${System.currentTimeMillis()}@example.com")
        val password = Password(generateSecurePassword())
        val userPage = UserManagementPage(page)
        userPage.load()
        val user = userPage.createUserViaAdminUi(username, password)
        return user.username.value to user.password.value
    }

    @Test
    fun `create user modal opens when a late error dialog covers its trigger`() {
        browser.newContext().use { context ->
            val page = context.newPage()
            page.setContent(
                """
                <button data-id="user-management.create-user-button" disabled
                        onclick="document.getElementById('create-user').hidden = false">Create user</button>
                <div id="create-user" data-id="create-user-modal.modal" hidden>
                    <input data-id="create-user-modal.input.username">
                </div>
                <div id="error" style="position: fixed; inset: 0; z-index: 1" hidden>
                    <button data-id="null.modal.submit-button"
                            onclick="document.getElementById('error').remove()">Dismiss error</button>
                </div>
                <script>
                    setTimeout(() => {
                        document.getElementById('error').hidden = false;
                        document.querySelector('[data-id="user-management.create-user-button"]').disabled = false;
                    }, 200);
                </script>
                """.trimIndent()
            )

            val modal = UserManagementPage(page).openCreateUserModal()

            assertThat(modal.usernameInput()).isVisible()
        }
    }

    @Test
    fun test_createUser_appearsInUserList() {
        val context = adminContext()
        try {
            val page = context.newPage()
            val (username, _) = createUserViaAdminUi(page)

            val createdUserRow = page.waitForSelector("text=$username")

            Assertions.assertTrue(createdUserRow?.isVisible?:false, "The newly created user should show up in the user list.")
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
            page.getByDataId("login-form.input.password").fillPassword(password)
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
                usernameProperty?.isVisible?:false,
                "The private user page should display the logged-in user's own username."
            )
        } finally {
            context.close()
        }
    }
}