package org.solyton.solawi.bid.module.style.shadow

import org.jetbrains.compose.web.css.*

fun StyleScope.boxShadow(
    offsetX: CSSNumeric = 0.px,
    offsetY: CSSNumeric = 0.px,
    blurRadius: CSSNumeric = 0.px,
    spreadRadius: CSSNumeric? = null,
    color: CSSColorValue = rgba(0, 0, 0, 0.5)
) {
    val shadowValue = buildString {
        append("$offsetX $offsetY $blurRadius")
        if (spreadRadius != null) {
            append(" $spreadRadius")
        }
        append(" $color")
    }
    property("box-shadow", shadowValue)
}
