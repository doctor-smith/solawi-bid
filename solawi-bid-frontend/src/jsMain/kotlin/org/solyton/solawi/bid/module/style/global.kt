package org.solyton.solawi.bid.module.style

import org.jetbrains.compose.web.css.*

object GlobalStyles : StyleSheet() {
    init {
        "*" style {
            margin(0.px)
            padding(0.px)
            boxSizing("border-box")
        }

        "html, body" style {
            margin(0.px)
            padding(0.px)
            height(100.vh)
            width(100.vw)
            overflow("hidden")
            fontFamily("-apple-system", "BlinkMacSystemFont", "Segoe UI", "Roboto", "Helvetica Neue", "Arial", "sans-serif")
            backgroundColor(Color("#f8fafc"))
            color(Color("#1e293b"))
        }

        "container" style {
            width(80.percent)
            marginLeft(1.vw)
            marginRight(1.vw)

            media(mediaMaxWidth(768.px)) {
                width(98.vw)
            }
        }
    }

    val formStyle by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        width(50.percent)
        padding(24.px)
        borderRadius(12.px)
        backgroundColor(Color.white)
        property("border", "1px solid #e2e8f0")
        property("box-shadow", "0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -2px rgba(0, 0, 0, 0.05)")

        media(mediaMaxWidth(768.px)) {
            self {
                width(92.vw)
                padding(16.px)
            }
        }
    }
}
