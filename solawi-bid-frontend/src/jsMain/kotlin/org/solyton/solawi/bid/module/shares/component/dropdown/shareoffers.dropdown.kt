package org.solyton.solawi.bid.module.shares.component.dropdown

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.dataId
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.shares.data.offers.ShareOffer
import org.solyton.solawi.bid.module.style.cursor.Cursor
import org.solyton.solawi.bid.module.style.cursor.cursor
import org.solyton.solawi.bid.module.style.dropdown.UserSelect
import org.solyton.solawi.bid.module.style.dropdown.userSelect
import org.solyton.solawi.bid.module.style.overflow.Overflow
import org.solyton.solawi.bid.module.style.overflow.overflowY

@Markup
@Suppress("FunctionName")
@Composable
fun ShareOffersDropdown(
    options: Map<String, ShareOffer>,
    selected: String?,
    closeOnSelect: Boolean = true,
    styles: ShareOffersDropdownStyles = ShareOffersDropdownStyles(),
    icon: String = "+", // Default icon,
    dataId: String? = "share-offers-dropdown",
    onSelected: (Map.Entry<String, ShareOffer>) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Div({
        if (dataId != null) dataId(dataId)
        style {
            with(styles) { containerStyle() }
        }
    }) {

        // Trigger
        Div({
            if (dataId != null) dataId("$dataId.trigger")
            style { with(styles) { triggerStyle()} }
            onClick { expanded = !expanded }
        }) {
            Text(selected ?: "Select offer")
            // Icon span
            Span({
                style { with(styles) { triggerIconStyle()} }
            }) {
                Text(icon)
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


data class ShareOffersDropdownStyles(
    val containerStyle: StyleScope.()->Unit = {
        position(Position.Relative)
        width(300.px)
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
        val Default = ShareOffersDropdownStyles()
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
