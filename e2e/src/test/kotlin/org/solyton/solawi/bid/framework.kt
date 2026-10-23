package org.solyton.solawi.bid

import TestConfig
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.assertions.LocatorAssertions
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat

abstract class TestPage(
    private val page: Page,
    private val url: String,
    private val baseUrl: String = TestConfig.BASE_URL,
) {

    /**
     *
     */
    fun load(): Response = page.navigate("$baseUrl/$url")
}

abstract class TestComponent(internal open val locator: Locator){
    fun click() = locator.click()
}

class TestLink(override val locator: Locator): TestComponent(locator)

/*
open class TestDropdown(override val locator: Locator): TestComponent(locator) {
    fun toggle() = click()

}

 */

fun assertThat(component: TestComponent): LocatorAssertions = assertThat(component.locator)

