package org.solyton.solawi.bid.pageobjects.module.user

import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import org.junit.jupiter.api.Assertions.assertTrue
import org.solyton.solawi.bid.StdTestUser
import org.solyton.solawi.bid.TestConfig
import org.solyton.solawi.bid.clickOn
import org.solyton.solawi.bid.module.values.Password
import org.solyton.solawi.bid.module.values.Username
import org.solyton.solawi.bid.put

class UserManagementPage(private val page: Page) {
    val url = "${TestConfig.BASE_URL}/app/management/users"

    fun load(): Response = page.navigate(url)

    /**
     *
     */
    fun createUserViaAdminUi(username: Username, password: Password, useTimestamp: Boolean = false): StdTestUser {
        // Usernames must be valid email addresses.

        assertTrue{ username.value.contains('@') }
        val realUsername = when{
            useTimestamp -> with(username){
                val name = username.value.takeWhile { it != '@' }
                val domain = username.value.dropWhile { it != '@' }
                "$name-${System.currentTimeMillis()}$domain"
            }
            else -> username.value
        }


        page.clickOn("user-management.create-user-button")

        page.put("create-user-modal.input.username", realUsername)
        page.put("create-user-modal.input.password", password.value)
        page.put("create-user-modal.input.repeat-password", password.value)
        page.clickOn("create-user-modal.modal.submit-button")

        return StdTestUser(Username(realUsername), password)
    }
}