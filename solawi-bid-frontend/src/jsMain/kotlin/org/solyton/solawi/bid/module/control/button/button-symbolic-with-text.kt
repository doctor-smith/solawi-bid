package org.solyton.solawi.bid.module.control.button

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.math.Source
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.Color
import org.solyton.solawi.bid.module.style.data.Side

@Markup
@Composable
@Suppress("FunctionName")
fun GearButtonWithText(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,
    text: Source<String> = {""},
    tooltip: Source<String?> = {null},
    iconSide: Side = Side.Left,
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit
) = IconButtonWithText(
    color,
    bgColor,
    arrayOf("fa-solid", "fa-gear"),
    text,
    tooltip,
    deviceType,
    iconSide,
    isDisabled,
    dataId,
    onClick
)

@Markup
@Composable
@Suppress("FunctionName")
fun PlayButtonWithText(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,
    text: Source<String> = {""},
    tooltip: Source<String?> = {null},
    iconSide: Side = Side.Left,
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit
) = IconButtonWithText(
    color,
    bgColor,
    arrayOf("fa-solid", "fa-play"),
    text,
    tooltip,
    deviceType,
    iconSide,
    isDisabled,
    dataId,
    onClick
)

@Markup
@Composable
@Suppress("FunctionName")
fun UploadButtonWithText(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,
    text: Source<String> = {""},
    tooltip: Source<String?> = {null},
    iconSide: Side = Side.Left,
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit
) = IconButtonWithText(
    color,
    bgColor,
    arrayOf("fa-solid", "fa-upload"),
    text,
    tooltip,
    deviceType,
    iconSide,
    isDisabled,
    dataId,
    onClick
)
