import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class TestUtils {

    val storageStatePath: Path = Paths.get("loggedInState.json")

    fun getCookie(browser: Browser){

        val context = browser.newContext()
        val page = context.newPage()

        try {
            page.navigate("http://localhost:8080/login")
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Ok")).click()
            page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Nutzername")).fill("owner@solyton.org")
            page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Passwort")).fill("4l=6mx4Vinz-DT")
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Login")).click()
            page.waitForURL("**/dashboard")

            // Storage-State speichern (Cookies, LocalStorage, etc.)
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