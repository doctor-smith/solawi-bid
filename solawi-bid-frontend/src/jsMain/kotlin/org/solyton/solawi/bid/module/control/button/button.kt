package org.solyton.solawi.bid.module.control.button

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.dataId
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.device.DeviceType
import org.solyton.solawi.bid.application.ui.style.button.buttonStyle
import org.solyton.solawi.bid.application.ui.style.button.cancelButtonStyle
import org.solyton.solawi.bid.application.ui.style.button.submitButtonStyle

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