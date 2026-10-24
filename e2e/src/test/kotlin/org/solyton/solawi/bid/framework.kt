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

    fun clickWhen(predicate: (Locator)-> Boolean) = run { if(predicate(locator)) click() }

}

class TestLink(override val locator: Locator): TestComponent(locator)


abstract class TestDropdown(
    override val locator: Locator,
    val options: Map<String,()->Locator>,
): TestComponent(locator) {
    /**
     * Opens the dropdown component, making its options visible and accessible for interaction.
     * This method should be implemented to handle the logic required to display the dropdown contents.
     */
    abstract fun open()

    /**
     * Closes the dropdown component, hiding its options and making it inaccessible for interaction.
     * This method is responsible for handling the logic required to collapse the dropdown contents.
     */
    abstract fun close()
    abstract fun isExpanded(): Boolean

    /**
     * Retrieves the locator for the element that triggers the dropdown to open.
     *
     * @return The Locator instance associated with the dropdown trigger element.
     */
    abstract fun trigger(): Locator

    /**
     * Selects the specified option from the dropdown. If the dropdown is not expanded, it opens
     * the dropdown first. By default, the dropdown will close after the selection unless
     * specified otherwise.
     *
     * @param option The name of the option to be selected from the dropdown. Must match an entry in the options map.
     * @param closeOnSelect Determines whether the dropdown should close automatically after selecting the option. Defaults to true.
     * @throws IllegalArgumentException If the specified option is not found in the options map.
     */
    open fun select(option: String, closeOnSelect: Boolean = true) {
        if(!isExpanded()) open()
        val optionLocator = options[option]?.invoke()
        requireNotNull(optionLocator)
        assertThat(optionLocator).isVisible()
        optionLocator.click()

        if(!isExpanded() && closeOnSelect) close()
    }
}

abstract class TestAccordion (
    override val locator: Locator
): TestComponent(locator) {
    abstract fun open()
    abstract fun close()
    abstract fun isOpened(): Boolean
    abstract fun trigger(): Locator

}

fun assertThat(component: TestComponent): LocatorAssertions = assertThat(component.locator)

interface TestWorkflow<in S, out T> {
    val context: TestUserContext
    fun TestUserContext.runWith(input: S): T

    fun runWith(input: S): T = context.runWith(input)
}

infix fun <S, T> Any?.run(workflow: TestWorkflow<S,T>): (S) -> T = {s:S -> workflow.runWith(s)}
infix fun <S, T> ((S) -> T).withInput(input: S): T = this(input)


