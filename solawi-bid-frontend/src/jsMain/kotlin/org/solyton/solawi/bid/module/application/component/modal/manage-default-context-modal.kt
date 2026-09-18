package org.solyton.solawi.bid.module.application.component.modal

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.modal.*
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.jetbrains.compose.web.dom.ElementScope
import org.w3c.dom.HTMLElement


@Markup
@Suppress("FunctionName")
fun ManageDefaultContextModal(
    id: Int,
    texts: Source<Lang.Block>,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    styles: (Source<DeviceType>)-> ModalStyles,
    cancel: ()->Unit,
    update: ()->Unit,
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    type = ModalType.Dialog,
    id = id,
    modals = modals,
    device = device,

    onOk = {
        update()
    },
    onCancel = {
        cancel()
    },
    texts = texts.emit(),
    styles = styles(device),
) {

}

@Markup
fun Storage<Modals<Int>>.showManageDefaultContextModal(
    texts: Source<Lang.Block>,
    device: Source<DeviceType>,
    styles: (Source<DeviceType>)-> ModalStyles,
    cancel: ()->Unit,
    update: ()->Unit,
) = with(nextId()) {
    put(this to ModalData(this,
        ModalType.Dialog,
        ManageDefaultContextModal(
            this,
            texts,
            this@showManageDefaultContextModal,
            device,
            styles,

            cancel,
            update,
        )
    ) )
}
