package org.solyton.solawi.bid.module.shares.component.dropdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.left
import org.jetbrains.compose.web.css.marginLeft
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.right
import org.jetbrains.compose.web.css.top
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.shares.data.offers.ShareOffer
import org.solyton.solawi.bid.module.style.cursor.Cursor
import org.solyton.solawi.bid.module.style.cursor.cursor
import org.solyton.solawi.bid.module.style.dropdown.UserSelect
import org.solyton.solawi.bid.module.style.dropdown.userSelect

@Markup
@Suppress("FunctionName")
@Composable
fun ShareOffersDropdown(
    options: Map<String, ShareOffer>,
    selected: String?,
    closeOnSelect: Boolean = true,
    icon: String = "+", // Default icon,
    onSelected: (Map.Entry<String, ShareOffer>) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Div({
        style {
            position(Position.Relative)
            width(200.px)
        }
    }) {

        // Trigger
        Div({
            style {
                padding(8.px)
                border(1.px, LineStyle.Solid, Color.black)
                cursor(Cursor.Pointer)
                userSelect(UserSelect.None)
            }
            onClick { expanded = !expanded }
        }) {
            Text(selected ?: "Select offer")
            // Icon span
            Span({
                style {
                    marginLeft(8.px)
                    fontWeight("bold")
                    color(Color.black)
                }
            }) {
                Text(icon)
            }
        }

        // Dropdown content
        if (expanded) {
            Div({
                style {
                    position(Position.Absolute)
                    top(100.percent)
                    left(0.px)
                    right(0.px)
                    backgroundColor(Color.white)
                    border(1.px, LineStyle.Solid, Color.black)
                    zIndex(100)
                }
            }) {
                options.forEach { option ->
                    Div({
                        style {
                            padding(8.px)
                            cursor(Cursor.Pointer)
                        }
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

fun StyleScope.zIndex(i: Int)  {
    require(i >= 0) { "z-index must be non-negative" }
    property("z-index", i)
}
