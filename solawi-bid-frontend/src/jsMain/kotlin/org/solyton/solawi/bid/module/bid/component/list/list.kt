package org.solyton.solawi.bid.module.bid.component.list

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import kotlin.js.Date


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
    text: String,
    style: StyleScope.()->Unit = {
    }
){
    Div({style {
        textAlign("left")
        width(10.percent)
        style()
    }}){Text(text)}
}

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
