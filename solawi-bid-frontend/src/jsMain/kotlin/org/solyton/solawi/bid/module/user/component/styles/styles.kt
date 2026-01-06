package org.solyton.solawi.bid.module.user.component.styles


import org.evoleq.compose.Style
import org.evoleq.compose.modal.ModalStyles
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.math.Source
import org.jetbrains.compose.web.css.*

typealias userModalStyles = ModalStyles

@Style
fun modalStyles(device : Source<DeviceType>): ModalStyles = ModalStyles(
    containerStyle = modalContainerStyle(device)
)


@Style
val modalContainerStyle: (Source<DeviceType>) -> StyleScope.()->Unit = {
        _ -> {
    height(90.vh)
    justifyContent(JustifyContent.Center)
}
}
