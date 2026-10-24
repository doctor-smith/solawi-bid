package org.solyton.solawi.bid.module.style.wrap

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.dataId
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.w3c.dom.HTMLElement


@Markup
@Composable
@Suppress("FunctionName")
fun Wrap(
    style: StyleScope.()->Unit = {},
    dataId: String?=  null,
    content: @Composable ElementScope<HTMLElement>.()->Unit
) = Div({
    if(dataId != null) dataId(dataId)
    style {
        marginBottom(20.px)
        style()
    }
}){
    content()
}
