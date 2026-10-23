package org.solyton.solawi.bid.pageobjects.module.authentication

import TestConfig
import clickOn
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import org.solyton.solawi.bid.TestUser
import org.solyton.solawi.bid.module.values.Password
import org.solyton.solawi.bid.module.values.Username
import put

class LoginPage(private val page: Page) {

    val url = "${TestConfig.BASE_URL}/login"

    fun load(): Response = page.navigate(url)

    fun login(username: Username, password: Password) {
        page.put("login-form.input.username", username.value)
        page.put("login-form.input.password", password.value)
        page.clickOn("login-form.submit-button")
    }

    fun login(user: TestUser) = login(user.username, user.password)

}