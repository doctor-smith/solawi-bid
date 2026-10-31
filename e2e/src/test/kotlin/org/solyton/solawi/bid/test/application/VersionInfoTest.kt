package org.solyton.solawi.bid.test.application

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.RequestOptions
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
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

                // Expected values from backend and frontend version endpoints
                val feResponse = page.request().get("${TestConfig.BASE_URL}/version.json")
                val feJson = Json.parseToJsonElement(feResponse.text()).jsonObject
                val expectedFeVersion = feJson["version"]?.jsonPrimitive?.content ?: ""
                val expectedFeCommit = feJson["commit"]?.jsonPrimitive?.content ?: ""

                val backendUrl = if (TestConfig.BASE_URL.contains("18080")) {
                    "http://localhost:18081"
                } else {
                    TestConfig.BASE_URL
                }
                val beResponse = page.request().get(
                    "$backendUrl/api/version",
                    RequestOptions.create().setHeader("CONTEXT", "solawi-bid")
                )
                val beJson = Json.parseToJsonElement(beResponse.text()).jsonObject
                val beData = beJson["data"]?.jsonObject
                val expectedBeVersion = beData?.get("version")?.jsonPrimitive?.content ?: ""
                val expectedBeCommit = beData?.get("commit")?.jsonPrimitive?.content ?: ""

                val expectedEnv = "DEV"
                val expectedUser = System.getenv("TEST_USER") ?: "owner@solyton.org"

                // 1. VersionInfo Element auf der Seite auffinden
                val versionInfoContainer = page.getByDataId("app.version-info")
                assertThat(versionInfoContainer).isVisible()

                // Label auf der Seite prüfen
                val labelText = versionInfoContainer.innerText()
                assertTrue(labelText.isNotBlank(), "Version label should not be blank")

                // 2. Klick auf das Label, um das Overlay zu öffnen
                versionInfoContainer.locator("div").first().click()

                // 3. Prüfen, ob das Overlay ("Version Info") sichtbar wird
                val overlayHeader = page.locator("text=Version Info")
                assertThat(overlayHeader).isVisible()

                // 4. Prüfen, ob Environment und Username im Overlay korrekt angezeigt werden
                val envValue = versionInfoContainer.locator("xpath=.//div[span[text()='Environment']]/span[2]")
                assertThat(envValue).hasText(expectedEnv)

                val userValue = versionInfoContainer.locator("xpath=.//div[span[text()='Username']]/span[2]")
                assertThat(userValue).hasText(expectedUser)

                // 5. Prüfen, ob Frontend- und Backend-Sektionen sowie Versions- und Commit-Zeilen vorhanden sind
                val frontendSection = page.locator("text=FRONTEND")
                assertThat(frontendSection).isVisible()

                val feVersionRow = versionInfoContainer.locator("xpath=.//span[text()='FRONTEND']/following-sibling::div[span[text()='Version']][1]")
                assertThat(feVersionRow).isVisible()

                val feCommitRow = versionInfoContainer.locator("xpath=.//span[text()='FRONTEND']/following-sibling::div[span[text()='Commit']][1]")
                assertThat(feCommitRow).isVisible()

                val backendSection = page.locator("text=BACKEND")
                assertThat(backendSection).isVisible()

                val beVersionRow = versionInfoContainer.locator("xpath=.//span[text()='BACKEND']/following-sibling::div[span[text()='Version']][1]")
                assertThat(beVersionRow).isVisible()

                val beCommitRow = versionInfoContainer.locator("xpath=.//span[text()='BACKEND']/following-sibling::div[span[text()='Commit']][1]")
                assertThat(beCommitRow).isVisible()

                // 6. Falls Version und Commit im Frontend angezeigt werden, prüfen ob sie mit den Endpunkten übereinstimmen
                val feVersionText = feVersionRow.locator("span").nth(1).innerText()
                if (feVersionText.isNotBlank()) {
                    assertEquals(expectedFeVersion, feVersionText)
                }

                val feCommitText = feCommitRow.locator("span").nth(1).innerText()
                if (feCommitText.isNotBlank()) {
                    assertEquals(expectedFeCommit.take(7), feCommitText)
                }

                val beVersionText = beVersionRow.locator("span").nth(1).innerText()
                if (beVersionText.isNotBlank()) {
                    assertEquals(expectedBeVersion, beVersionText)
                }

                val beCommitText = beCommitRow.locator("span").nth(1).innerText()
                if (beCommitText.isNotBlank()) {
                    assertEquals(expectedBeCommit.take(7), beCommitText)
                }

                page.shot("dev/x.png")
            }
        } finally {
            context.close()
        }
    }
}
