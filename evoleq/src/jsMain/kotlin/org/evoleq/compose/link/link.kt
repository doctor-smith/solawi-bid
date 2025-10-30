package org.evoleq.compose.link

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.routing.navigate
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.textDecoration
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Markup
@Composable
@Suppress("FunctionName")
fun Link(text: String, target: String, styles: StyleScope.()->Unit = defaultLinkStyles) = Span({
    style{styles()}
    onClick {
        it.stopPropagation()
        navigate(target)
    }
}) {
    Text(text)
}

val defaultLinkStyles: StyleScope.()->Unit = {
    textDecoration("underline")
    cursor("pointer")
}
