import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class TestUtils {

    var numberOfUsers = 0

    val storageStatePath: Path = Paths.get("loggedInState.json")
    private lateinit var user: String
    private lateinit var password: String

    fun getCookie(browser: Browser, of: String? = null){

        numberOfUsers++

        val context = browser.newContext()
        val page = context.newPage()

        // get data from environment
        user = System.getenv("TEST_USER")
        password = System.getenv("TEST_USER_PASSWORD")

        try {
            page.navigate("${TestConfig.BASE_URL}/login")
            page.clickOn("cookie-disclaimer.modal.submit-button")

            page.put("login-form.input.username", user)
            page.put("login-form.input.password", password)
            page.clickOn("login-form.submit-button")
            page.waitForURL("**/dashboard")

            val storagePath = when{
                of == null -> storageStatePath
                else -> Paths.get("$of/loggedInState.json")
            }

            context.storageState(
                BrowserContext.StorageStateOptions().setPath(storagePath)
            )
        } finally {
            context.close()
        }
    }

    fun deleteStorageStateFile(of: String? = null) {
        try {
            val storagePath = when{
                of == null -> storageStatePath
                else -> Paths.get("$of/loggedInState.json")
            }
            when {
                of == null && numberOfUsers == 1 -> Files.deleteIfExists(storagePath)
                of != null -> Files.deleteIfExists(storagePath)
            }
            numberOfUsers--
        } catch (e: Exception) {
            println("Error deleting storage state file: ${e.message}")
        }
    }

}