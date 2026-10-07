package org.evoleq.compose.modal

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.dataId
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.language.Block
import org.evoleq.language.get
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.remove
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.solyton.solawi.bid.module.control.button.CancelButton
import org.solyton.solawi.bid.module.control.button.SubmitButton
import org.solyton.solawi.bid.module.style.button.symbolicButtonStyle
import org.solyton.solawi.bid.module.style.page.SubTitleOfH3
import org.w3c.dom.HTMLElement

@Markup
@Suppress("FunctionName", "UnusedParameter")
fun <Id> Modal(
    type: ModalType<Id>,
    id: Id,
    modals: Storage<Modals<Id>>,
    device: Source<DeviceType>,
    onOk: ()->Unit,
    onCancel: (()->Unit)?,
    texts: Block,
    styles: ModalStyles = ModalStyles(),
    dataId: String? = null,
    isOkButtonDisabled: ()->Boolean = {false},
    content: @Composable ElementScope<HTMLElement>.()->Unit
):  @Composable ElementScope<HTMLElement>.()->Unit = {

    val close: Id.()-> Unit = { modals.remove( this )}
    ModalContainer(
        styles.containerStyle
    ) {

        ModalHeader(
            id = id,
            device = device,
            close = if(onCancel != null) {close} else {null},
            texts = texts
        )
        ModalContentWrapper(
            dataId = dataId,
            styles = styles.contentWrapperStyle
        ) {
            content()
        }
        // Vertical space
        // Div({style { flexGrow(1) }}){}
        ModalFooter(
            id,
            device,
            onOk,
            onCancel,
            close,
            texts,
            styles,
            dataId,
            isOkButtonDisabled,
        )
    }
}

@Markup
@Composable
@Suppress("FunctionName")
fun ModalContainer(
    styles : StyleScope.() -> Unit,
    content: @Composable ElementScope<HTMLElement>.()->Unit
)  = Div({
    style {
        border {
            style = LineStyle.Solid
            color = Color("#e2e8f0")
            width = 1.px
        }
        borderRadius(12.px)
        backgroundColor(Color.white)
        property("box-shadow", "0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.05)")
        width(90.percent)
        marginLeft(5.percent)
        padding(20.px)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        styles()
    }
}) {
    content()
}

@Markup
@Composable
@Suppress("FunctionName")
fun <Id> ModalHeader(
    id: Id,
    device: Source<DeviceType>,
    close: (Id.() -> Unit)?,
    texts: Block,
    dataId: String? = null,
) {
    if(close != null) {
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.FlexEnd)
            }
        }) {
            Button({
                if(dataId != null) dataId("$dataId.modal.close-x")
                //classes("button")
                style{
                    symbolicButtonStyle(device.emit())()
                    // backgroundColor(Color.crimson)
                }
                onClick { id.close() }
            }) {
                I({
                    classes("fa-solid", "fa-xmark")
                })
            }
        }
    }

    H3({
        if(dataId != null) dataId("$dataId.modal.title")
        style {
            marginTop(10.px)
            marginLeft(10.px)
            marginBottom(10.px)
        }
    }){
        Text(texts["title"])
    }
    val subTitle = tryOrNull{ texts["subTitle"] }
    if(subTitle != null) SubTitleOfH3(subTitle) {
        marginTop((-10).px)
        marginLeft(10.px)
        marginBottom(10.px)
    }
}
fun <T> tryOrNull(block: ()->T) = try{block()}catch(_: Exception) { null }
@Markup
@Composable
@Suppress("FunctionName")
fun ModalContentWrapper(
   dataId: String? = null,
   styles: StyleScope.()->Unit,
   content: @Composable ElementScope<HTMLElement>.()->Unit
) = Div({ // content Wrapper

    if(dataId != null) dataId("$dataId.modal.content-wrapper")
    style {
        marginBottom(10.px)
        minHeight(1.px)
        maxHeight(80.vh)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        flexGrow(1)
        styles()
    }
}) {
       content()
}

@Markup
@Composable
@Suppress("FunctionName")
fun <Id> ModalFooter(
    id: Id,
    device: Source<DeviceType>,
    onOk: ()->Unit,
    onCancel: (()->Unit)?,
    close: Id.()->Unit,
    texts: Block,
    styles: ModalStyles = ModalStyles(),
    dataId: String? = null,
    isOkButtonDisabled: ()->Boolean = {false},
) = Div({
    style {
        marginTop(16.px)
        paddingTop(12.px)
        property("border-top", "1px solid #f1f5f9")
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.FlexEnd)
        alignItems(AlignItems.Center)
        gap(10.px)
        with(styles) {
            footerWrapperStyle()
        }
    }
}) {
    if(onCancel != null) {
        CancelButton(
            texts = {texts["cancelButton.title"]},
            deviceType = device.emit(),
            styles = styles.cancelButtonStyles,
            dataId = "$dataId.modal.close-button",
        ) {
            onCancel()
            id.close()
        }
    }
    SubmitButton(
        texts = {texts["okButton.title"]},
        deviceType = device.emit(),
        styles = styles.okButtonStyles,
        dataId = "$dataId.modal.submit-button",
        disabled = isOkButtonDisabled()
    ) {
        onOk()
        id.close()
    }
}

