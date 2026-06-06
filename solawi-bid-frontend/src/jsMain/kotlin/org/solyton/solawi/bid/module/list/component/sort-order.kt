package org.solyton.solawi.bid.module.list.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.I
import org.jetbrains.compose.web.dom.Span
import org.solyton.solawi.bid.module.control.dropdown.Dropdown
import org.solyton.solawi.bid.module.control.dropdown.DropdownStyles
import org.solyton.solawi.bid.module.control.dropdown.Item
import org.solyton.solawi.bid.module.control.dropdown.SymbolDropdown

enum class SortOrder { ASC, DESC, NONE }
@Markup
@Composable
@Suppress("FunctionName")
fun SortBy(setSortOrder: suspend (SortOrder) -> Unit) {
    var order by remember { mutableStateOf(SortOrder.ASC) }
    LaunchedEffect(order) {
        setSortOrder(order)
    }
    When(order == SortOrder.NONE) {
        Div({ onClick{ order = SortOrder.ASC }} ) {
            I({
                classes("fa-solid","fa-arrow-up")
            })
        }
    }
    When(order == SortOrder.ASC) {
        Div({ onClick{ order = SortOrder.DESC } }) {
            I({
                classes("fa-solid", "fa-arrow-down")
            })
        }
    }
    When(order == SortOrder.DESC) {
        Div({ onClick{ order = SortOrder.NONE } }) {
            I({
                classes("fa-solid", "fa-arrow-down-up-across-line")
            })
        }
    }
}

@Markup
@Composable
@Suppress("FunctionName")
fun SortByDrop(
    styles: DropdownStyles = DropdownStyles().modifyContainerStyle {
        width(30.px)
    }.modifyTriggerStyle {
        border(0.px)
    }.modifyDropdownContentStyle {
        border(1.px,LineStyle.Solid, Color.gray)
    },
    showChosenOption: Boolean = false,
    initialValue: SortOrder = SortOrder.NONE,
    setSortOrder: suspend (SortOrder) -> Unit
) {
    var order by remember { mutableStateOf(initialValue) }
    LaunchedEffect(order) {
        setSortOrder(order)
    }
    val sortOrderOptions = SortOrder.entries.associateBy ({ it.name }){
        Item(it){ sortOrder ->
            When(sortOrder == SortOrder.ASC) {
                Span({ onClick{ order = SortOrder.ASC }} ) {
                    I({
                        classes("fa-solid","fa-arrow-up")
                    })
                }
            }
            When(sortOrder == SortOrder.DESC) {
                Span({ onClick{ order = SortOrder.DESC } }) {
                    I({
                        classes("fa-solid", "fa-arrow-down")
                    })
                }
            }
            When(sortOrder == SortOrder.NONE) {
                Span({ onClick{ order = SortOrder.NONE } }) {
                    I({
                        classes("fa-solid", "fa-arrow-down-up-across-line")
                    })
                }
            }
        }
    }

    SymbolDropdown<SortOrder>(
        options = sortOrderOptions,
        selected = order.name,
        closeOnSelect = true,
        showCosenOption = showChosenOption,
        styles = styles
    ){ _, value -> order = value }
}

