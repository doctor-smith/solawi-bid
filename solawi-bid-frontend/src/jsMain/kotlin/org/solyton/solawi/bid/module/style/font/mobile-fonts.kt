package org.solyton.solawi.bid.module.style.font

import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgb

val StdMobileFonts : Fonts by lazy {
    object : Fonts {
        private val sansFont = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif"
        private val monoFont = "'JetBrains Mono', 'SFMono-Regular', Menlo, Monaco, Consolas, 'Liberation Mono', monospace"

        // Headings
        override val h1 = Font(size = 26.px, weight = "700", family = sansFont, color = rgb(15, 23, 42))
        override val h2 = Font(size = 22.px, weight = "600", family = sansFont, color = rgb(30, 41, 59))
        override val h3 = Font(size = 19.px, weight = "600", family = sansFont, color = rgb(30, 41, 59))
        override val h4 = Font(size = 17.px, weight = "600", family = sansFont, color = rgb(51, 65, 85))
        override val h5 = Font(size = 15.px, weight = "600", family = sansFont, color = rgb(51, 65, 85))
        override val h6 = Font(size = 14.px, weight = "600", family = sansFont, color = rgb(71, 85, 105))

        // Text Elements
        override val body = Font(size = 15.px, family = sansFont, color = rgb(30, 41, 59))
        override val paragraph = Font(size = 15.px, family = sansFont, color = rgb(51, 65, 85))
        override val smallText = Font(size = 13.px, family = sansFont, color = rgb(100, 116, 139))
        override val caption = Font(size = 11.px, family = sansFont, color = rgb(148, 163, 184))
        override val quote = Font(size = 16.px, style = "italic", family = sansFont, color = rgb(71, 85, 105))
        override val code = Font(size = 13.px, family = monoFont, color = rgb(194, 65, 12))

        // Interactive Elements
        override val button = Font(size = 15.px, weight = "600", family = sansFont)
        override val link = Font(size = 15.px, weight = "500", color = rgb(2, 132, 199), family = sansFont)

        // Form Elements
        override val input = Font(size = 15.px, color = rgb(15, 23, 42), family = sansFont)
        override val select = Font(size = 15.px, color = rgb(15, 23, 42), family = sansFont)
        override val textarea = Font(size = 15.px, color = rgb(15, 23, 42), family = sansFont)
        override val label = Font(size = 14.px, weight = "500", color = rgb(51, 65, 85), family = sansFont)
        override val placeholder = Font(size = 14.px, color = rgb(148, 163, 184), family = sansFont)
    }
}

val LargeMobileFonts : Fonts by lazy {
    object : Fonts {
        private val sansFont = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif"
        private val monoFont = "'JetBrains Mono', 'SFMono-Regular', Menlo, Monaco, Consolas, 'Liberation Mono', monospace"

        override val h1 = Font(size = 32.px, weight = "700", family = sansFont, color = rgb(15, 23, 42))
        override val h2 = Font(size = 28.px, weight = "600", family = sansFont, color = rgb(30, 41, 59))
        override val h3 = Font(size = 24.px, weight = "600", family = sansFont, color = rgb(30, 41, 59))
        override val h4 = Font(size = 20.px, weight = "600", family = sansFont, color = rgb(51, 65, 85))
        override val h5 = Font(size = 18.px, weight = "600", family = sansFont, color = rgb(51, 65, 85))
        override val h6 = Font(size = 16.px, weight = "600", family = sansFont, color = rgb(71, 85, 105))

        override val body = Font(size = 16.px, family = sansFont, color = rgb(30, 41, 59))
        override val paragraph = Font(size = 16.px, family = sansFont, color = rgb(51, 65, 85))
        override val smallText = Font(size = 14.px, family = sansFont, color = rgb(100, 116, 139))
        override val caption = Font(size = 12.px, family = sansFont, color = rgb(148, 163, 184))
        override val quote = Font(size = 18.px, style = "italic", family = sansFont, color = rgb(71, 85, 105))
        override val code = Font(size = 15.px, family = monoFont, color = rgb(194, 65, 12))

        override val button = Font(size = 16.px, weight = "600", family = sansFont)
        override val link = Font(size = 16.px, weight = "500", color = rgb(2, 132, 199), family = sansFont)

        override val input = Font(size = 16.px, color = rgb(15, 23, 42), family = sansFont)
        override val select = Font(size = 16.px, color = rgb(15, 23, 42), family = sansFont)
        override val textarea = Font(size = 16.px, color = rgb(15, 23, 42), family = sansFont)
        override val label = Font(size = 15.px, weight = "500", color = rgb(51, 65, 85), family = sansFont)
        override val placeholder = Font(size = 15.px, color = rgb(148, 163, 184), family = sansFont)
    }
}
