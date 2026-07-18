package org.solyton.solawi.bid.module.style.card

import org.jetbrains.compose.web.css.*

val cardStyle: StyleScope.() -> Unit = {
    border {
        style(LineStyle.Solid)
        width(1.px)
        borderRadius(5.px)
        color(Color.lightgray)
    }
    padding(5.px)
    paddingLeft(10.px)
}
