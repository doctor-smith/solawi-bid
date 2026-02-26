package org.solyton.solawi.bid.application.ui.component.organization

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.jetbrains.compose.web.dom.ElementScope
import org.solyton.solawi.bid.module.user.component.styles.modalStyles
import org.w3c.dom.HTMLElement


@Markup
@Suppress("FunctionName", "UNUSED_PARAMETER")
fun CreateMemberOfOrganizationModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    csv: String?,
    setCsv: (String)->Unit,
    isOkButtonDisabled: ()->Boolean,
    cancel: ()->Unit,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    id,
    modals,
    device,
    onOk = {
        update()
    },
    onCancel = {
        cancel()
    },
    isOkButtonDisabled = isOkButtonDisabled,
    texts = texts,
    styles = modalStyles(device),
) {

}


@Markup
fun Storage<Modals<Int>>.showCreateMembersOfOrganizationModal(
    texts: Lang.Block,
    device: Source<DeviceType>,
    csv: String? = null,
    setCsv: (String) -> Unit,
    isOkButtonDisabled: ()->Boolean = {false},
    cancel: ()->Unit = {},
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        CreateMemberOfOrganizationModal(
            this,
            texts,
            this@showCreateMembersOfOrganizationModal,
            device,
            csv,
            setCsv,
            isOkButtonDisabled,
            cancel,
            update
        )
    ) )
}
