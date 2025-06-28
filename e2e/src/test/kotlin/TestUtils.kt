import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import java.nio.file.Path
import java.nio.file.Paths

class TestUtils {

    val storageStatePath: Path = Paths.get("loggedInState.json")

    fun getCookie(browser: Browser){

            val context = browser.newContext()
            val page = context.newPage()

            try {
                page.navigate("http://localhost:8080/login")
                page.click("[data-id='cookie-disclaimer.modal.submit-button']")
                page.fill("[data-id='login-form.input.username']", "owner@solyton.org")
                page.fill("[data-id='login-form.input.password']", "4l=6mx4Vinz-DT")
                page.click("[data-id='login-form.submit-button']")
                page.waitForURL("**/dashboard")

                // Storage-State speichern (Cookies, LocalStorage, etc.)
                context.storageState(
                    BrowserContext.StorageStateOptions().setPath(storageStatePath)
                )
            } finally {
                context.close()
            }
    }

}