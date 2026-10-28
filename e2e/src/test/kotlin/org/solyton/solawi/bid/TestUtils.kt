package org.solyton.solawi.bid

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class TestUtils {


    val storageStatePath: Path
        get() = storageStatePath(null)

    fun storageStatePath(of: String? = null): Path {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        if (!File(dir, "e2e").exists() && dir.name != "e2e") {
            // Falls aus einem Unterordner gestartet
            while (dir.parentFile != null && !File(dir, "e2e").exists()) {
                dir = dir.parentFile
            }
        }
        val e2eDir = if (dir.name == "e2e") dir else File(dir, "e2e")
        val base = File(e2eDir, "build/playwright").toPath().toAbsolutePath().normalize()

        val targetDir = if (of == null) base.resolve("default") else base.resolve(of)
        Files.createDirectories(targetDir)

        return targetDir.resolve("loggedInState.json")
    }

    fun getCookie(browser: Browser, of: String? = null) {
        val target = storageStatePath(of)
        val context = browser.newContext()

        context.use { context ->
            val page = context.newPage()

            val user = System.getenv("TEST_USER")
            val password = System.getenv("TEST_USER_PASSWORD")

            page.navigate("${TestConfig.BASE_URL}/login")
            page.clickOn("cookie-disclaimer.modal.submit-button")
            /*
            page.clickIfPresent(
                "cookie-disclaimer.modal.submit-button",
                3_000.0
            )

             */

            val usernameInput = page.getByDataId("login-form.input.username")
            assertThat(usernameInput).isVisible()
            usernameInput.fill(user)

            val passwordInput = page.getByDataId("login-form.input.password",)
            assertThat(passwordInput).isVisible()
            passwordInput.fill(password)

            val submitButton = page.getByDataId("login-form.submit-button")
            assertThat(submitButton).isVisible()
            submitButton.click()

            page.waitForURL("**/dashboard")

            context.storageState(
                BrowserContext.StorageStateOptions().setPath(target)
            )
        }
    }

    fun deleteStorageStateFile(of: String? = null) {
        val target = storageStatePath(of)
        try {
            Files.deleteIfExists(target)
        } catch (e: Exception) {
            println("Error deleting storage state file: ${e.message}")
        }
    }
}