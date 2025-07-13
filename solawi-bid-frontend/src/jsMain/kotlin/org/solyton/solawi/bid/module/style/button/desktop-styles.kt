package org.solyton.solawi.bid.module.style.button

import org.jetbrains.compose.web.css.StyleScope
import org.solyton.solawi.bid.module.style.font.DesktopFonts
import org.solyton.solawi.bid.module.style.font.setFont

val submitButtonDesktopStyle: StyleScope.()->Unit = {
    setFont(DesktopFonts.button)
}
