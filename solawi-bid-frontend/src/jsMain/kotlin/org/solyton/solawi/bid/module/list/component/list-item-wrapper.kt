package org.solyton.solawi.bid.module.list.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.dataId
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.solyton.solawi.bid.module.list.style.defaultListStyles
import org.w3c.dom.HTMLElement


@Markup
@Composable
@Suppress("FunctionName")
fun ListItemWrapper(
    styles: StyleScope.()->Unit = defaultListStyles.listItemWrapper,
    dataId: String? = null,
    content: @Composable ElementScope<HTMLElement>.()->Unit
) = Div({
    if(dataId != null) dataId(dataId)
    style{styles()
}}) {
    content()
}
