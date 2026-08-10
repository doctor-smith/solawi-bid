package org.solyton.solawi.bid.module.style.page

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.H3
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

@Markup
@Composable
@Suppress("FunctionName")
fun SubTitleOfH3(
    text: Source<String>,
    styles: StyleScope.()->Unit = {}
) = SubTitleOfH3(text.emit(), styles)


@Markup
@Composable
@Suppress("FunctionName")
fun SubTitleOfH3(
    text: String,
    styles: StyleScope.()->Unit = {}
) = H3(
    attrs = {
        style{
            color(Color.gray)
            fontSize(0.9.em)
            styles()
        }
    }
){
    Text(text)
}


val defaultHeadlineStyles: StyleScope.()->Unit by lazy { {
    marginTop(10.px)
    marginBottom(10.px)
} }

@Markup
@Composable
fun Headline(
    text: Source<String>,
    styles: StyleScope.()->Unit = defaultHeadlineStyles
) = Headline(text.emit(), styles)

@Markup
@Composable
fun Headline(
    text: String,
    styles: StyleScope.()->Unit = defaultHeadlineStyles
) {
    H2({style { styles() }}){ Text(text) }
}

val defaultParagraphStyles: StyleScope.()->Unit by lazy { {
    marginTop(10.px)
    marginBottom(5.px)
} }

@Markup
@Composable
fun Paragraph(
    text: Source<String>,
    styles: StyleScope.()->Unit = defaultParagraphStyles
) = Paragraph(text.emit(), styles)

@Markup
@Composable
fun Paragraph(

    text: String,
    styles: StyleScope.()->Unit = defaultParagraphStyles
) {
    H3({style { styles() }}){ Text(text) }
}

