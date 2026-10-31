package org.solyton.solawi.bid.test.application

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.*
import java.nio.file.Files

class VersionInfoTest {

    companion object {
        const val STORAGE_PATH = "VersionInfoTest"
        private lateinit var playwright: Playwright
        private lateinit var browser: Browser

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            playwright = Playwright.create()
            browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))
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

    @Smoke
    @Test
    fun test_versionInfo_labelAndOverlayDisplaysCorrectData() {
        val storedState = Files.readString(TestUtils().storageStatePath(STORAGE_PATH))
        val context = browser.newContext(
            Browser.NewContextOptions().setStorageState(storedState)
        )

        try {
            val page: Page = context.newPage()
            page.withFailureDiagnostics("VersionInfoTest") {
                page.navigate("${TestConfig.BASE_URL}/app/dashboard")
                page.waitForLoadingToFinish()

                // 1. VersionInfo Element auf der Seite auffinden
                val versionInfoContainer = page.getByDataId("app.version-info")
                assertThat(versionInfoContainer).isVisible()

                // Das Label sollte Text enthalten (z. B. "DEV · 1.0.0 · abc1234")
                val labelText = versionInfoContainer.innerText()
                assertTrue(labelText.isNotBlank(), "Version label should not be blank")

                // 2. Klick auf das Label, um das Overlay zu öffnen
                versionInfoContainer.locator("div").first().click()

                // 3. Prüfen, ob das Overlay ("Version Info") sichtbar wird
                val overlayHeader = page.locator("text=Version Info")
                assertThat(overlayHeader).isVisible()

                // 4. Prüfen, ob Frontend- und Backend-Labels sowie Versions-Einträge vorhanden sind
                val frontendSection = page.locator("text=FRONTEND")
                assertThat(frontendSection).isVisible()

                val backendSection = page.locator("text=BACKEND")
                assertThat(backendSection).isVisible()

                val overlayText = versionInfoContainer.innerText()
                assertTrue(overlayText.contains("FRONTEND"), "Overlay must contain FRONTEND section")
                assertTrue(overlayText.contains("BACKEND"), "Overlay must contain BACKEND section")
                assertTrue(overlayText.contains("Version"), "Overlay must show version row")
                assertTrue(overlayText.contains("Commit"), "Overlay must show commit row")
            }
        } finally {
            context.close()
        }
    }
}
