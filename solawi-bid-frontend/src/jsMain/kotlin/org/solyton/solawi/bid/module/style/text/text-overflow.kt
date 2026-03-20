package org.solyton.solawi.bid.module.style.text

import org.jetbrains.compose.web.css.StyleScope

enum class TextOverflow(val value: String) {
    Clip("clip"),
    Ellipsis("ellipsis")
}

fun StyleScope.textOverflow(value: TextOverflow) = property("text-overflow", value.value)
