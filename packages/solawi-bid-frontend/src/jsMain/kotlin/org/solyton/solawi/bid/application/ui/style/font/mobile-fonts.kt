package org.solyton.solawi.bid.application.ui.style.font

import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgb

val StdMobileFonts : Fonts by lazy {
    object : Fonts {
        // Headings
        override val h1 = Font(size = 28.px, weight = "bold", family = "Arial, sans-serif")
        override val h2 = Font(size = 24.px, weight = "bold")
        override val h3 = Font(size = 22.px, weight = "bold")
        override val h4 = Font(size = 20.px)
        override val h5 = Font(size = 18.px)
        override val h6 = Font(size = 16.px)

        // Text Elements
        override val body = Font(size = 16.px)
        override val paragraph = Font(size = 16.px, family = "Georgia, serif")
        override val smallText = Font(size = 12.px, color = rgb(128, 128, 128))
        override val caption = Font(size = 10.px, color = rgb(169, 169, 169))
        override val quote = Font(size = 18.px, style = "italic")
        override val code = Font(size = 14.px, family = "monospace", color = rgb(139, 0, 0))

        // Interactive Elements
        override val button = Font(size = 16.px, weight = "bold")
        override val link = Font(size = 14.px, color = rgb(0, 0, 255))

        // Form Elements
        override val input = Font(size = 14.px, color = rgb(0, 0, 0))
        override val select = Font(size = 14.px)
        override val textarea = Font(size = 14.px, family = "Verdana, sans-serif")
        override val label = Font(size = 14.px, weight = "bold")
        override val placeholder = Font(size = 14.px, color = rgb(128, 128, 128))
    }
}

val LargeMobileFonts : Fonts by lazy {
    object : Fonts {
        override val h1 = Font(size = 38.px, weight = "bold", family = "Arial, sans-serif")
        override val h2 = Font(size = 34.px, weight = "bold")
        override val h3 = Font(size = 30.px, weight = "bold")
        override val h4 = Font(size = 26.px)
        override val h5 = Font(size = 22.px)
        override val h6 = Font(size = 20.px)

        override val body = Font(size = 18.px)
        override val paragraph = Font(size = 18.px, family = "Georgia, serif")
        override val smallText = Font(size = 16.px, color = rgb(128, 128, 128))
        override val caption = Font(size = 14.px, color = rgb(169, 169, 169))
        override val quote = Font(size = 22.px, style = "italic")
        override val code = Font(size = 18.px, family = "monospace", color = rgb(139, 0, 0))

        override val button = Font(size = 20.px, weight = "bold")
        override val link = Font(size = 18.px, color = rgb(0, 0, 255))

        override val input = Font(size = 18.px, color = rgb(0, 0, 0))
        override val select = Font(size = 18.px)
        override val textarea = Font(size = 18.px, family = "Verdana, sans-serif")
        override val label = Font(size = 18.px, weight = "bold")
        override val placeholder = Font(size = 18.px, color = rgb(128, 128, 128))
    }
}