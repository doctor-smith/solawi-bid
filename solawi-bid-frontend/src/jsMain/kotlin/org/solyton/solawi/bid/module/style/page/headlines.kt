package org.solyton.solawi.bid.module.style.page

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text


@Markup
@Composable
@Suppress("FunctionName")
fun PageTitle(
    text: Source<String>,
    styles: StyleScope.()->Unit = {}
) = PageTitle(text.emit(), styles)

@Markup
@Composable
@Suppress("FunctionName")
fun PageTitle(
    text: String,
    styles: StyleScope.()->Unit = {}
) = Title(text, styles)


@Markup
@Composable
@Suppress("FunctionName")
fun Title(
    text: String,
    styles: StyleScope.()->Unit = {}
) = H1( attrs = { style{ styles() } }){
    Text(text)
}

@Markup
@Composable
@Suppress("FunctionName")
fun SubTitle(
    text: Source<String>,
    styles: StyleScope.()->Unit = {}
) = SubTitle(text.emit(), styles)

@Markup
@Composable
@Suppress("FunctionName")
fun SubTitle(
    text: String,
    styles: StyleScope.()->Unit = {}
) = H2(
    attrs = {
        style{
            color(Color.gray)
            fontSize(1.2.em)
            styles()
        }
    }
){
    Text(text)
}
