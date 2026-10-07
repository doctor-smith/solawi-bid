package org.solyton.solawi.bid.module.style.card

import org.jetbrains.compose.web.css.*

val cardStyle: StyleScope.() -> Unit = {
    backgroundColor(Color.white)
    property("border", "1px solid #e2e8f0")
    borderRadius(10.px)
    padding(16.px)
    property("box-shadow", "0 1px 3px 0 rgba(0, 0, 0, 0.05), 0 1px 2px -1px rgba(0, 0, 0, 0.05)")
}
