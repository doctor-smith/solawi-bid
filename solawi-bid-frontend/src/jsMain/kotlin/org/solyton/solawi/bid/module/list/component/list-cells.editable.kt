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
import org.solyton.solawi.bid.module.shares.component.dropdown.zIndex
import org.solyton.solawi.bid.module.style.cursor.Cursor
import org.solyton.solawi.bid.module.style.cursor.cursor
import org.solyton.solawi.bid.module.style.dropdown.UserSelect
import org.solyton.solawi.bid.module.style.dropdown.userSelect
import org.solyton.solawi.bid.module.style.overflow.Overflow
import org.solyton.solawi.bid.module.style.overflow.overflow
import org.solyton.solawi.bid.module.style.overflow.overflowY
import org.solyton.solawi.bid.module.style.text.TextOverflow
import org.solyton.solawi.bid.module.style.text.textOverflow
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
            Span({
                style {
                    minWidth(0.px)
                    whiteSpace("normal")
                }
            }) { Text(text) }
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
            paddingLeft(5.px)
            paddingRight(5.px)
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
    paddingLeft(5.px)

}

data class EditableSelectCellStyles(
    val containerStyle: StyleScope.()->Unit = {
        position(Position.Relative)
        width(100.percent)
        border(1.px, LineStyle.Solid, Color.gray)
        borderRadius(5.px)
        padding(1.px)
        cursor(Cursor.Pointer)
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.SpaceBetween)
        alignItems(AlignItems.Center)
        userSelect(UserSelect.None)
        maxHeight(300.px)
    },
    val labelStyle: StyleScope.()->Unit = {
        paddingLeft(5.px)
        overflow(Overflow.Hidden)
        textOverflow(TextOverflow.Ellipsis)
    },
    val selectStyle: StyleScope.()->Unit = {
        position(Position.Absolute)
        top(100.percent)
        left(0.px)
        right(0.px)
        backgroundColor(Color.white)
        border(1.px, LineStyle.Solid, Color.gray)
        borderRadius(5.px)
        maxHeight(300.px)
        overflowY(Overflow.Auto)
        zIndex( 100)
    },
    val iconStyle: StyleScope.()->Unit = {
        marginLeft(2.px)
        fontWeight("bold")
        color(Color.black)
    },
    val itemStyle: StyleScope.()->Unit = {
        width(100.percent)
        paddingTop(2.px)
        paddingBottom(2.px)
        paddingLeft(5.px)
        overflow(Overflow.Hidden)
        textOverflow(TextOverflow.Ellipsis)
        cursor(Cursor.Pointer)
    }
) {
    fun modifySelectStyle(style: StyleScope.() -> Unit): EditableSelectCellStyles = with(this){
        copy(selectStyle = {
            selectStyle()
            style()
        })
    }
    fun modifyLabelStyle(style: StyleScope.() -> Unit): EditableSelectCellStyles = with(this){
        copy(labelStyle = {
            labelStyle()
            style()
        })
    }
    fun modifyContainerStyle(style: StyleScope.() -> Unit): EditableSelectCellStyles = with(this){
        copy(containerStyle = {
            containerStyle()
            style()
        })
    }
    fun modifyIconStyle(style: StyleScope.() -> Unit): EditableSelectCellStyles = with(this){
        copy(iconStyle = {
            iconStyle()
            style()
        })
    }
    fun modifyItemStyle(style: StyleScope.() -> Unit): EditableSelectCellStyles = with(this){
        copy(itemStyle = {
            itemStyle()
            style()
        })
    }

    companion object {
        val Default = EditableSelectCellStyles()
        fun modifySelectStyle(style: StyleScope.() -> Unit): EditableSelectCellStyles = with(Default){
            copy(selectStyle = {
                selectStyle()
                style()
            })
        }
        fun modifyLabelStyle(style: StyleScope.() -> Unit): EditableSelectCellStyles = with(Default){
            copy(labelStyle = {
                labelStyle()
                style()
            })
        }
        fun modifyContainerStyle(style: StyleScope.() -> Unit): EditableSelectCellStyles = with(Default){
            copy(containerStyle = {
                containerStyle()
                style()
            })
        }
        fun modifyIconStyle(style: StyleScope.() -> Unit): EditableSelectCellStyles = with(Default){
            copy(iconStyle = {
                iconStyle()
                style()
            })
        }
        fun modifyItemStyle(style: StyleScope.() -> Unit): EditableSelectCellStyles = with(Default){
            copy(itemStyle = {
                itemStyle()
                style()
            })
        }
    }
}

@Markup
@Suppress("FunctionName")
@Composable
fun <T> EditableSelectCell(
    options: Map<String, T>,       // Label -> Value
    selected: T?,                  // current value
    placeholder: String = "Select...",
    closeOnSelect: Boolean = true, // optional
    disabled: Boolean = false,
    styles: EditableSelectCellStyles = EditableSelectCellStyles(),
    iconContent: @Composable ((expanded: Boolean) -> Unit)? = null, // Optional custom icon
    onSelected: (T) -> Unit,       // Callback when selection changed
) {
    var expanded by remember { mutableStateOf(false) }

    // Find label of current selection
    val initialLabel = options.entries.firstOrNull { it.value == selected }?.key
    var selectedLabel by remember { mutableStateOf(initialLabel) }

    Div({
        style {
            with(styles) { containerStyle() }
        }
        if (!disabled) onClick {
            it.stopPropagation()
            expanded = !expanded
        }
    }) {
        // Label
        Span(attrs = {
            title(selectedLabel ?: placeholder)
            style {
                with(styles) { labelStyle() }
            }
        }) { Text(selectedLabel ?: placeholder) }

        // Icon
        Span({
            style { with(styles) {iconStyle() } }
        }) {
            // Use custom icon content if provided, otherwise default to "+"
            iconContent?.invoke(expanded) ?: Text("+")
        }

        // Dropdown
        if (!disabled && expanded) {
            Div({
                style { with(styles) { selectStyle() } }
                onClick { it.stopPropagation() }
            }) {
                options.filterKeys { it != selectedLabel  }.forEach { (label, value) ->
                    Div({
                        title(label)
                        style { with(styles) { itemStyle() } }
                        onClick { evt ->
                            evt.stopPropagation()
                            onSelected(value)
                            selectedLabel = label
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

