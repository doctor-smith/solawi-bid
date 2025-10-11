package org.solyton.solawi.bid.module.control.button

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.dataId
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.optics.prism.Either
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.I
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.style.button.buttonStyle
import org.solyton.solawi.bid.module.style.button.cancelButtonStyle
import org.solyton.solawi.bid.module.style.button.submitButtonStyle
import org.solyton.solawi.bid.module.style.button.symbolicButtonStyle
import org.solyton.solawi.bid.module.style.data.Side

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

/**
 * Show icon on the left side of a text
 */
@Markup
@Composable
@Suppress("FunctionName", "CognitiveComplexMethod")
fun IconButtonWithText(
    color: CSSColorValue,
    bgColor: CSSColorValue = Color.transparent,
    classes: Array<String>,
    text: Source<String> = {""},
    tooltip: Source<String?> = {null},
    deviceType: Source<DeviceType>,
    iconSide: Side = Side.Left,
    isDisabled: Boolean = false,
    dataId: String? = null,
    onClick: ()->Unit) = Button(
    attrs = {
        if(tooltip.emit() != null) title(tooltip.emit()!!)
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
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(0.5.em)
            if(iconSide is Side.Right) flexDirection(FlexDirection.RowReverse)
        }
        onClick {
            if(isDisabled) return@onClick
            onClick()
        }
    }
) {
    Span({style {
        width(2.em)
        height(auto)
        flexShrink(0)
    }}){
        I({
            classes(*classes)
            style {
                width(2.em)
                height(auto)
                flexShrink(0)
            }
        })
    }
    Span{ Text(text.emit()) }
}
