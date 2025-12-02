package org.evoleq.compose.modal

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.optics.storage.Storage
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
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
            zIndex +100,
            modals.components(ModalType.CookieDisclaimer)
        ) {
            flexDirection(FlexDirection.Column)
            justifyContent(JustifyContent.FlexEnd)
            alignItems(AlignItems.Center)
            marginBottom(100.px)
        }
        SubLayer("Dialogs",
            zIndex +200,
            modals.components(ModalType.Dialog)
        ) {
            flexDirection(FlexDirection.Column)
            justifyContent(JustifyContent.Center)
            alignItems(AlignItems.Center)
        }
        SubLayer("Error",
            zIndex +300,
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
@Suppress("FunctionName", "UNUSED_VARIABLE", "UNUSED_PARAMETER")
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
