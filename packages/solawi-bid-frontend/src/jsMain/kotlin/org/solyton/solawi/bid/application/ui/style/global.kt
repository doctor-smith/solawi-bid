package org.solyton.solawi.bid.application.ui.style

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
/*
    val container by style {
        width(80.percent)
        marginLeft(1.vw)
        marginRight(1.vw)

        media(mediaMaxWidth(768.px)) {
            self{width(98.vw)}
        }
    }
*/
    val formStyle by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        width(50.percent)
        padding(10.px)
        borderRadius(8.px)
        backgroundColor(Color.whitesmoke)


        media(mediaMaxWidth(768.px)) {
            self{width(80.vw)}
        }
    }
}
