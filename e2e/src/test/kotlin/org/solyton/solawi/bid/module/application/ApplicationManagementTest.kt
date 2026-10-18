package org.solyton.solawi.bid.module.application

import TestConfig
import TestUtils
import clickOn
import com.microsoft.playwright.*
import com.microsoft.playwright.options.WaitForSelectorState
import getByDataId
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import waitForLoadingToFinish
import java.nio.file.Files

/**
 * Convenient E2E tests for the application module, covering:
 * - the admin "application management" page being reachable and listing applications
 * - navigating from an application's details into its modules and back
 * - the (currently not implemented) "edit application" action showing its placeholder dialog
 * - the personal "my applications" page being reachable
 *
 * Since the texts shown on these pages come from a dynamically configurable i18n backend,
 * these tests deliberately avoid asserting on translated texts. Instead, they rely on the
 * `data-id` attributes exposed by the action buttons, and on URL changes to verify behaviour,
 * the same way [TestUtils] and the other e2e tests rely on `data-id` attributes rather than
 * on rendered copy.
 */
class ApplicationManagementTest {

    companion object {
        private lateinit var playwright: Playwright
        private lateinit var browser: Browser

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            playwright = Playwright.create()
            browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))
            // Logs in once as TEST_USER (the application owner, who may manage applications)
            // and stores the resulting session, so every test here can reuse it.
            TestUtils().getCookie(browser)
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            browser.close()
            playwright.close()
            TestUtils().deleteStorageStateFile()
        }
    }

    // A fresh context, already logged in as TEST_USER via the stored session.
    private fun adminContext(): BrowserContext {
        val storedState = Files.readString(TestUtils().storageStatePath)
        return browser.newContext(
            Browser.NewContextOptions().setStorageState(storedState)
        )
    }

    /**
     * Navigating straight to one of these pages can briefly flash an unrelated
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
    fun test_applicationManagementPage_listsApplications() {
        val context = adminContext()
        try {
            val page = context.newPage()
            page.navigate("${TestConfig.BASE_URL}/app/management")
            dismissStrayErrorDialogIfPresent(page)
            page.waitForLoadingToFinish()

            // The "APPLICATION_MANAGEMENT" application is always present (it is seeded by a
            // migration), so its "show details" button must be visible once the page has
            // finished loading.
            val detailsButton = page.getByDataId("application-management.page.application.APPLICATION_MANAGEMENT.details-button")
            detailsButton.first().waitFor(
                Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
            )

            Assertions.assertTrue(
                detailsButton.first().isVisible,
                "The application management page should list at least one application."
            )
            Assertions.assertTrue(
                page.url().contains("/app/management"),
                "The admin user should not be redirected away from the application management page."
            )
        } finally {
            context.close()
        }
    }

    @Test
    fun test_applicationManagementPage_detailsButton_navigatesToApplicationDetailsAndBack() {
        val context = adminContext()
        try {
            val page = context.newPage()
            page.navigate("${TestConfig.BASE_URL}/app/management")
            dismissStrayErrorDialogIfPresent(page)
            page.waitForLoadingToFinish()

            val detailsButton = page.getByDataId("application-management.page.application.APPLICATION_MANAGEMENT.details-button")
            detailsButton.first().waitFor(
                Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
            )

            page.clickOn("application-management.page.application.APPLICATION_MANAGEMENT.details-button")
            page.waitForURL("**/app/management/application/*")
            // The application details page fetches its data asynchronously and shows a full-page
            // loading overlay in the meantime; wait for it to disappear before interacting with
            // the page, otherwise the back button click below can be swallowed by the overlay.
            page.waitForLoadingToFinish()

            Assertions.assertTrue(
                page.url().contains("/app/management/application/"),
                "Clicking an application's details button should navigate to its details page."
            )

            val backButton = page.getByDataId("application.page.back-button")
            backButton.first().waitFor(
                Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
            )

            // Navigate back to the application management page via the "back" button.
            page.clickOn("application.page.back-button")
            page.waitForURL("**/app/management")

            Assertions.assertTrue(
                page.url().endsWith("/app/management"),
                "The back button on the application details page should return to the application list."
            )
        } finally {
            context.close()
        }
    }

    @Test
    fun test_applicationManagementPage_editButton_showsNotImplementedDialog() {
        val context = adminContext()
        try {
            val page = context.newPage()
            page.navigate("${TestConfig.BASE_URL}/app/management")
            dismissStrayErrorDialogIfPresent(page)
            page.waitForLoadingToFinish()

            page.clickOn("application-management.page.application.APPLICATION_MANAGEMENT.edit-button")

            // The dialog itself does not expose the bare "...not-implemented" data-id; only its
            // content wrapper and submit button do (see ConfirmationDialog/showDialogModal).
            val dialog = page.getByDataId("application-management.page.edit-application.not-implemented.modal.content-wrapper")
            dialog.first().waitFor(
                Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
            )
            Assertions.assertTrue(
                dialog.first().isVisible,
                "Editing an application's properties is not implemented yet, the placeholder dialog should show up instead."
            )

            page.clickOn("application-management.page.edit-application.not-implemented.modal.submit-button")
        } finally {
            context.close()
        }
    }

    @Test
    fun test_privateApplicationManagementPage_isAccessibleToAdmin() {
        val context = adminContext()
        try {
            val page = context.newPage()
            page.navigate("${TestConfig.BASE_URL}/app/management/private")
            dismissStrayErrorDialogIfPresent(page)
            page.waitForLoadingToFinish()

            // The personal applications page always renders its "booked applications" list
            // title, even when the list itself happens to be empty.
            val title = page.waitForSelector("h3")

            Assertions.assertTrue(title.isVisible, "The personal application management page should render.")
            Assertions.assertTrue(
                page.url().contains("/app/management/private"),
                "The admin user should not be redirected away from the personal application management page."
            )
        } finally {
            context.close()
        }
    }
}
