package org.solyton.solawi.bid.module.list.component

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.style.cursor.Cursor
import org.solyton.solawi.bid.module.style.cursor.cursor
import org.solyton.solawi.bid.module.values.Price


@OptIn(ExperimentalComposeWebApi::class)
@Markup
@Composable
@Suppress("FunctionName")
fun EditableTextCell(
    text: String,
    tooltip: String? = null,
    style: StyleScope.() -> Unit = {},
    disabled: Boolean = false,
    onValueChange: (String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember(text) { mutableStateOf(text) }

    Div({
        if (tooltip != null) title(tooltip)

        style {
            textAlign("left")
            width(10.percent)
            when{
                disabled -> cursor(Cursor.NotAllowed)
                else -> cursor(Cursor.Pointer)
            }
            style()
        }

        if (!isEditing && !disabled) {
            onClick {
                isEditing = true
            }
        }
    }) {

        if (isEditing && !disabled) {

            Input(type = InputType.Text) {
                value(editText)

                style {
                    width(100.percent)
                }

                onInput {
                    editText = it.value
                }

                onBlur {
                    isEditing = false
                    onValueChange(editText)
                }

                onKeyDown { event ->
                    when (event.key) {
                        "Enter" -> {
                            isEditing = false
                            onValueChange(editText)
                        }
                        "Escape" -> {
                            isEditing = false
                            editText = text // revert
                        }
                    }
                }
            }
        } else {
            Text(text)
        }
    }
}


@OptIn(ExperimentalComposeWebApi::class)
@Markup
@Composable
@Suppress("FunctionName")
fun <T> EditableCell(
    initValue: T,
    format: (T?)->String,
    fromText: (String) -> T?,
    tooltip: String? = null,
    style: StyleScope.() -> Unit = {},
    disabled: Boolean = false,
    onValueChange: (T?) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember(initValue){ mutableStateOf<String>(format(initValue)) }

    Div({
        if (tooltip != null) title(tooltip)

        style {
            textAlign("left")
            width(10.percent)
            when{
                disabled -> cursor(Cursor.NotAllowed)
                else -> cursor(Cursor.Pointer)
            }
            style()
        }

        if (!isEditing && !disabled) {
            onClick {
                isEditing = true
            }
        }
    }) {

        if (isEditing && !disabled) {

            Input(type = InputType.Text) {
                value(editText)

                style {
                    width(100.percent)
                }

                onInput {
                    editText = it.value
                }

                onBlur {
                    isEditing = false
                    onValueChange(fromText(editText))
                }

                onKeyDown { event ->
                    when (event.key) {
                        "Enter" -> {
                            isEditing = false
                            onValueChange(fromText(editText))
                        }
                        "Escape" -> {
                            isEditing = false
                            editText = format(initValue) // revert
                        }
                    }
                }
            }
        } else {
            Text(format(initValue))
        }
    }
}

@Markup
@Composable
@Suppress("FunctionName")
fun EditablePriceCell(
    initValue: Price,
    style: StyleScope.()->Unit = {},
    disabled: Boolean = false,
    onValueChange: (Price?) -> Unit
) = EditableCell(
    initValue = initValue,
    format = {price -> (price?:Price(0.0)).format()},
    fromText = {Price.fromString(it)},
    style = {
        editableNumberStyle()
        style()
    },
    disabled = disabled,
    onValueChange = onValueChange
)

@Markup
@Composable
@Suppress("FunctionName")
fun EditableNullablePriceCell(
    initValue: Price?,
    style: StyleScope.()->Unit = {},
    disabled: Boolean = false,
    onValueChange: (Price?) -> Unit
) = EditableCell(
    initValue = initValue,
    format = {price -> price?.format()?: ""},
    fromText = {when{
        it.isBlank() -> null
        else -> Price.fromString(it)
    }},
    style = {
        editableNumberStyle()
        style()
    },
    disabled = disabled,
    onValueChange = onValueChange
)

@Markup
@Composable
@Suppress("FunctionName")
fun EditableIntCell(
    initValue: Int,
    style: StyleScope.()->Unit = {},
    disabled: Boolean = false,
    onValueChange: (Int?) -> Unit
) = EditableCell(
    initValue = initValue,
    format = {int -> int.toString()},
    fromText = {int -> int.toIntOrNull()},
    style = {
        editableNumberStyle()
        style()
    },
    disabled = disabled,
    onValueChange = onValueChange
)

val editableNumberStyle: StyleScope.() -> Unit = {
    textAlign("right")
    paddingRight(5.px)

}

@Markup
@Suppress("FunctionName")
@Composable
fun <T> EditableSelectCell(
    options: Map<String, T>,       // Label -> Value
    selected: T?,                  // Aktueller Wert
    placeholder: String = "Select...",
    closeOnSelect: Boolean = true, // optional
    disabled: Boolean = false,
    onSelected: (T) -> Unit,       // Callback wenn Auswahl geändert
) {
    var expanded by remember { mutableStateOf(false) }

    // Find label of current selection
    val selectedLabel = options.entries.firstOrNull { it.value == selected }?.key

    Div({
        style {
            position(Position.Relative)
            width(150.px)
            border(1.px, LineStyle.Solid, Color.black)
            padding(4.px)
            cursor("pointer")
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.SpaceBetween)
            alignItems(AlignItems.Center)
            property("user-select", "none")
        }
        if(!disabled) onClick {
            it.stopPropagation()
            expanded = !expanded
        }
    }) {
        // Label
        Text(selectedLabel ?: placeholder)

        // Icon
        Span({
            style {
                marginLeft(4.px)
                fontWeight("bold")
                color(Color.black)
            }
        }) {
            Text("+") // optional rotate on expand
        }

        // Dropdown
        if (expanded) {
            Div({
                style {
                    position(Position.Absolute)
                    top(100.percent)
                    left(0.px)
                    right(0.px)
                    backgroundColor(Color.white)
                    border(1.px, LineStyle.Solid, Color.black)
                    property("z-index", 100)
                }
                onClick { it.stopPropagation() }
            }) {
                options.filterKeys { it != selectedLabel  }.forEach { (label, value) ->
                    Div({
                        style {
                            padding(4.px)
                            cursor("pointer")
                        }
                        onClick { evt ->
                            evt.stopPropagation()
                            onSelected(value)
                            if (closeOnSelect) expanded = false
                        }
                    }) {
                        Text(label)
                    }
                }
            }
        }
    }
}

