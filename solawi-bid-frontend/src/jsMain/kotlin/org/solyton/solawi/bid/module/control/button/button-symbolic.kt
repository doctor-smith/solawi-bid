package org.solyton.solawi.bid.module.control.button

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.math.Source
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.Color


@Markup
@Composable
@Suppress("FunctionName")
fun EditButton(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,
    texts: Source<String?> = {null},
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit
) = IconButton(
    color,
    bgColor,
    arrayOf("fa-solid", "fa-pen"),
    texts,
    deviceType,
    isDisabled,
    dataId,
    onClick
)

@Markup
@Composable
@Suppress("FunctionName")
fun PlusButton(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,
    texts: Source<String?> = {null},
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit
) = IconButton(
    color,
    bgColor,
    arrayOf("fa-solid", "fa-plus"),
    texts,
    deviceType,
    isDisabled,
    dataId,
    onClick
)

@Markup
@Composable
@Suppress("FunctionName")
fun DetailsButton(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,

    texts: Source<String?> = {null},
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit
) = IconButton(
    color,
    bgColor,
    arrayOf("fa-solid", "fa-file-lines"),
    texts,
    deviceType,
    isDisabled,
    dataId,
    onClick
)

@Markup
@Composable
@Suppress("FunctionName")
fun TrashCanButton(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,
    texts: Source<String?> = {null},
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit
) = IconButton(
    color,
    bgColor,
    arrayOf("fa-solid", "fa-trash-can"),
    texts,
    deviceType,
    isDisabled,
    dataId,
    onClick
)

@Markup
@Composable
@Suppress("FunctionName")
fun GearButton(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,
    texts: Source<String?> = {null},
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit
) = IconButton(
    color,
    bgColor,
    arrayOf("fa-solid", "fa-gear"),
    texts,
    deviceType,
    isDisabled,
    dataId,
    onClick
)

@Markup
@Composable
@Suppress("FunctionName")
fun FileImportButton(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,
    texts: Source<String?> = {null},
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit
) = IconButton(
    color,
    bgColor,
    arrayOf("fa-solid", "fa-file-import"), //"fa-file-arrow-up"
    texts,
    deviceType,
    isDisabled,
    dataId,
    onClick
)

@Markup
@Composable
@Suppress("FunctionName")
fun FileExportButton(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,
    texts: Source<String?> = {null},
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit
) = IconButton(
    color,
    bgColor,
    arrayOf("fa-solid", "fa-file-export"),
    texts,
    deviceType,
    isDisabled,
    dataId,
    onClick
)

@Markup
@Composable
@Suppress("FunctionName")
fun UploadButton(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,
    texts: Source<String?> = {null},
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit
) = IconButton(
    color,
    bgColor,
    arrayOf("fa-solid", "fa-upload"),
    texts,
    deviceType,
    isDisabled,
    dataId,
    onClick
)

@Markup
@Composable
@Suppress("FunctionName")
fun DownloadButton(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,
    texts: Source<String?> = {null},
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit
) = IconButton(
    color,
    bgColor,
    arrayOf("fa-solid", "fa-download"),
    texts,
    deviceType,
    isDisabled,
    dataId,
    onClick
)

@Markup
@Composable
@Suppress("FunctionName")
fun PlayButton(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,
    texts: Source<String?> = {null},
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit
) = IconButton(
    color,
    bgColor,
    arrayOf("fa-solid", "fa-play"),
    texts,
    deviceType,
    isDisabled,
    dataId,
    onClick
)

@Markup
@Composable
@Suppress("FunctionName")
fun HomeButton(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,
    texts: Source<String?> = {null},
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit
) = IconButton(
    color,
    bgColor,
    arrayOf("fa-solid", "fa-house"),
    texts,
    deviceType,
    isDisabled,
    dataId,
    onClick
)


@Markup
@Composable
@Suppress("FunctionName")
fun AppsButton(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,
    texts: Source<String?> = {null},
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit
) = IconButton(
    color,
    bgColor,
    arrayOf("fa-solid", "fa-grip"),
    texts,
    deviceType,
    isDisabled,
    dataId,
    onClick
)

@Markup
@Composable
@Suppress("FunctionName")
fun EvaluationButton(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,
    texts: Source<String?> = {null},
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit
) = IconButton(
    color,
    bgColor,
    arrayOf("fa-solid", "fa-chart-line"),
    texts,
    deviceType,
    isDisabled,
    dataId,
    onClick
)

@Markup
@Composable
@Suppress("FunctionName")
fun XMarkButton(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,
    texts: Source<String?> = {null},
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit
) = IconButton(
    color,
    bgColor,
    arrayOf("fa-solid", "fa-xmark"),
    texts,
    deviceType,
    isDisabled,
    dataId,
    onClick
)

@Markup
@Composable
@Suppress("FunctionName")
fun HelpButton(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,
    texts: Source<String?> = {null},
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit
) = IconButton(
    color,
    bgColor,
    arrayOf("fa-solid", "fa-question"),
    texts,
    deviceType,
    isDisabled,
    dataId,
    onClick
)

@Markup
@Composable
@Suppress("FunctionName")
fun BuildingColumnsButton(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,
    texts: Source<String?> = {null},
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit
) = IconButton(
    color,
    bgColor,
    arrayOf("fa-solid", "fa-building-columns"),
    texts,
    deviceType,
    isDisabled,
    dataId,
    onClick
)
