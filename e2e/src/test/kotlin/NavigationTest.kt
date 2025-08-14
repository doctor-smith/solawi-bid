import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
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

    @Test
    fun test_useStoredState() {
        val jsonString = Files.readString(TestUtils().storageStatePath)
        val context = browser.newContext(
            Browser.NewContextOptions().setStorageState(jsonString)
        )

        val page = context.newPage()

        page.navigate("http://localhost:8080/solyton/dashboard")

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
    """

        val context = browser.newContext(
            Browser.NewContextOptions().setStorageState(invalidStorageState)
        )

        val page = context.newPage()

        page.navigate("http://localhost:8080/solyton/dashboard")
        page.waitForURL("**/login")

        assertTrue(
            !page.url().contains("/dashboard"), "The user should not enter the dashboard with an invalid storage state."
        )

        context.close()
    }

}
