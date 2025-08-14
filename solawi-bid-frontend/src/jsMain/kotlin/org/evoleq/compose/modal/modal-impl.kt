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
import org.w3c.dom.HTMLElement

@Markup
@Suppress("FunctionName")
fun <Id> Modal(
    id: Id,
    modals: Storage<Modals<Id>>,
    device: Source<DeviceType>,
    onOk: ()->Unit,
    onCancel: (()->Unit)?,
    texts: Block,
    styles: ModalStyles = ModalStyles(),
    dataId: String? = null,
    content: @Composable ElementScope<HTMLElement>.()->Unit
):  @Composable ElementScope<HTMLElement>.()->Unit = {

    val close: Id.()-> Unit = { modals.remove( this )}

    Div({
        style {
            // minHeight("300px")
            border {
                style = LineStyle.Solid
                color = Color("black")
                width = 1.px
            }
            borderRadius(10.px)
            backgroundColor(Color.white)
            width(90.percent)
            marginLeft(5.percent)
            padding(10.px)
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            with(styles){containerStyle()}
        }
    }) {
        //
        // Header
        //
        if(onCancel != null) {
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

        //
        // Content area
        //
        Div({

            if(dataId != null) dataId("$dataId.modal.content-wrapper")
            style {
                maxWidth(80.pc)
                marginLeft(10.px)
                marginBottom(10.px)
                minHeight(1.px)
                maxHeight(80.vh)
            }
        }) {
            content()
        }
        // Vertical space
        Div({style { flexGrow(1) }}){}
        //
        // Footer
        //
        Div({
            style {
                height(30.px)
                marginBottom(0.px)
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.FlexEnd)
            }
        }) {
            if(onCancel != null) {
                CancelButton(
                    {texts["cancelButton.title"]},
                    device.emit(),
                    dataId = "$dataId.modal.close-button",
                ) {
                    onCancel()
                    id.close()
                }
            }
            SubmitButton(
                {texts["okButton.title"]},
                device.emit(),
                dataId = "$dataId.modal.submit-button",
            ) {
                onOk()
                id.close()
            }
        }
    }
}
