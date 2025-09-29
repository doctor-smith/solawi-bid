package org.solyton.solawi.bid.module.style.layout.accent.vertical

import org.evoleq.compose.Style
import org.evoleq.compose.layout.VerticalAccentStyles
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.compose.style.data.device.compareTo
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.marginRight
import org.jetbrains.compose.web.css.minHeight
import org.jetbrains.compose.web.css.paddingBottom
import org.jetbrains.compose.web.css.paddingTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.solyton.solawi.bid.module.style.verticalAccentBar

@Style
fun verticalAccentStyles(device: Source<DeviceType>): VerticalAccentStyles =
    verticalAccentStyles(device.emit())

@Style
fun verticalAccentStyles(device: DeviceType): VerticalAccentStyles = VerticalAccentStyles(
    containerStyle(device),
    accentBarStyle(device),
    contentWrapperStyle(device),
)

@Style
@Suppress("UnusedParameter")
fun containerStyle(device: DeviceType): StyleScope.()->Unit =  {}


@Style
@Suppress("UnusedParameter")
fun accentBarStyle(device: DeviceType): StyleScope.()-> Unit = {
    minHeight(1.px)
    width(16.px)
    borderRadius(4.px)
    backgroundColor(verticalAccentBar)
    marginRight(12.px)
}
@Style
@Suppress("UnusedParameter")
fun contentWrapperStyle(device: DeviceType): StyleScope.()-> Unit = {
    paddingTop(20.px)
    paddingBottom(20.px)
}
