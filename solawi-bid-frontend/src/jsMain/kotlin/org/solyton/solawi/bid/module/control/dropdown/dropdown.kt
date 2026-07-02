package org.solyton.solawi.bid.module.control.dropdown


import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.style.cursor.Cursor
import org.solyton.solawi.bid.module.style.cursor.cursor
import org.solyton.solawi.bid.module.style.dropdown.UserSelect
import org.solyton.solawi.bid.module.style.dropdown.userSelect
import org.solyton.solawi.bid.module.style.overflow.Overflow
import org.solyton.solawi.bid.module.style.overflow.overflowY

@Markup
@Suppress("FunctionName")
@Composable
fun <T> Dropdown(
    options: Map<String, T>,
    selected: String?,
    closeOnSelect: Boolean = true,
    styles: DropdownStyles = DropdownStyles(),
    iconContent: @Composable ((expanded: Boolean) -> Unit)? = null, // Optional custom icon
    onSelected: (Map.Entry<String, T>) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Div({
        style {
            with(styles) { containerStyle() }
        }
    }) {

            // Trigger
            Div({
                style { with(styles) { triggerStyle() } }
                onClick { expanded = !expanded }
            }) {
                Text(selected ?: "Select ...")
                // Icon span
                Span({
                    style { with(styles) { triggerIconStyle() } }
                }) {
                    // Use custom icon content if provided, otherwise default to "+"
                    iconContent?.invoke(expanded) ?: Text("+")
                }
            }

            // Dropdown content
            if (expanded) {
                Div({
                    style { with(styles) { dropdownContentStyle() } }
                }) {
                    options.forEach { option ->
                        Div({
                            style { with(styles) { dropdownItemStyle() } }
                            onClick {
                                onSelected(option)
                                if (closeOnSelect) {
                                    expanded = false
                                }
                            }
                        }) {
                            Text(option.key)
                        }
                    }
                }
            }
        }
    }


data class Item<T>(
    val value: T,
    val component: @Composable (T) -> Unit
)

@Markup
@Suppress("FunctionName")
@Composable
fun <T> SymbolDropdown(
    options: Map<String, Item<T>>,
    selected: String?,
    closeOnSelect: Boolean = true,
    showCosenOption: Boolean = false,
    styles: DropdownStyles = DropdownStyles().modifyContainerStyle {
        width(25.px)
    },
    //iconContent: @Composable ((expanded: Boolean) -> Unit)? = null, // Optional custom icon
    onSelected: (String,T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Div({
        style {
            with(styles) { containerStyle() }
        }
    }) {

        // Trigger
        Div({
            style { with(styles) { triggerStyle()} }
            onClick { expanded = !expanded }
        }) {
            val icon = options[selected]?.component!!
            icon(options[selected]!!.value)

            // Text(selected ?: "Select ...")
            /*
            // Icon span
            Span({
                style { with(styles) { triggerIconStyle()} }
            }) {


                // Use custom icon content if provided, otherwise default to "+"
                iconContent?.invoke(expanded) ?: Text("+")
            }

             */
        }

        // Dropdown content
        if (expanded) {
            Div({
                style { with(styles) { dropdownContentStyle() } }
            }) {
                options.filter { showCosenOption || it.key != selected }.forEach { option ->
                    Div({
                        style { with(styles) { dropdownItemStyle() } }
                        onClick {
                            onSelected(option.key, option.value.value)
                            if (closeOnSelect) {
                                expanded = false
                            }
                        }
                    }) {
                        /*When(showTextOnOptions) {

                        Text(option.key)
                        }*/
                        option.value.component(option.value.value)
                    }
                }
            }
        }
    }
}


data class DropdownStyles(
    val containerStyle: StyleScope.()->Unit = {
        position(Position.Relative)
        width(200.px)
    },
    val triggerStyle: StyleScope.()->Unit = {
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.SpaceBetween)
        paddingTop(2.px)
        paddingBottom(2.px)
        paddingLeft(5.px)
        border(2.px, LineStyle.Solid, Color.gray)
        borderRadius(5.px)
        cursor(Cursor.Pointer)
        userSelect(UserSelect.None)
    },

    val triggerIconStyle: StyleScope.()->Unit = {
        marginLeft(8.px)
        marginRight(2.px)
        fontWeight("bold")
        color(Color.black)
        right(0.px)
    },


    val dropdownContentStyle: StyleScope.()->Unit = {
        position(Position.Absolute)
        top(100.percent)
        left(0.px)
        right(0.px)
        backgroundColor(Color.white)
        border(2.px, LineStyle.Solid, Color.gray)
        borderRadius(5.px)
        maxHeight(300.px)
        overflowY(Overflow.Auto)
        zIndex(100)
    },
    val dropdownItemStyle: StyleScope.()->Unit = {
        padding(8.px)
        cursor(Cursor.Pointer)
    },
) {

    fun modifyContainerStyle(modifier: StyleScope.()->Unit) = with(this) {
        copy( containerStyle = {
            containerStyle()
            modifier()
        } )
    }


    fun modifyDropdownContentStyle(modifier: StyleScope.()->Unit) = with(this) {
        copy( dropdownContentStyle = {
            dropdownContentStyle()
            modifier()
        })
    }
    fun modifyDropdownItemStyle(modifier: StyleScope.()->Unit) = with(this) {
        copy( dropdownItemStyle = {
            dropdownItemStyle()
            modifier()
        })
    }

    fun modifyTriggerStyle(modifier: StyleScope.()->Unit) = with(this) {
        copy( triggerStyle = {
            triggerStyle()
            modifier()
        })
    }

    fun modifyTriggerIconStyle(modifier: StyleScope.()->Unit) = with(this) {
        copy( triggerIconStyle = {
            triggerIconStyle()
            modifier()
        })
    }
    companion object {
        val Default = DropdownStyles()
        fun modifyContainerStyle(modifier: StyleScope.()->Unit) = with(Default) {
            copy( containerStyle = {
                containerStyle()
                modifier()
            } )
        }


        fun modifyDropdownContentStyle(modifier: StyleScope.()->Unit) = with(Default) {
            copy( dropdownContentStyle = {
                dropdownContentStyle()
                modifier()
            })
        }
        fun modifyDropdownItemStyle(modifier: StyleScope.()->Unit) = with(Default) {
            copy( dropdownItemStyle = {
                dropdownItemStyle()
                modifier()
            })
        }

        fun modifyTriggerStyle(modifier: StyleScope.()->Unit) = with(Default) {
            copy( triggerStyle = {
                triggerStyle()
                modifier()
            })
        }

        fun modifyTriggerIconStyle(modifier: StyleScope.()->Unit) = with(Default) {
            copy( triggerIconStyle = {
                triggerIconStyle()
                modifier()
            })
        }
    }
}


fun StyleScope.zIndex(i: Int)  {
    require(i >= 0) { "z-index must be non-negative" }
    property("z-index", i)
}
