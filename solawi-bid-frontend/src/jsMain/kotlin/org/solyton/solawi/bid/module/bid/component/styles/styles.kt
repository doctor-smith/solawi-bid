package org.solyton.solawi.bid.module.bid.component.styles

import org.evoleq.compose.Style
import org.evoleq.compose.modal.ModalStyles
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.math.Source
import org.jetbrains.compose.web.css.*

@Style
fun auctionModalStyles(device : Source<DeviceType>): ModalStyles = ModalStyles(
    containerStyle = auctionModalContainerStyle(device)
)


@Style
val auctionModalContainerStyle: (Source<DeviceType>) -> StyleScope.()->Unit = {
    _ -> {
        height(90.vh)
        justifyContent(JustifyContent.Center)
    }
}
