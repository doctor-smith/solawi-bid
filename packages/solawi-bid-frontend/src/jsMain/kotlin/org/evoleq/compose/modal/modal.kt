package org.evoleq.compose.modal

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.language.Block
import org.evoleq.language.get
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.remove
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.solyton.solawi.bid.application.data.device.DeviceType
import org.solyton.solawi.bid.application.ui.style.button.buttonStyle
import org.solyton.solawi.bid.application.ui.style.button.symbolicButtonStyle
import org.solyton.solawi.bid.module.control.button.CancelButton
import org.solyton.solawi.bid.module.control.button.SubmitButton
import org.w3c.dom.HTMLElement

typealias Modals<Id> = Map<Id, ModalData>//@Composable ElementScope<HTMLElement>.() -> Unit>

interface ModalType {
    object Dialog : ModalType
    object Error : ModalType
    object CookieDisclaimer : ModalType
}
data class ModalData(
    val type: ModalType,
    val component: @Composable ElementScope<HTMLElement>.() -> Unit
)

@Markup
@Composable
@Suppress("FunctionName")
fun <Id> ModalLayer(
    zIndex: Int = 1000,
    modals: Storage<Modals<Id>>,
    content: @Composable ElementScope<HTMLElement>.()->Unit
) {
    if(modals.read().keys.isNotEmpty()) {
        ModalBackground(zIndex)
        SubLayer("CookieDisclaimer",
            zIndex +1,
            modals.components(ModalType.CookieDisclaimer)
        ) {
            flexDirection(FlexDirection.Column)
            justifyContent(JustifyContent.FlexEnd)
            alignItems(AlignItems.Center)
            marginBottom(100.px)
        }
        SubLayer("Dialogs",
            zIndex +2,
            modals.components(ModalType.Dialog)
        ) {
            flexDirection(FlexDirection.Column)
            justifyContent(JustifyContent.Center)
            alignItems(AlignItems.Center)
        }
        SubLayer("Error",
            zIndex +3,
            modals.components(ModalType.Error)
        ) {
            flexDirection(FlexDirection.Column)
            alignItems(AlignItems.Center)
        }
    }
    Div {
        content()
    }
}

@Markup
@Suppress("FunctionName")
@Composable
fun ModalBackground(zIndex: Int) = Div({
    style {
        property("z-index", zIndex)
        position(Position.Absolute)
        width(100.vw)
        boxSizing("border-box")
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        height(100.vh)
        backgroundColor(Color.black)
        opacity(0.5)
    }
}){}

@Markup
@Composable
@Suppress("FunctionName", "UnusedVariable")
fun SubLayer(name: String, index: Int, modals: List<@Composable ElementScope<HTMLElement>.()->Unit>, styles: StyleScope.()->Unit, ) {
    if(modals.isNotEmpty()) {Div({
        style {
            property("z-index", index)
            position(Position.Absolute)
            width(100.vw)
            height(100.vh)
            boxSizing("border-box")
            display(DisplayStyle.Flex)
            backgroundColor(Color.transparent)
            styles()
        }
    }){
        modals.forEach {
            it()
        }
    }}
}


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
                    device.emit()
                ) {
                    onCancel()
                    id.close()
                }
            }
            SubmitButton(
                {texts["okButton.title"]},
                device.emit()
            ) {
                onOk()
                id.close()
            }
        }
    }
}
