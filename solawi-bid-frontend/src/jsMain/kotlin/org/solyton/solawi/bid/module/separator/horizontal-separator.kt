package org.solyton.solawi.bid.module.separator

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Markup
@Composable
@Suppress("FunctionName")
fun LineSeparator(styles: LineSeparatorStyles = LineSeparatorStyles()) = Div{
    val (containerStyle, separatorStyle) = styles
    Div({ style { containerStyle() }}) {
        Div({ style { separatorStyle() } } )
    }
}

data class LineSeparatorStyles(
    val containerStyle: StyleScope.()->Unit = {
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.Center)
        alignItems(AlignItems.Center)
    },
    val separatorStyles: StyleScope.()->Unit = {
        width(100.percent)
        height(2.px)
        backgroundColor(Color.black)
        margin(20.px,0.px)
    }
)
