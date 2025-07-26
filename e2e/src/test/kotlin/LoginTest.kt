import com.microsoft.playwright.*
import com.microsoft.playwright.options.AriaRole
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LoginTest {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var page: Page
    private lateinit var user: String
    private lateinit var password: String

    @BeforeEach
    fun beforeEach() {
        println("Starting test setup...")
        playwright = Playwright.create()
        browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))
        page = browser.newPage()
        user = System.getenv("TEST_USER")
        password = System.getenv("TEST_USER_PASSWORD")
    }

    @AfterEach
    fun afterEach() {
        browser.close()
        playwright.close()
    }

    @Test
    fun test_login() {
        page.navigate("http://localhost:8080/login")
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Ok")).click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Nutzername")).fill(user)
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Passwort")).fill(password)
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Login")).click()

        page.waitForURL("**/dashboard")

        val currentUrl = page.url()
        assertTrue(currentUrl.contains("/dashboard"), "User was not redirected to the dashboard after login.")
    }

    @Test
    fun test_login_userDoesNotExist() {
        page.navigate("http://localhost:8080/login")

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Ok")).click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Nutzername")).fill("Tom")
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Passwort")).fill("1234")
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Login")).click()

        val errorMessage = page.waitForSelector("text=User with username Tom does not exists")

        assertTrue(errorMessage.isVisible, "The error message was not displayed as expected.")
    }

}