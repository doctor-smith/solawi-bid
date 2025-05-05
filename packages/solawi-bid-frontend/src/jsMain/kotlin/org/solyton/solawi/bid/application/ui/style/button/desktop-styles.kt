package org.solyton.solawi.bid.application.ui.style.button

import org.jetbrains.compose.web.css.StyleScope
import org.solyton.solawi.bid.application.ui.style.font.DesktopFonts
import org.solyton.solawi.bid.application.ui.style.font.setFont

val submitButtonDesktopStyle: StyleScope.()->Unit = {
    setFont(DesktopFonts.button)
}