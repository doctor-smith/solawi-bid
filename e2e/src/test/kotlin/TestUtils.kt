import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class TestUtils {

    val storageStatePath: Path = Paths.get("loggedInState.json")
    private lateinit var user: String
    private lateinit var password: String

    fun getCookie(browser: Browser){

        val context = browser.newContext()
        val page = context.newPage()

        // get data from environment
        user = System.getenv("TEST_USER")
        password = System.getenv("TEST_USER_PASSWORD")

        try {
            page.navigate("http://localhost:8080/login")
            page.clickOn("cookie-disclaimer.modal.submit-button")

            page.put("login-form.input.username", user)
            page.put("login-form.input.password", password)
            page.clickOn("login-form.submit-button")
            page.waitForURL("**/dashboard")

            context.storageState(
                BrowserContext.StorageStateOptions().setPath(storageStatePath)
            )
        } finally {
            context.close()
        }
    }

    fun deleteStorageStateFile() {
        try {
            Files.deleteIfExists(storageStatePath)
        } catch (e: Exception) {
            println("Error deleting storage state file: ${e.message}")
        }
    }

}