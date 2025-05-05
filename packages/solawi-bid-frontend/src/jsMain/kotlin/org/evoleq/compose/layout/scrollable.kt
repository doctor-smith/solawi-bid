package org.evoleq.compose.layout

import org.jetbrains.compose.web.css.*

val scrollableStyle: StyleScope.()->Unit = {
    //marginTop(10.px)
    overflowY("auto")
    // Scrollbar custom styling
    property("scrollbar-width", "thin") // Thin scrollbar (Firefox)
    property("scrollbar-color", "${Color.blue} ${Color.lightgray}") // Track & Thumb color

    // For WebKit (Chrome, Safari)
    property("::-webkit-scrollbar", "width: 6px")
    property("::-webkit-scrollbar-thumb", "background-color: ${Color.blue}; border-radius: 3px")
    property("::-webkit-scrollbar-track", "background-color: ${Color.lightgray}")
}