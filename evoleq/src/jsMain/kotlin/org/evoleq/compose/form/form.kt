package org.evoleq.compose.form

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.dataId
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.dom.Div


@Markup
@Composable
@Suppress("FunctionName")
fun Form(
    styles: StyleScope.()->Unit,
    dataId: String? = null,
    content: @Composable ()->Unit
) = Div({
    if(dataId != null) dataId(dataId)
    style { styles() } }
) { content() }
