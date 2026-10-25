package org.solyton.solawi.bid.pageobjects.shared

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.solyton.solawi.bid.getByDataId
import org.solyton.solawi.bid.waitForLoadingToFinish

class NavBar(private val page: Page) {

    fun dashboardButton(): Locator =
        page.getByDataId("navbar.apps-button")

    fun moveToDashboard() {
        val button = dashboardButton()
        assertThat(button).isVisible()
        button.click()
        page.waitForLoadingToFinish()
    }

    fun personalDropdown(): Locator =
        page.getByDataId("navbar.personal-dropdown")

    fun logout() {
        val dropdown = personalDropdown()
        assertThat(dropdown).isVisible()
        dropdown.click()
        page.getByDataId("navbar.personal-dropdown.option.logout").click()
        page.waitForLoadingToFinish()
    }

    fun moveToMyData() {
        val dropdown = personalDropdown()
        assertThat(dropdown).isVisible()
        dropdown.click()
        page.getByDataId("navbar.personal-dropdown.option.my-data").click()
        page.waitForLoadingToFinish()
    }
}