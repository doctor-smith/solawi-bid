package org.solyton.solawi.bid.module.banking.component.tab

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.solyton.solawi.bid.module.style.cursor.Cursor
import org.solyton.solawi.bid.module.style.cursor.cursor

@Markup
@Composable
@Suppress("FunctionName")
fun TabParagraphWrapper(
    isLast: Boolean = false,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) = Div({
    style {
        marginRight(5.px)
        cursor(Cursor.Pointer)
        if(isLast) return@style

        border{
            style = LineStyle.Solid
            width = 1.px
            color = Color.gray
        }
        borderWidth(0.px, 0.px, 1.px)

    }
    onClick { onClick() }
}) {
    content()
}
