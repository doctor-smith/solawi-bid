package org.solyton.solawi.bid.module.style.button

import org.jetbrains.compose.web.css.*
import org.solyton.solawi.bid.module.style.font.LargeMobileFonts
import org.solyton.solawi.bid.module.style.font.setFont

val submitButtonMobileStyle: StyleScope.()->Unit = {
    width(100.percent)
    height(50.px)
    setFont(LargeMobileFonts.button)
}
