package org.solyton.solawi.bid.module.style.modal

import org.evoleq.compose.Style
import org.evoleq.compose.modal.ModalStyles
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.math.Source
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.vh

// @Style
@Style
fun commonModalStyles(device : Source<DeviceType>): ModalStyles = ModalStyles(
    containerStyle = commonModalContainerStyle(device)
)


@Style
val commonModalContainerStyle: (Source<DeviceType>) -> StyleScope.()->Unit = {
        _ -> {
    height(90.vh)
    justifyContent(JustifyContent.Center)
}
}
