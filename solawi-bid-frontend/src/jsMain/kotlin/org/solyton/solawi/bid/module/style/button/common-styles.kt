package org.solyton.solawi.bid.module.style.button

import org.evoleq.compose.Style
import org.evoleq.compose.style.data.device.DeviceType
import org.jetbrains.compose.web.css.*
import org.solyton.solawi.bid.module.style.font.LargeMobileFonts
import org.solyton.solawi.bid.module.style.font.setFont
import org.solyton.solawi.bid.module.style.forestGreen

@Style
fun buttonStyle(deviceType: DeviceType): StyleScope.()->Unit = {
    backgroundColor(Color.white)
    color(Color("#1e293b"))
    borderRadius(6.px)
    padding(8.px, 16.px)
    property("border", "1px solid #cbd5e1")
    property("box-shadow", "0 1px 2px 0 rgba(0, 0, 0, 0.05)")
    property("transition", "all 0.15s ease-in-out")
    property("font-weight", "500")
    fontSize(14.px)
    cursor("pointer")
    minWidth(90.px)
    if(deviceType <= DeviceType.Tablet) {
        width(100.percent)
        height(44.px)
        setFont(LargeMobileFonts.button)
    }
}

@Style
@Suppress("UNUSED_PARAMETER")
fun symbolicButtonStyle(deviceType: DeviceType): StyleScope.()->Unit = {
    backgroundColor(Color.white)
    borderRadius(6.px)
    padding(6.px, 10.px)
    property("border", "1px solid #cbd5e1")
    property("box-shadow", "0 1px 2px 0 rgba(0, 0, 0, 0.05)")
    property("transition", "all 0.15s ease-in-out")
    cursor("pointer")
}

@Style
fun submitButtonStyle(deviceType: DeviceType): StyleScope.()->Unit = {
    buttonStyle(deviceType)()
    backgroundColor(forestGreen)
    color(Color.white)
    property("border", "1px solid transparent")
    property("box-shadow", "0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px -1px rgba(0, 0, 0, 0.1)")
    property("font-weight", "600")
    when{
        deviceType > DeviceType.Tablet -> submitButtonDesktopStyle()
        else -> submitButtonMobileStyle()
    }
}

@Style
fun cancelButtonStyle(deviceType: DeviceType): StyleScope.()->Unit = {
    buttonStyle(deviceType)()
    backgroundColor(Color.white)
    color(Color("#b91c1c"))
    property("border", "1px solid #fecaca")
    property("font-weight", "500")
}
