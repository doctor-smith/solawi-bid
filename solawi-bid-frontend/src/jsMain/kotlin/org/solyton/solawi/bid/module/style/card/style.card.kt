package org.solyton.solawi.bid.module.style.card

import org.jetbrains.compose.web.css.*
import org.solyton.solawi.bid.module.style.shadow.boxShadow

val cardStyle: StyleScope.() -> Unit = {
    border {
        style(LineStyle.Solid)
        width(1.px)
        borderRadius(5.px)
        color(Color.lightgray)
    }
    padding(5.px)
    paddingLeft(10.px)
    boxShadow(
        offsetX = 0.px,
        offsetY = 2.px,
        blurRadius = 6.px,
        color = rgba(0, 0, 0, 0.125)
    )
}
