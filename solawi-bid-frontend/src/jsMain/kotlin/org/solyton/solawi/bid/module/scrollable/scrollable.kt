package org.solyton.solawi.bid.module.scrollable

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Vertical
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.maxHeight
import org.jetbrains.compose.web.css.minHeight
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.solyton.solawi.bid.module.style.overflow.Overflow
import org.solyton.solawi.bid.module.style.overflow.overflowX
import org.solyton.solawi.bid.module.style.overflow.overflowY

data class ScrollableStyles(
    val containerStyle: StyleScope.()->Unit = {
        display( DisplayStyle.Flex )
        width(100.percent)
        maxHeight(200.px)
        height(200.px)
    },
    val contentStyle: StyleScope.()->Unit = {
        minHeight(180.px)
        width(100.percent)
        display( DisplayStyle.Flex )
        flexDirection(FlexDirection.Column)
        overflowY(Overflow.Auto)
        overflowX(Overflow.Hidden)
    }
) {
    fun modifyContainerStyle(style: StyleScope.()->Unit) = copy(
        containerStyle = {
            contentStyle()
            style()
        }
    )
    fun modifyContentStyle(style: StyleScope.()->Unit) = copy(
        contentStyle = {
            containerStyle()
            style()
        }
    )

    companion object {
        val Default = ScrollableStyles()
        fun modifyContainerStyle(style: StyleScope.()->Unit) = Default.modifyContainerStyle(style)
        fun modifyContentStyle(style: StyleScope.()->Unit) = Default.modifyContentStyle(style)
    }
}

@Markup
@Composable
@Suppress("FunctionName")
fun Scrollable(
    styles: ScrollableStyles = ScrollableStyles(),
    content: @Composable () -> Unit
) = Div(
    attrs = {style {
        with(styles){containerStyle()}
    }}
){
    Vertical(style = styles.contentStyle){
        content()
    }
}
