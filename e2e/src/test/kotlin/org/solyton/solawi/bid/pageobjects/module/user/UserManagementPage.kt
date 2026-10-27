package org.solyton.solawi.bid.pageobjects.module.user

import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import org.junit.jupiter.api.Assertions.assertTrue
import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.application.api.solawiApi
import org.solyton.solawi.bid.module.user.data.api.ApiUser
import org.solyton.solawi.bid.module.user.data.api.CreateUser
import org.solyton.solawi.bid.module.values.Password
import org.solyton.solawi.bid.module.values.Username

class UserManagementPage(private val page: Page) {
    val url = "${TestConfig.BASE_URL}/app/management/users"

    fun load(): Response? = page.navigate(url)

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

        page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = CreateUser::class
        ) {
            page.clickOn("create-user-modal.modal.submit-button")
        }.decodeBodyTo<ApiUser>()

        return StdTestUser(Username(realUsername), password)
    }
}