package org.solyton.solawi.bid.module.user.component.modal


import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.dnd.Dropzone
import org.evoleq.compose.dnd.readFileContent
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
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.w3c.dom.HTMLElement

@Markup
@Suppress("FunctionName", "UNUSED_PARAMETER")
fun ImportMembersToOrganizationModal(
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
    styles = auctionModalStyles(device),
) {
    var isProcessingFileContent by remember{ mutableStateOf(false) }

    Dropzone(
        onProcessingStarted = {isProcessingFileContent = true},
        onProcessingStopped = {isProcessingFileContent = false},
    ) { files ->
        files.filter { it.name.endsWith(".csv") }.map {
            readFileContent(it) { content ->
                console.log("Inhalt gelesen:", content)
                setCsv(content)
            }
        }
    }
}



@Markup
fun Storage<Modals<Int>>.showImportMembersToOrganizationModal(
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
        ImportMembersToOrganizationModal(
            this,
            texts,
            this@showImportMembersToOrganizationModal,
            device,
            csv,
            setCsv,
            isOkButtonDisabled,
            cancel,
            update
        )
    ) )
}
