package org.solyton.solawi.bid.module.style.dropdown

import org.jetbrains.compose.web.css.StyleScope
/**
 * Represents the CSS `user-select` property.
 *
 * Controls whether and how text inside an element can be selected by the user.
 *
 * Common use cases:
 * - Disable text selection for buttons or draggable UI
 * - Auto-select content for copy-to-clipboard fields
 * - Restore normal text behavior inside custom components
 *
 * Example:
 *
 * ```
 * style {
 *     userSelect(UserSelect.None)
 * }
 * ```
 */
enum class UserSelect(val value: String) {

    /**
     * Default browser behavior.
     *
     * The browser decides whether text is selectable.
     * This is the standard behavior for most elements.
     */
    Auto("auto"),

    /**
     * Allows normal text selection.
     *
     * Useful when a parent disables selection and
     * you want to re-enable it for a specific element.
     */
    Text("text"),

    /**
     * Prevents text selection entirely.
     *
     * Commonly used for:
     * - Buttons
     * - Navigation items
     * - Drag handles
     * - Interactive UI components
     */
    None("none"),

    /**
     * Restricts selection to within the element.
     *
     * The selection cannot extend outside the element's bounds.
     * Rarely needed in typical UI development.
     */
    Contain("contain"),

    /**
     * Automatically selects all content when the element is clicked.
     *
     * Ideal for:
     * - Copy-to-clipboard fields
     * - Code blocks
     * - URLs
     * - Token inputs
     */
    All("all"),

    /**
     * Inherits the value from the parent element.
     */
    Inherit("inherit"),

    /**
     * Resets to the property's initial value.
     */
    Initial("initial"),

    /**
     * Reverts to the user-agent or previously cascaded value.
     */
    Revert("revert"),

    /**
     * Reverts considering cascade layers (CSS Cascade Level 5).
     */
    RevertLayer("revert-layer"),

    /**
     * Acts as `inherit` if the property is inheritable,
     * otherwise behaves like `initial`.
     */
    Unset("unset")
}

/**
 * Sets the CSS `user-select` property for this element in a type-safe way.
 *
 * This controls whether and how the user can select text inside the element.
 * It applies standard cross-browser prefixes for compatibility:
 * - `-webkit-user-select` (Chrome, Safari, iOS)
 * - `-moz-user-select` (Firefox)
 *
 * Usage example:
 *
 * ```
 * style {
 *     userSelect(UserSelect.None)   // Prevent text selection
 * }
 *
 * style {
 *     userSelect(UserSelect.All)    // Automatically select all content when clicked
 * }
 * ```
 *
 * @param select The [UserSelect] value specifying the desired selection behavior.
 */
fun StyleScope.userSelect(select: UserSelect) {
    property("user-select", select.value)
    property("-webkit-user-select", select.value)
    property("-moz-user-select", select.value)
}
