import com.microsoft.playwright.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class LoginTest {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var page: Page

    @BeforeEach
    fun beforeEach() {
        println("Starting test setup...")
        playwright = Playwright.create()
        browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))
        page = browser.newPage()
    }

    @AfterEach
    fun afterEach() {
        browser.close()
        playwright.close()
    }

    @Test
    fun test_login() {
        page.navigate("http://localhost:8080/login")
        page.screenshot(Page.ScreenshotOptions().setPath(Paths.get("screenshot_before_click.png")))
        val html = page.content()
        println("Page HTML snapshot:\n$html")


        try {
            page.waitForSelector("[data-id='cookie-disclaimer.modal.submit-button']", Page.WaitForSelectorOptions().setTimeout(
                10000.0
            ))
            println("Cookie Button ist jetzt sichtbar")
            page.click("[data-id='cookie-disclaimer.modal.submit-button']")
        } catch (e: TimeoutError) {
            println("Cookie Button erschien nicht, überspringe Klick")
        }

        //page.click("[data-id='cookie-disclaimer.modal.submit-button']")
        page.fill("[data-id='login-form.input.username']", "owner@solyton.org")
        page.fill("[data-id='login-form.input.password']", "4l=6mx4Vinz-DT")
        page.click("[data-id='login-form.submit-button']")

        page.waitForURL("**/dashboard")

        val currentUrl = page.url()
        assertTrue(currentUrl.contains("/dashboard"), "User wurde nicht zum Dashboard weitergeleitet.")
    }

    @Test
    fun test_login_userDoesNotExist() {
        page.navigate("http://localhost:8080/login")

        page.click("[data-id='cookie-disclaimer.modal.submit-button']")
        page.fill("[data-id='login-form.input.username']", "Tom")
        page.fill("[data-id='login-form.input.password']", "1234")
        page.click("[data-id='login-form.submit-button']")

        val errorMessage = page.waitForSelector("text=User with username Tom does not exists")

        assertTrue(errorMessage.isVisible, "The error message was not displayed as expected.")
    }

}
