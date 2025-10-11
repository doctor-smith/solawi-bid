package org.solyton.solawi.bid.module.list.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.solyton.solawi.bid.module.list.style.defaultListStyles
import org.w3c.dom.HTMLElement


@Markup
@Composable
@Suppress("FunctionName")
fun ActionsWrapper(
    styles: StyleScope.()->Unit = defaultListStyles.actionsWrapper,
    content: @Composable ElementScope<HTMLElement>.()->Unit
) = Div({style{styles()}}) {
    content()
}
