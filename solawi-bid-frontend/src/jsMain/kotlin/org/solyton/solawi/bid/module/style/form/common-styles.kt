package org.solyton.solawi.bid.module.style.form

import org.evoleq.compose.Style
import org.evoleq.compose.style.data.device.DeviceType
import org.jetbrains.compose.web.css.*

@Style
fun formPageStyle(device: DeviceType): StyleScope.()->Unit = {
    display(DisplayStyle.Flex)
    flexDirection(FlexDirection.Column)
    alignItems(AlignItems.Center)
    justifyContent(JustifyContent.Center)
    backgroundColor(Color("#f8fafc"))
    height(100.vh) // Full viewport height
    when{
        device > DeviceType.Tablet -> formPageDesktopStyle()
        else -> formPageMobileStyle()
    }
}

@Style
@Suppress("UNUSED_PARAMETER")
fun fieldStyle(device: DeviceType): StyleScope.()->Unit = {
    width(100.percent)
    display(DisplayStyle.Flex)
    flexDirection(FlexDirection.Column)
    alignItems(AlignItems.FlexStart)
    justifyContent(JustifyContent.Center)
}

@Style
fun formStyle(device: DeviceType): StyleScope.()->Unit = {
    display(DisplayStyle.Flex)
    flexDirection(FlexDirection.Column)
    backgroundColor(Color.white)
    property("border", "1px solid #e2e8f0")
    property("box-shadow", "0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -2px rgba(0, 0, 0, 0.05)")
    when{
        device > DeviceType.Tablet -> formDesktopStyle()
        else -> formMobileStyle()
    }
}

@Style
fun formLabelStyle(device: DeviceType): StyleScope.()->Unit = when {
    device > DeviceType.Tablet -> formLabelDesktopStyle
    else -> formLabelMobileStyle
}

@Style
fun textInputStyle(device: DeviceType): StyleScope.()->Unit = when {
    device > DeviceType.Tablet -> textInputDesktopStyle
    else -> textInputMobileStyle
}

@Style
fun numberInputStyle(device: DeviceType): StyleScope.()->Unit = when {
    device > DeviceType.Tablet -> numberInputDesktopStyle
    else -> numberInputMobileStyle
}

@Style
fun formControlBarStyle(device: DeviceType): StyleScope.()->Unit = when {
    device > DeviceType.Tablet -> formControlBarDesktopStyle
    else -> formControlBarMobileStyle
}

@Style
fun dateInputStyle(device: DeviceType): StyleScope.()->Unit = when {
    device > DeviceType.Tablet -> dateInputDesktopStyle
    else -> dateInputMobileStyle
}

@Style
fun formButtonStyle(device: DeviceType): StyleScope.()->Unit = when {
    device > DeviceType.Tablet -> formButtonDesktopStyle
    else -> formButtonMobileStyle
}
