package org.solyton.solawi.bid.module.shares.component.modal

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.device.data.mediaType
import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.dom.ElementScope
import org.solyton.solawi.bid.module.banking.component.form.PartialSepaCollection
import org.solyton.solawi.bid.module.banking.component.form.SepaCollectionForm
import org.solyton.solawi.bid.module.banking.component.form.defaultSepaCollectionInputs
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.deviceData
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.sepa.collection.SepaCollection
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.w3c.dom.HTMLElement


@Markup
@Suppress("FunctionName")
fun AttachSepaCollectionModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    storage: Storage<BankingApplication>,
    device: Source<DeviceType>,
    bankAccounts: List<BankAccount>,
    sepaCollection: PartialSepaCollection?,
    setSepaCollection: (PartialSepaCollection) -> Unit,
    isOkButtonDisabled: () -> Boolean,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    id,
    modals,
    storage * deviceData * mediaType.get,
    onOk = {
        update()
    },
    onCancel = {},
    texts = texts,
    styles = commonModalStyles(device),
    isOkButtonDisabled = isOkButtonDisabled
) {
    Wrap {
        SepaCollectionForm(
            inputs = Source{defaultSepaCollectionInputs()} ,
            bankAccounts =  bankAccounts,
            sepaCollection = sepaCollection,
            setSepaCollection = setSepaCollection
        )
    }
}

@Markup
fun Storage<Modals<Int>>.showAttachSepaCollectionModal(
    storage: Storage<BankingApplication>,
    texts: Lang.Block,
    device: Source<DeviceType>,
    bankAccounts: List<BankAccount>,
    sepaCollection: PartialSepaCollection?,
    setSepaCollection: (PartialSepaCollection) -> Unit,
    isOkButtonDisabled: () -> Boolean,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        AttachSepaCollectionModal(
            this,
            texts,
            this@showAttachSepaCollectionModal,
            storage,
            device,
            bankAccounts,
            sepaCollection,
            setSepaCollection,
            isOkButtonDisabled,
            update = update
        )
    ) )
}
