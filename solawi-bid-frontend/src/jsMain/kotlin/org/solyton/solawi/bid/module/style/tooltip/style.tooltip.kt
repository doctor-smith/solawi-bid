package org.solyton.solawi.bid.module.style.tooltip

import org.jetbrains.compose.web.css.*

val tooltipContentStyle: StyleScope.() -> Unit by lazy { {
    position(Position.Absolute)
    backgroundColor(Color("#1e293b"))
    color(Color("#f8fafc"))
    fontSize(12.px)
    fontWeight("500")
    lineHeight("1.35")
    padding(6.px, 10.px)
    borderRadius(6.px)
    whiteSpace("nowrap")
    property("pointer-events", "none")
    property("z-index", 900)
    property("box-shadow", "0 4px 6px -1px rgba(0, 0, 0, 0.2), 0 2px 4px -2px rgba(0, 0, 0, 0.15)")
} }
