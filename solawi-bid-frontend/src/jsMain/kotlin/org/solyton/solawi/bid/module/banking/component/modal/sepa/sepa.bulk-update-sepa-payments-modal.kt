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
import org.evoleq.language.texts
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.dom.ElementScope
import org.solyton.solawi.bid.module.banking.component.form.sepa.BulkUpdateSepaPaymentsForm
import org.solyton.solawi.bid.module.banking.component.form.sepa.updateSepaPaymentFormTexts
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.deviceData
import org.solyton.solawi.bid.module.banking.data.sepa.payment.SepaPayment
import org.solyton.solawi.bid.module.banking.i18n.updateSepaPaymentForm
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.w3c.dom.HTMLElement


@Markup
@Suppress("FunctionName", "UnusedParameter")
fun BulkUpdateSepaPaymentsModal(
    id: Int,
    parentModalId: Int? = null,
    texts: Source<Lang.Block>,
    modals: Storage<Modals<Int>>,
    storage: Storage<BankingApplication>,
    device: Source<DeviceType>,
    sepaPayments: List<SepaPayment>,
    setSepaPayments: (List<SepaPayment>) -> Unit,
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
) {

    Wrap {
        BulkUpdateSepaPaymentsForm(
            texts = texts * updateSepaPaymentForm,
            sepaPayments = sepaPayments,
            setSepaPayments = setSepaPayments
        )
    }
}

@Markup
fun Storage<Modals<Int>>.showBulkUpdateSepaPaymentsModal(
    parentModalId: Int? = null,
    storage: Storage<BankingApplication>,
    texts: Source<Lang.Block>,
    device: Source<DeviceType>,
    sepaPayments: List<SepaPayment>,
    setSepaPayments: (List<SepaPayment>) -> Unit,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(this,
        when (parentModalId) {
            null -> ModalType.Dialog
            else -> ModalType.Child<Int>(parentModalId)
        },
        BulkUpdateSepaPaymentsModal(
            this,
            parentModalId,
            texts,
            this@showBulkUpdateSepaPaymentsModal,
            storage,
            device,
            sepaPayments,
            setSepaPayments,
            update = update
        )
    ))
}


val bulkUpdateSepaPaymentsModalTexts = Source {
    "dialog" texts {
        "title" colon "Bulk update SEPA payments"
        "okButton" block {
            "title" colon "Ok"
        }
        "cancelButton" block {
            "title" colon "Cancel"
        }
        +updateSepaPaymentFormTexts.emit()
    }
}
