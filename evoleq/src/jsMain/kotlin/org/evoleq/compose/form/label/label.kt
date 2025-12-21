package org.evoleq.compose.form.label

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.attributes.forId
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Text

@Markup
@Composable
@Suppress("FunctionName")
fun Label(text: String, id: String = "", labelStyle: StyleScope.()->Unit) {
    Label(attrs = {
        forId(id)
        style {
            labelStyle()
        }
    }) {
        Text(text)
    }
}
