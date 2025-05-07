package org.solyton.solawi.bid.application.ui.style.font

import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgb

val DesktopFonts: Fonts by lazy {
    object : Fonts{
        // Headings
        override val h1 = Font(size = 36.px, weight = "bold", family = "Arial, sans-serif")  // H1 = 36px
        override val h2 = Font(size = 30.px, weight = "bold")  // H2 = 30px
        override val h3 = Font(size = 24.px, weight = "bold")  // H3 = 24px
        override val h4 = Font(size = 20.px, weight = "bold")  // H4 = 20px
        override val h5 = Font(size = 18.px, weight = "bold")  // H5 = 18px
        override val h6 = Font(size = 16.px, weight = "bold")  // H6 = 16px

        // Text Elements
        override val body = Font(size = 14.px)  // Body Text = 14px
        override val paragraph = Font(size = 14.px, family = "Georgia, serif")  // Paragraph = 14px
        override val smallText = Font(size = 12.px, color = rgb(128, 128, 128))  // Small Text = 12px (gray)
        override val caption = Font(size = 10.px, color = rgb(169, 169, 169))  // Caption = 10px (dark gray)
        override val quote = Font(size = 16.px, style = "italic")  // Quote = 16px (italic)
        override val code = Font(size = 12.px, family = "monospace", color = rgb(139, 0, 0))  // Code = 12px (dark red)

        // Interactive Elements
        override val button = Font(size = 14.px)  // Button = 14px
        override val link = Font(size = 14.px, color = rgb(0, 0, 255))  // Link = 14px (blue)

        // Form Elements
        override val input = Font(size = 14.px, color = rgb(0, 0, 0))  // Input = 14px (black)
        override val select = Font(size = 14.px)  // Select = 14px
        override val textarea = Font(size = 14.px, family = "Verdana, sans-serif")  // Textarea = 14px
        override val label = Font(size = 14.px, weight = "bold")  // Label = 14px (bold)
        override val placeholder = Font(size = 14.px, color = rgb(128, 128, 128))  // Placeholder = 14px (gray)

    }
}
