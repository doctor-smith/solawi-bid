package org.solyton.solawi.bid.module.control.button

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.dataId
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.I
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.style.button.buttonStyle
import org.solyton.solawi.bid.module.style.button.cancelButtonStyle
import org.solyton.solawi.bid.module.style.button.submitButtonStyle
import org.solyton.solawi.bid.module.style.button.symbolicButtonStyle

@Markup
@Composable
@Suppress("FunctionName")
fun SubmitButton(texts: Source<String>,deviceType: DeviceType,disabled: Boolean = false, dataId: String? = null, onClick: ()->Unit) = Button(
    attrs = {
        if(disabled) disabled()
        if(dataId != null) dataId(dataId)
        style {
            submitButtonStyle(deviceType)()
        }
        onClick {
            if(disabled) return@onClick
            onClick()
        }
    }
) {
    Text(texts.emit())
}

@Markup
@Composable
@Suppress("FunctionName")
fun CancelButton(texts: Source<String>,deviceType: DeviceType, disabled: Boolean = false, dataId: String? = null, onClick: ()->Unit) = Button(
    attrs = {
        if(disabled) disabled()
        if(dataId != null) dataId(dataId)
        style {
            cancelButtonStyle(deviceType)()
        }
        onClick {
            if(disabled) return@onClick
            onClick()
        }
    }
) {
    Text(texts.emit())
}

@Markup
@Composable
@Suppress("FunctionName")
fun StdButton(texts: Source<String>,deviceType: Source<DeviceType>,disabled: Boolean = false, dataId: String? = null, onClick: ()->Unit) =
    StdButton(texts, deviceType.emit(), disabled, dataId, onClick)

@Markup
@Composable
@Suppress("FunctionName")
fun StdButton(texts: Source<String>, deviceType: DeviceType, isDisabled: Boolean = false, dataId: String? = null, onClick: ()->Unit) = Button(
    attrs = {
        if(isDisabled) disabled()
        if(dataId != null) dataId(dataId)
        style {
            buttonStyle(deviceType)()
        }
        onClick {
            if(isDisabled) return@onClick
            onClick()
        }
    }
) {
    Text(texts.emit())
}

@Markup
@Composable
@Suppress("FunctionName")
fun ColoredButton(color: CSSColorValue, texts: Source<String>, deviceType: DeviceType, isDisabled: Boolean = false, dataId: String? = null, onClick: ()->Unit) = Button(
    attrs = {
        if(isDisabled) disabled()
        if(dataId != null) dataId(dataId)
        style {
            submitButtonStyle(deviceType)()
            backgroundColor(color)
        }
        onClick {
            if(isDisabled) return@onClick
            onClick()
        }
    }
) {
    Text(texts.emit())
}

@Markup
@Composable
@Suppress("FunctionName")
fun IconButton(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,
    classes: Array<String>,
    texts: Source<String?> = {null},
    deviceType: Source<DeviceType>,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit) = Button(
    attrs = {
        if(texts.emit() != null) title(texts.emit()!!)
        if(isDisabled) disabled()
        if(dataId != null) dataId(dataId)
        style {
            symbolicButtonStyle(deviceType.emit())()
            color(color)
            property("border-color", color)
            backgroundColor(bgColor)
            if(isDisabled) {
                property("opacity", 0.5)
                cursor("not-allowed")
            }
        }
        onClick {
            if(isDisabled) return@onClick
            onClick()
        }
    }
) {
    I({
        classes(*classes)
    })
}

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

