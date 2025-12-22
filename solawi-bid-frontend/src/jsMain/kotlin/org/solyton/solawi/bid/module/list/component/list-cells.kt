package org.solyton.solawi.bid.module.list.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import kotlin.js.Date

@Markup
@Composable
@Suppress("FunctionName")
fun HeaderCell(
    text: Source<String>,
    style: StyleScope.()->Unit = {}
) = HeaderCell(text.emit(), style)

@Markup
@Composable
@Suppress("FunctionName")
fun HeaderCell(
    text: String,
    style: StyleScope.()->Unit = {}
){
    Div({style {
        fontWeight("bold")
        textAlign("left")
        width(10.percent)
        style()
    }}){Text(text)}
}

@Markup
@Composable
@Suppress("FunctionName")
fun TextCell(
    text: Source<String>,
    tooltip: String? = null,
    style: StyleScope.()->Unit = {
    }
) = TextCell(text.emit(), tooltip, style)

@Markup
@Composable
@Suppress("FunctionName")
fun TextCell(
    text: String,
    tooltip: String? = null,
    style: StyleScope.()->Unit = {
    }
){
    Div({
        if(tooltip != null) title(tooltip)
        style {
            textAlign("left")
            width(10.percent)
            style()
        }
    }){Text(text)}
}

@Markup
@Composable
@Suppress("FunctionName")
fun NumberCell(
    number: Source<Number>,
    style: StyleScope.()->Unit = { }
) = NumberCell(number.emit(), style)

@Markup
@Composable
@Suppress("FunctionName")
fun NumberCell(
    number: Number,
    style: StyleScope.()->Unit = { }
){
    Div({style {
        textAlign("left")
        width(10.percent)
        style()
    }}){Text("$number")}
}

@Markup
@Composable
@Suppress("FunctionName")
fun TimeCell(
    date: Source<Date>,
    showDate: Boolean = false,
    style: StyleScope.()->Unit = { }
) = TimeCell(date.emit(), showDate, style)

@Markup
@Composable
@Suppress("FunctionName")
fun TimeCell(
    date: Date,
    showDate: Boolean = false,
    style: StyleScope.()->Unit = { }
){
    Div({style {
        textAlign("left")
        width(10.percent)
        style()
    }}){
        when{
            showDate -> Text("${date.getFullYear()}/${(date.getMonth() +1).fix()}${date.getDay().fix()} - ${date.getHours().fix()}:${date.getMinutes().fix()}")
            else -> Text("${date.getHours().fix()}:${date.getMinutes().fix()}")
        }}
}

fun Int.fix(): String = when(this){
    in (0..9) -> "0$this"
    else -> "$this"
}

@Markup
@Composable
@Suppress("FunctionName")
fun ActionCell(
    style: StyleScope.()->Unit = {},
    actions: @Composable ()->Unit
){
    Div({style {
        textAlign("left")
        width(10.percent)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        gap(5.px)
        style()
    }}){
        actions()
    }
}

@Markup
@Composable
@Suppress("FunctionName")
fun ActionCellItem(
    text: String,
    style: StyleScope.()->Unit = {},
    onClick: () -> Unit = {},
    action: @Composable ()->Unit
){
    Div({
        onClick { onClick() }
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            gap(5.px)
        }
    }) {
        Div({
            style {
                textAlign("left")
                style()
            }
        }) {
            Text(text)
        }
        action()
    }
}

