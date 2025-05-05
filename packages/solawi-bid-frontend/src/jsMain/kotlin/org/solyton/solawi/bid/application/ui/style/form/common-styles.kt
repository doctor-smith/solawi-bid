package org.solyton.solawi.bid.application.ui.style.form

import org.evoleq.compose.Style
import org.jetbrains.compose.web.css.*
import org.solyton.solawi.bid.application.data.device.DeviceType
import org.solyton.solawi.bid.application.data.device.compareTo

@Style
fun formPageStyle(device: DeviceType): StyleScope.()->Unit = {
    display(DisplayStyle.Flex)
    flexDirection(FlexDirection.Column)
    alignItems(AlignItems.Center)
    justifyContent(JustifyContent.Center)
    backgroundColor(Color.white)
    height(100.vh) // Full viewport height
    when{
        device > DeviceType.Tablet -> formPageMobileStyle()
        else -> formPageDesktopStyle()
    }
}

@Style
fun fieldStyle(device: DeviceType): StyleScope.()->Unit = {
    width(100.percent)
    display(DisplayStyle.Flex)
    flexDirection(FlexDirection.Column)
    alignItems(AlignItems.Center)
    justifyContent(JustifyContent.Center)
}

@Style
fun formStyle(device: DeviceType): StyleScope.()->Unit = {
    display(DisplayStyle.Flex)
    flexDirection(FlexDirection.Column)
    backgroundColor(Color.whitesmoke)
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
