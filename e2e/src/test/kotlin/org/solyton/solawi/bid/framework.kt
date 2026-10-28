package org.solyton.solawi.bid

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.assertions.LocatorAssertions
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.evoleq.math.x

/**
 * Represents an abstract base class for a test page that provides functionality
 * for loading a specific URL in the context of a given page instance.
 *
 * @param page The Page instance used to execute navigation and other actions.
 * @param url The relative URL of the page to be loaded.
 * @param baseUrl The base URL to which the relative URL will be appended, defaulting to TestConfig.BASE_URL.
 */
abstract class TestPage(
    private val page: Page,
    private val url: String,
    private val baseUrl: String = TestConfig.BASE_URL,
) {

    /**
     *
     */
    fun load(): Response? = page.navigate("$baseUrl/$url")
}

/**
 * Represents an abstract class that serves as a base component for interaction with a specific UI element
 * identified by a [Locator].
 *
 * @property locator The locator used to identify and interact with the component.
 */
abstract class TestComponent(internal open val locator: Locator){

    fun click() = locator.click()

    fun clickWhen(predicate: (Locator)-> Boolean) = run { if(predicate(locator)) click() }

}

/**
 * Represents a testable link component that extends the functionality of the base TestComponent class.
 *
 * @constructor Creates an instance of TestLink with the specified locator.
 * @param locator The Locator associated with the test link component, used for identifying and interacting with the link element.
 */
class TestLink(override val locator: Locator): TestComponent(locator)


/**
 * Represents an abstract base class for a dropdown component in a user interface. It provides
 * functionality for interacting with a dropdown, including opening, closing, checking its
 * state, and selecting options.
 *
 * @property locator The Locator instance used to identify the dropdown component in the UI.
 * @property options A map representing the available options in the dropdown. Each map entry
 * maps the option name (key) to a function that returns the corresponding Locator for the option (value).
 */
abstract class TestDropdown(
    override val locator: Locator,
    open val options: Map<String,()->Locator>,
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

    /**
     * Checks whether the dropdown is currently expanded and its options are visible.
     *
     * @return true if the dropdown is expanded; false otherwise.
     */
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
     * If the option is already selected, the function returns without performing any action.
     *
     * @param option The name of the option to be selected from the dropdown. Must match an entry in the options map.
     * @param closeOnSelect Determines whether the dropdown should close automatically after selecting the option. Defaults to true.
     * @throws IllegalArgumentException If the specified option is not found in the options map.
     */
    open fun select(option: String, closeOnSelect: Boolean = true) {

        if(isSelected(option)) return
        if(!isExpanded()) open()

        val optionLocator = options[option]?.invoke()
        requireNotNull(optionLocator)
        assertThat(optionLocator).isVisible()

        optionLocator.click()

        if(isExpanded() && closeOnSelect) close()
    }

    open fun isSelected(option: String): Boolean = trigger().textContent() == option

    companion object Builder {
         val option: Locator.(String)-> Pair<String, ()-> Locator> = {item->  item x { getByPartialDataId(".option.$item") }}
    }
}

/**
 * A concrete implementation of the TestDropdown abstract class, representing a default dropdown
 * component in the user interface. This implementation provides behavior for opening, closing,
 * and checking the expanded state of the dropdown, as well as retrieving its trigger element.
 *
 * @constructor Initializes the DefaultTestDropdown with the specified locator and options map.
 * @param locator The Locator instance used to identify the dropdown component in the UI.
 * @param options A map of dropdown options where each entry maps the option name (key) to
 * a function that returns the corresponding Locator instance (value).
 */
open class DefaultTestDropdown(
    /**
     * Needs to point to $dataId.wrapper, as the trigger owns the dataId. See [DefaultTestDropdown.trigger]
     */
    override val locator: Locator,
    /**
     * All options need to point to dataId.option.<NAME>
     * Hint. One can use the option builder [TestDropdown.Builder.option] for convenient option generation
     */
    override val options: Map<String, () -> Locator>
): TestDropdown(
    locator,
    options
) {
    override fun open() {
        if(!isExpanded()) trigger().click()
    }
    override fun close() {
        if(isExpanded()) trigger().click()
    }

    override fun isExpanded(): Boolean {
        val expanded = locator.getAttribute("expanded")
        return expanded == "true"
    }

    override fun trigger(): Locator = with(locator.getAttribute("data-id")){
        val wrapperId = requireNotNull(this) {
            "DefaultTestDropdown: The main locator has no data-id"
        }

        // TODO Here we compute the triggerId in a complicated way. This is due to the fact, that the dataId structure
        //  of the [Dropdown] is messed up. But this messed up structure is used widely in the tests by now. So the fix
        //  has been postponed. Later, the .wrapper suffix should be removed from the dataId structure and be replaced
        //  by dataId, while the trigger gets dataId trigger (or dataId.trigger). The options can remain as the are
        //  by now or be reduced to option.<NAME>
        val suffix = ".wrapper"
        val triggerId = when {
            wrapperId.endsWith(suffix) -> wrapperId.dropLast(suffix.length)
            else -> "$wrapperId.trigger"
        }
        locator.getByDataId(triggerId)
    }

}

/**
 * Represents an abstract accordion UI component that can be opened and closed.
 * It extends the base functionality provided by [TestComponent].
 *
 * @constructor Initializes the accordion with the given UI element [locator].
 */
abstract class TestAccordion (
    override val locator: Locator
): TestComponent(locator) {
    abstract fun open()
    abstract fun close()
    abstract fun isOpened(): Boolean
    abstract fun trigger(): Locator

}

/**
 * Represents a default implementation of the [TestAccordion], providing functionality
 * to interact with an accordion UI component.
 *
 * The class defines the behavior for opening and closing the accordion,
 * and checks whether the accordion is currently opened or closed
 * based on its attributes.
 *
 * @constructor Initializes the instance with the given [locator] representing the UI element.
 */
abstract class DefaultTestAccordion(override val locator: Locator): TestAccordion(locator) {

    /**
     * Checks whether the accordion is currently opened.
     *
     * The method evaluates the "opened" attribute of the accordion's trigger element.
     * It returns true if the attribute's value is "true", indicating that the accordion is open.
     *
     * @return true if the accordion is opened, false otherwise.
     */
    override fun isOpened(): Boolean =
        trigger().getAttribute("opened") == "true"

    /**
     * Opens the accordion if it is not already open.
     *
     * This method checks the current state of the accordion by invoking [isOpened].
     * If the accordion is not open, it triggers a click action on the accordion's toggle control
     * by calling [trigger] and performing the click operation.
     */
    override fun open(){
        if(!isOpened()) trigger().click()
    }

    /**
     * Closes the accordion if it is currently open.
     *
     * This method checks the state of the accordion using [isOpened]. If the accordion is open,
     * it performs a click action on the accordion's trigger element, represented by [trigger],
     * to toggle its state to closed.
     */
    override fun close() {
        if(isOpened()) trigger().click()
    }
}

/**
 * Represents an abstract modal component for UI interactions. A modal is a dialog box
 * displayed on top of the main content, typically used for user confirmations or input.
 *
 * IMPORTANT: usually, the modal is found on the underlying page!!
 *
 * @constructor Initializes the modal with the [locator] to identify the modal itself and
 * the [trigger] to interact with the element responsible for opening the modal.
 *
 * @property locator The locator used to identify and interact with the modal.
 * @property trigger The locator used to identify and interact with the trigger element
 * responsible for opening the modal.
 */
abstract class TestModal(
    override val locator: Locator,
    open val trigger: Locator
): TestComponent(locator) {
    fun open() = trigger().click()
    fun close() = closeButton().click()
    fun submit() = submitButton().click()
    fun cancel() = cancelButton().click()

    fun trigger(): Locator = trigger
    abstract fun submitButton(): Locator
    abstract fun cancelButton(): Locator
    abstract fun closeButton(): Locator

}

fun assertThat(component: TestComponent): LocatorAssertions = assertThat(component.locator)


/**
 * Represents a workflow interface that operates in a specific user context, allowing
 * the execution of a task with the provided input and producing a result of type `T`.
 *
 * @param S The type of input data this workflow expects.
 * @param T The type of result data this workflow produces.
 */
interface TestWorkflow<in S, out T> {
    val context: TestUserContext
    fun TestUserContext.runWith(input: S): T

    fun runWith(input: S): T = context.runWith(input)

    fun <R> map(transform: (T) -> R): TestWorkflow<S, R> = object : TestWorkflow<S, R> {
        override val context: TestUserContext = this@TestWorkflow.context
        override fun TestUserContext.runWith(input: S): R = transform(this@TestWorkflow.runWith(input))
    }

    fun <R> contraMap(transform: (R)->S): TestWorkflow<R, T> = object : TestWorkflow<R, T> {
        override val context: TestUserContext = this@TestWorkflow.context
        override fun TestUserContext.runWith(input: R): T = this@TestWorkflow.runWith(transform(input))
    }

    companion object {
        /**
         * Monad Return
         */
        fun <S, T> Return(context: TestUserContext, t: T): TestWorkflow<S, T> = object : TestWorkflow<S, T> {
            override val context: TestUserContext = context
            override fun TestUserContext.runWith(input: S): T = t
        }

    }
}

fun <S, T> TestWorkflow<S, TestWorkflow<S, T>>.multiply(): TestWorkflow<S, T> = object : TestWorkflow<S, T> {
    override val context: TestUserContext = this@multiply.context
    override fun TestUserContext.runWith(input: S): T = this@multiply.runWith(input).runWith(input)
}

infix fun <S, T> Any?.run(workflow: TestWorkflow<S,T>): (S) -> T = {s:S -> workflow.runWith(s)}
infix fun <S, T> ((S) -> T).withInput(input: S): T = this(input)


