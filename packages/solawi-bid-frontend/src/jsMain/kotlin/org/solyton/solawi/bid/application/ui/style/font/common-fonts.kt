package org.solyton.solawi.bid.application.ui.style.font

import org.evoleq.compose.Style
import org.jetbrains.compose.web.css.*

data class Font(
    val size: CSSSizeValue<*>,
    val weight: String? = null,
    val family: String? = null,
    val style: String? = null,
    val color: CSSColorValue? = null
)

@Style
fun StyleScope.setFont(font: Font) {
    fontSize(font.size)
    font.weight?.apply { fontWeight(font.weight) }
    font.family?.apply { fontFamily(font.family) }
    font.style?.apply { fontStyle(font.style) }
    font.color?.apply { color(font.color) }
}

interface Fonts {
    // Headings
    val h1: Font
    val h2: Font
    val h3: Font
    val h4: Font
    val h5: Font
    val h6: Font

    // Text Elements
    val body: Font
    val paragraph: Font
    val smallText: Font
    val caption: Font
    val quote: Font
    val code: Font

    // Interactive Elements
    val button: Font
    val link: Font

    // Form Elements
    val input: Font
    val select: Font
    val textarea: Font
    val label: Font
    val placeholder: Font
}


