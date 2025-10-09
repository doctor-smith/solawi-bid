package org.solyton.solawi.bid.module.bid.component.button

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.routing.navigate
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.math.Source
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.Color
import org.solyton.solawi.bid.module.control.button.BuildingColumnsButton

// BuildingColumnsButton

@Markup
@Composable
@Suppress("FunctionName")
fun AuctionsButton(
    url: String,
    color: CSSColorValue,
    bgColor: CSSColorValue,
    texts: Source<String?>,
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
) = BuildingColumnsButton(
    color,
    bgColor,
    texts,
    deviceType,
    isDisabled,
    dataId
) {
    navigate(url)
}
