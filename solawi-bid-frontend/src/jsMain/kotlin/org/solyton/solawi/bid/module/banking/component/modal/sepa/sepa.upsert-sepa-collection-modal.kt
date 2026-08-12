package org.solyton.solawi.bid.module.banking.component.modal.sepa

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.device.data.mediaType
import org.evoleq.language.Lang
import org.evoleq.language.component
import org.evoleq.language.extend
import org.evoleq.language.texts
import org.evoleq.math.Source
import org.evoleq.math.onError
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.dom.ElementScope
import org.solyton.solawi.bid.module.banking.component.form.sepa.FormConfiguration
import org.solyton.solawi.bid.module.banking.component.form.sepa.PartialSepaCollection
import org.solyton.solawi.bid.module.banking.component.form.sepa.UpsertSepaCollectionForm
import org.solyton.solawi.bid.module.banking.component.form.sepa.defaultSepaCollectionInputs
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.deviceData
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.w3c.dom.HTMLElement


val defaultUpsertSepaCollectionModalTexts by lazy{
    "upsertSepaCollectionDialog" texts {
        "title" colon "Upsert SEPA Collection"
        "okButton" block {
            "title" colon "Ok"
        }
        "cancelButton" block {
            "title" colon "Cancel"
        }
    } extend { defaultSepaCollectionInputs() }
}

@Markup
@Suppress("FunctionName")
fun UpsertSepaCollectionModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    storage: Storage<BankingApplication>,
    device: Source<DeviceType>,
    configuration: FormConfiguration = FormConfiguration(),
    creditorBankAccounts: List<BankAccount>,
    partialSepaCollection: PartialSepaCollection?,
    setPartialSepaCollection: (PartialSepaCollection) -> Unit,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    type = ModalType.Dialog,
    id = id,
    modals = modals,
    device = storage * deviceData * mediaType.get,
    onOk = {
        update()
    },
    onCancel = {},
    texts = texts,
    styles = commonModalStyles(device),
) {
    val formTexts = Source{ texts.component("inputs") } onError { defaultSepaCollectionInputs() }

    Wrap {
        UpsertSepaCollectionForm(
            inputs = formTexts,
            configuration = configuration,
            bankAccounts = creditorBankAccounts,
            sepaCollection = partialSepaCollection,
            setSepaCollection = setPartialSepaCollection,
        )
    }
}

@Markup
fun Storage<Modals<Int>>.showUpsertSepaCollectionModal(
    storage: Storage<BankingApplication>,
    texts: Lang.Block,
    device: Source<DeviceType>,
    configuration: FormConfiguration = FormConfiguration(),
    creditorBankAccounts: List<BankAccount>,
    partialSepaCollection: PartialSepaCollection?,
    setPartialSepaCollection: (PartialSepaCollection) -> Unit,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(this,
        ModalType.Dialog,
        UpsertSepaCollectionModal(
            this,
            texts,
            this@showUpsertSepaCollectionModal,
            storage,
            device,
            configuration,
            creditorBankAccounts,
            partialSepaCollection,
            setPartialSepaCollection,
            update = update
        )
    ) )
}
