package org.solyton.solawi.bid.application.ui.page.test

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Vertical
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*
import org.solyton.solawi.bid.application.ui.style.font.DesktopFonts
import org.solyton.solawi.bid.application.ui.style.font.setFont

@Markup
@Composable
@Suppress("FunctionName")
fun FontsPage() {
    Vertical {
        val fonts = DesktopFonts

        // Testseite mit den verschiedenen Schriftarten

        H1({
            style { setFont(fonts.h1) }
        }) {
            Text("This is H1 (36px)")
        }

        H2({
            style { setFont(fonts.h2) }
        }) {
            Text("This is H2 (30px)")
        }

        H3({
            style { setFont(fonts.h3) }
        }) {
            Text("This is H3 (24px)")
        }

        P({
            style { setFont(fonts.body) }
        }) {
            Text("This is Body Text (14px). Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
        }

        P({
            style { setFont(fonts.paragraph) }
        }) {
            Text("This is Paragraph Text (14px) in Georgia font.")
        }

        Button({
            style { setFont(fonts.button) }
            style {
                padding(10.px)
                marginTop(10.px)
            }
        }) {
            Text("This is a Button (14px)")
        }

        TextInput(attrs = {
            style {
                setFont(fonts.input)
                padding(5.px)
                marginTop(10.px)
            }
        })

        TextArea(attrs = {
            style {
                setFont(fonts.textarea)

                padding(5.px)
                marginTop(10.px)
            }
        })

        Label(attrs = {
            style { setFont(fonts.label) }
        }) {
            Text("This is a Label (14px) with bold font.")
        }

        Div({
            style {
                marginTop(20.px)
            }
        }) {
            Text("The font sizes are defined based on the DesktopFonts object.")
        }

    }
}
