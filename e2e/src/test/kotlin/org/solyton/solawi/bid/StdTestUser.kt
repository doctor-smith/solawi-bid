package org.solyton.solawi.bid

import TestUtils
import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import org.solyton.solawi.bid.module.values.Password
import org.solyton.solawi.bid.module.values.Username
import java.nio.file.Files

sealed interface TestUser {
    val username: Username
    val password: Password

}

data class StdTestUser(
    override val username: Username,
    override val password: Password
) : TestUser

data class AdminTestUser(
    override val username: Username,
    override val password: Password
) : TestUser

data class TestUserContext(
    val user: TestUser,
    val page: Page,
    val context: BrowserContext
) : AutoCloseable {
    override fun close() {
        context.close()
    }

    fun switchUser(user: TestUser) = copy(user = user)

    // fun withUser
}

fun <T> withUser(userContext: TestUserContext, block: TestUserContext.() -> T): T {
    return userContext.use { _ ->
        userContext.block()
    }
}

infix fun AdminTestUser.on(browser: Browser): TestUserContext {
    val storedState = Files.readString(TestUtils().storageStatePath)
    val context = browser.newContext(
        Browser.NewContextOptions().setStorageState(storedState)
    )
    val page = context.newPage()
    return TestUserContext(this, page, context)
}

infix fun StdTestUser.on(browser: Browser): TestUserContext {
    val context = browser.newContext()
    val page = context.newPage()
    return TestUserContext(this, page, context)
}