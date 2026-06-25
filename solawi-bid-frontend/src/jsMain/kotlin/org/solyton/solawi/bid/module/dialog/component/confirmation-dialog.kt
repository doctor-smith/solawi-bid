package org.solyton.solawi.bid.module.dialog.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalStyles
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.language.Lang
import org.evoleq.language.get
import org.evoleq.math.Source
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.marginLeft
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.w3c.dom.HTMLElement


@Markup
@Suppress("FunctionName")
fun DialogModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    dataId: String? = null,
    symbol: (@Composable ElementScope<HTMLElement>.()->Unit)? = null,
    onCancel: (()->Unit)? = null,
    onOk: () -> Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    type = ModalType.Dialog,
    id = id,
    modals = modals,
    device = device,
    onOk = onOk,
    onCancel = onCancel,
    texts = texts,
    dataId = dataId,
) {
    Wrap({padding(10.px)}){
        Horizontal({gap(10.px)}) {
            When(symbol != null) {
                symbol!!()
            }

            Div {
                Text(texts["content.message"])
            }
        }
    }
}



@Markup
fun Storage<Modals<Int>>.showDialogModal(
    texts: Lang.Block,
    device: Source<DeviceType>,
    dataId: String? = null,
    symbol: (@Composable ElementScope<HTMLElement>.()->Unit)? = null,
    onCancel: (()->Unit)? = null,
    onOk: () -> Unit
) = with(nextId()){
    put(this to ModalData(this,
        ModalType.Dialog,
        DialogModal(
            id = this,
            texts = texts,
            modals = this@showDialogModal,
            device = device,
            dataId = dataId,
            symbol = symbol,
            onCancel = onCancel,
            onOk = onOk
        )
    ))
}


@Markup
@Suppress("FunctionName")
fun ChildDialogModal(
    parentModalId: Int,
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    dataId: String? = null,
    symbol: (@Composable ElementScope<HTMLElement>.()->Unit)? = null,
    onCancel: (()->Unit)? = null,
    onOk: () -> Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    type = ModalType.Child(parentModalId),
    id = id,
    modals = modals,
    device = device,
    onOk = onOk,
    onCancel = onCancel,
    texts = texts,
    dataId = dataId,
    styles = commonModalStyles(device)
) {
    Wrap({padding(10.px)}){
        Horizontal({gap(10.px)}) {
            When(symbol != null) {
                symbol!!()
            }

            Div {
                Text(texts["content.message"])
            }
        }
    }
}


@Markup
fun Storage<Modals<Int>>.showChildDialogModal(
    parentModalId: Int,
    texts: Lang.Block,
    device: Source<DeviceType>,
    dataId: String? = null,
    symbol: (@Composable ElementScope<HTMLElement>.()->Unit)? = null,
    onCancel: (()->Unit)? = null,
    onOk: () -> Unit
) = with(nextId()){
    put(this to ModalData(this,
        ModalType.Child(parentModalId),
        ChildDialogModal(
            parentModalId = parentModalId,
            id = this,
            texts = texts,
            modals = this@showChildDialogModal,
            device = device,
            dataId = dataId,
            symbol = symbol,
            onCancel = onCancel,
            onOk = onOk
        )
    ))
}

