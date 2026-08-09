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
import org.evoleq.language.subComp
import org.evoleq.language.texts
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.dom.ElementScope
import org.solyton.solawi.bid.module.banking.component.form.sepa.MergeSepaMessagesData
import org.solyton.solawi.bid.module.banking.component.form.sepa.MergeSepaMessagesForm
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.deviceData
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.w3c.dom.HTMLElement

@Markup
@Suppress("FunctionName", "UnusedParameter")
fun MergeSepaMessagesModal(
    id: Int,
    parentModalId: Int? = null,
    texts: Source<Lang.Block> = mergeSepaMessagesModalTexts,
    modals: Storage<Modals<Int>>,
    storage: Storage<BankingApplication>,
    device: Source<DeviceType>,
    isOkButtonDisabled: () -> Boolean,
    data: MergeSepaMessagesData,
    setData: (MergeSepaMessagesData)->Unit,
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
    texts = texts.emit(),
    styles = commonModalStyles(device).compact(),
    isOkButtonDisabled = isOkButtonDisabled
) {
    MergeSepaMessagesForm(
        texts * subComp("mergeSepaMessagesForm"),
        data,
        setData
    )
}


@Markup
fun Storage<Modals<Int>>.showMergeSepaMessagesModal(
    parentModalId: Int? = null,
    storage: Storage<BankingApplication>,
    texts: Source<Lang.Block>,
    device: Source<DeviceType>,
    isOkButtonDisabled: () -> Boolean,
    data: MergeSepaMessagesData,
    setData: (MergeSepaMessagesData)->Unit,
    update: () -> Unit
) = with(nextId()) {
    put(this to ModalData(this,
        when (parentModalId) {
            null -> ModalType.Dialog
            else -> ModalType.Child<Int>(parentModalId)
        },
        MergeSepaMessagesModal(
            this,
            parentModalId,
            texts,
            this@showMergeSepaMessagesModal,
            storage,
            device,
            isOkButtonDisabled,
            data,
            setData,
            update = update
        )
    ) )
}

val mergeSepaMessagesModalTexts = Source {
    "dialog" texts {
        "title" colon "Merge SEPA messages"
        "okButton" block {
            "title" colon "Ok"
        }
        "cancelButton" block {
            "title" colon "Cancel"
        }
        +mergeSepaMessagesFormTexts.emit()
    }
}

val mergeSepaMessagesFormTexts by lazy {
    Source {
        "mergeSepaMessagesForm" texts {
            "inputs" block {
                "executionDate" block {
                    "label" block {
                        "title" colon "Execution date"
                    }
                }
                "remittanceInformation" block {
                    "label" block {
                        "title" colon "Remittance information"
                    }
                }
            }
        }
    }
}
