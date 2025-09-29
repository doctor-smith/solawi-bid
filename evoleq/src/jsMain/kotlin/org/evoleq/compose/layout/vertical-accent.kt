package org.evoleq.compose.layout

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

data class VerticalAccentStyles(
    val containerStyle: StyleScope.()->Unit,
    val accentBarStyle: StyleScope.()->Unit,
    val contentWrapperStyle: StyleScope.()->Unit,
)

@Markup
@Composable
@Suppress("FunctionName")
fun VerticalAccent(
    styles: VerticalAccentStyles,
    content: @Composable ()->Unit
) {
    val (containerStyle, accentBarStyle, contentWrapperStyle) = styles
    Div(attrs = {
        style{
            containerStyle()
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Stretch)
        }
    }){
        // Accent bar
        Div(attrs = {
            style {
                accentBarStyle()
            }
        }){}
        // Content
        Div(attrs = {
            style {
                contentWrapperStyle()
                flex(1)
            }
        }){
            content()
        }
    }
}
