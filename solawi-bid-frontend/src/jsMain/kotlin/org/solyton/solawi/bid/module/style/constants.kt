package org.solyton.solawi.bid.module.style

import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.hsl
import org.jetbrains.compose.web.css.px

// logo
val topLogoHeight = 50.px

// Lists & Brand colors
val forestGreen: CSSColorValue = hsl(152, 60, 36)
val forestGreenLite: CSSColorValue = hsl(152, 35, 96)
@Suppress("UNUSED_VARIABLE")
val forestGreenUltraLite: CSSColorValue = hsl(152, 30, 98)

val verticalAccentBar: CSSColorValue = forestGreen

val listEven: CSSColorValue = Color.white
val listOdd: CSSColorValue = hsl(210, 20, 98)

// item gap
val listItemGap = 8.px
