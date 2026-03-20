package org.solyton.solawi.bid.module.style.overflow

import org.jetbrains.compose.web.css.StyleScope

enum class Overflow(val value: String) {
    Hidden("hidden"),
    Auto("auto"),
    Visible("visible")
}


// Generic overflow
fun StyleScope.overflow(value: Overflow) {
    property("overflow", value.value)
}

// Axis-specific helpers (recommended)
fun StyleScope.overflowX(value: Overflow) {
    property("overflow-x", value.value)
}

fun StyleScope.overflowY(value: Overflow) {
    property("overflow-y", value.value)
}
