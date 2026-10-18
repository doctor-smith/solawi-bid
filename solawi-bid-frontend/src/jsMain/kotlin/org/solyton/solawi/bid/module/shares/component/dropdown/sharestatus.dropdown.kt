package org.solyton.solawi.bid.module.shares.component.dropdown

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.dataId
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.style.cursor.Cursor
import org.solyton.solawi.bid.module.style.cursor.cursor
import org.solyton.solawi.bid.module.style.dropdown.UserSelect
import org.solyton.solawi.bid.module.style.dropdown.userSelect

@Markup
@Suppress("FunctionName")
@Composable
fun ShareStatusDropdown(
    options: Map<String, ShareStatus>,
    selected: String,
    closeOnSelect: Boolean = true,
    icon: String = "-", // Default icon,
    dataId: String? = null,
    onSelected: (Map.Entry<String, ShareStatus>) -> Unit,
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
            if(dataId != null) dataId(dataId)
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
                        if(dataId != null) dataId("$dataId.option.${option.value.value}")
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
