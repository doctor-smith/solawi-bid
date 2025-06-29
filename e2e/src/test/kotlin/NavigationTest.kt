import com.microsoft.playwright.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


class NavigationTest {

    companion object {
        private lateinit var playwright: Playwright
        private lateinit var browser: Browser

        private val FRONTEND_URL = System.getenv("FRONTEND_URL") ?: "http://localhost:8080"


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
        }
    }

    @Test
    fun test_useStoredState() {
        val jsonString = java.nio.file.Files.readString(TestUtils().storageStatePath)
        val context = browser.newContext(
            Browser.NewContextOptions().setStorageState(jsonString).setViewportSize(1280,720)
        )

        val page = context.newPage()

        page.navigate("$FRONTEND_URL/solyton/dashboard")

        assertTrue(page.url().contains("/dashboard"))

        context.close()
    }

    @Test
    fun test_useInvalidStoredState() {
        val invalidStorageState = """
        {
          "cookies": [],
          "origins": []
        }
    """.trimIndent()

        val context = browser.newContext(
            Browser.NewContextOptions().setStorageState(invalidStorageState)
        )

        val page = context.newPage()

        page.navigate("$FRONTEND_URL/solyton/dashboard")
        page.waitForURL("**/login")

        assertTrue(
            !page.url().contains("/dashboard"),
            "The user should not enter the dashboard with an invalid storage state."
        )

        context.close()
    }

}
