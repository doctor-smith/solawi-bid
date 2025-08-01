package org.solyton.solawi.bid.module.style.button

import org.evoleq.compose.Style
import org.jetbrains.compose.web.css.*
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.compose.style.data.device.compareTo
import org.solyton.solawi.bid.module.style.font.LargeMobileFonts
import org.solyton.solawi.bid.module.style.font.setFont

@Style
fun buttonStyle(deviceType: DeviceType): StyleScope.()->Unit = {
    backgroundColor(Color.white)
    borderRadius(5.px)
    padding(4.px, 8.px)
    cursor("pointer")
    minWidth(100.px)
    if(deviceType <= DeviceType.Tablet) {
        width(100.percent)
        height(50.px)
        setFont(LargeMobileFonts.button)
    }
}

@Style
@Suppress("UNUSED_PARAMETER") // todo:style use parameter !!
fun symbolicButtonStyle(deviceType: DeviceType): StyleScope.()->Unit = {
    backgroundColor(Color.white)
    borderRadius(5.px)
    padding(4.px, 8.px)
    cursor("pointer")
}

@Style
fun submitButtonStyle(deviceType: DeviceType): StyleScope.()->Unit = {
    buttonStyle(deviceType)()
    // backgroundColor(Color.seagreen)
    when{
        deviceType > DeviceType.Tablet -> submitButtonDesktopStyle()
        else -> submitButtonMobileStyle()
    }
}

@Style
fun cancelButtonStyle(deviceType: DeviceType): StyleScope.()->Unit = {
    buttonStyle(deviceType)()
    // backgroundColor(Color.crimson)
}
