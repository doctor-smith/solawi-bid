package org.solyton.solawi.bid.application.ui.style.button

import org.jetbrains.compose.web.css.*
import org.solyton.solawi.bid.application.ui.style.font.LargeMobileFonts
import org.solyton.solawi.bid.application.ui.style.font.setFont

val submitButtonMobileStyle: StyleScope.()->Unit = {
    width(100.percent)
    height(50.px)
    setFont(LargeMobileFonts.button)
}