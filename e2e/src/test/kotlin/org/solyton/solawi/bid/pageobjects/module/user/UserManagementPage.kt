package org.solyton.solawi.bid.pageobjects.module.user

import com.microsoft.playwright.Locator
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

    class CreateUserModal(
        override val locator: Locator,
        override val trigger: Locator,
    ) : TestModal(locator, trigger) {
        companion object {
            const val BASE_ID = "create-user-modal"
        }

        override fun submitButton(): Locator = locator.getByDataId("$BASE_ID.modal.submit-button")
        override fun cancelButton(): Locator = locator.getByDataId("$BASE_ID.modal.cancel-button")
        override fun closeButton(): Locator = locator.getByDataId("$BASE_ID.modal.close-button")

        fun usernameInput(): Locator = locator.getByDataId("$BASE_ID.input.username")
        fun passwordInput(): Locator = locator.getByDataId("$BASE_ID.input.password")
        fun repeatPasswordInput(): Locator = locator.getByDataId("$BASE_ID.input.repeat-password")
    }

    @Suppress("FunctionName")
    fun CreateUserModal(): CreateUserModal =
        CreateUserModal(
            page.getByDataId("create-user-modal.modal"),
            page.getByDataId("user-management.create-user-button")
        )

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

        val modal = CreateUserModal()
        modal.trigger.click()

        modal.usernameInput().fill(realUsername)
        modal.passwordInput().fill(password.value)
        modal.repeatPasswordInput().fill(password.value)

        page.waitForApi(
            api = solawiApi,
            keyOfEndpoint = CreateUser::class
        ) {
            modal.submitButton().click()
        }.decodeBodyTo<ApiUser>()

        return StdTestUser(Username(realUsername), password)
    }
}