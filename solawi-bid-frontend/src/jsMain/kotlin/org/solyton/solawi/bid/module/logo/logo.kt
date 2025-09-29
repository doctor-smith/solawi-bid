package org.solyton.solawi.bid.module.logo

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.jetbrains.compose.web.css.CSSNumericValue
import org.jetbrains.compose.web.css.CSSUnit
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img

@Markup
@Composable
@Suppress("FunctionName")
fun TopLogo(
    src: String,
    alt: String = "Logo",
    logoHeight: CSSNumericValue<CSSUnit.px> = 80.px
) {
    Div(attrs = {
        style {
            marginTop(10.px)
            marginBottom(5.px)
            width(100.percent)
            height(logoHeight)
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.Center)
        }
    }) {
        Img(
            src,
            alt,
            attrs = {
                style {
                    height(logoHeight)
                    width(auto)
                }
            }
        )
    }
}
