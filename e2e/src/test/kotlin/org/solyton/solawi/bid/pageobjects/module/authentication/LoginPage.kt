package org.solyton.solawi.bid.pageobjects.module.authentication

import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import org.solyton.solawi.bid.*
import org.solyton.solawi.bid.application.api.solawiApi
import org.solyton.solawi.bid.module.authentication.data.api.AccessToken
import org.solyton.solawi.bid.module.authentication.data.api.LoggedIn
import org.solyton.solawi.bid.module.authentication.data.api.Login
import org.solyton.solawi.bid.module.values.Password
import org.solyton.solawi.bid.module.values.Username

class LoginPage(private val page: Page) {

    val url = "${TestConfig.BASE_URL}/login"

    fun load(): Response = page.navigate(url)

    fun login(username: Username, password: Password): LoggedIn {
        page.put("login-form.input.username", username.value)
        page.put("login-form.input.password", password.value)

        val user = page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = Login::class
        ) {
            page.clickOn("login-form.submit-button")
        }.decodeBodyTo<LoggedIn>()
        return user
    }

    fun login(user: TestUser): TestUser {
        val data = login(user.username, user.password)
        return when(user) {
            is StdTestUser-> user.copy(
                accessToken = AccessToken(data.accessToken),
                refreshToken = data.refreshToken
            )
            is AdminTestUser-> user.copy(
                accessToken = AccessToken(data.accessToken),
                refreshToken = data.refreshToken
            )
        }
    }
}