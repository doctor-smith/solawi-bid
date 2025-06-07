package org.solyton.solawi.bid.module.error.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.language.Lang
import org.evoleq.language.get
import org.evoleq.math.Source
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.device.DeviceType
import org.w3c.dom.HTMLElement

@Markup
@Suppress("FunctionName")
fun ErrorModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    dataId: String? = null,
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    id = id,
    modals = modals,
    device = device,
    onOk = {},
    onCancel = null,
    texts = texts,
    dataId = dataId,
) {
    Div {
        Text(texts["content.message"])
    }
}


@Markup
fun Storage<Modals<Int>>.showErrorModal(
    texts: Lang.Block,
    device: Source<DeviceType>,
    dataId: String? = null,
) = with(nextId()){
    put(this to ModalData(
        ModalType.Error,
        ErrorModal(this, texts, this@showErrorModal, device, dataId)
    ))
}