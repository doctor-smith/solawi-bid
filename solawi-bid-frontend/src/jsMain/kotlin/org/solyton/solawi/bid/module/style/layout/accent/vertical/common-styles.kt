package org.solyton.solawi.bid.module.style.layout.accent.vertical

import org.evoleq.compose.Style
import org.evoleq.compose.layout.VerticalAccentStyles
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.jetbrains.compose.web.css.*
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
}
@Style
@Suppress("UnusedParameter")
fun contentWrapperStyle(device: DeviceType): StyleScope.()-> Unit = {
    paddingTop(20.px)
    paddingBottom(20.px)
}
