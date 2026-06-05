package org.solyton.solawi.bid.module.style.text
import org.jetbrains.compose.web.css.StyleScope

sealed class WhiteSpace(val cssValue: String) {
    data object Normal : WhiteSpace("normal")
    data object NoWrap : WhiteSpace("nowrap")
    data object Pre : WhiteSpace("pre")
    data object PreWrap : WhiteSpace("pre-wrap")
    data object PreLine : WhiteSpace("pre-line")
    data object BreakSpaces : WhiteSpace("break-spaces")

    data class Custom(val value: String) : WhiteSpace(value)
}

fun StyleScope.whiteSpace(value: WhiteSpace) {
    property("white-space", value.cssValue)
}
