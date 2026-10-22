package org.solyton.solawi.bid.test.application

import TestConfig
import TestUtils
import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Smoke
import java.nio.file.Files

class NavigationTest {

    companion object {
        private lateinit var playwright: Playwright
        private lateinit var browser: Browser


        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            playwright = Playwright.create()
            browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))

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

    @Smoke
    @Test
    fun test_useStoredState() {
        val jsonString = Files.readString(TestUtils().storageStatePath)
        val context = browser.newContext(
            Browser.NewContextOptions().setStorageState(jsonString)
        )

        val page = context.newPage()

        page.navigate("${TestConfig.BASE_URL}/app/dashboard")

        Assertions.assertTrue(page.url().contains("/dashboard"))

        context.close()
    }

    // @Test
    fun test_useInvalidStoredState() {
        val invalidStorageState = """
        {
          "cookies": [],
          "origins": []
        }
    """

        val context = browser.newContext(
            Browser.NewContextOptions().setStorageState(invalidStorageState)
        )

        val page = context.newPage()

        page.navigate("${TestConfig.BASE_URL}/app/dashboard")
        // Increase timeout and use a more specific pattern
        page.waitForURL("${TestConfig.BASE_URL}/app/login", Page.WaitForURLOptions().setTimeout(15000.0))

        Assertions.assertTrue(
            !page.url().contains("/dashboard"), "The user should not enter the dashboard with an invalid storage state."
        )

        context.close()
    }

}