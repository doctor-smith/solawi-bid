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
import org.solyton.solawi.bid.module.banking.component.form.UpdateSepaPaymentForm
import org.solyton.solawi.bid.module.banking.component.form.updateSepaPaymentFormTexts
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.deviceData
import org.solyton.solawi.bid.module.banking.data.sepa.payment.SepaPayment
import org.solyton.solawi.bid.module.banking.i18n.updateSepaPaymentForm
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.w3c.dom.HTMLElement


@Markup
@Suppress("FunctionName", "UnusedParameter")
fun UpdateSepaPaymentModal(
    id: Int,
    parentModalId: Int? = null,
    texts: Source<Lang.Block>,
    modals: Storage<Modals<Int>>,
    storage: Storage<BankingApplication>,
    device: Source<DeviceType>,
    sepaPayment: SepaPayment,
    setSepaPayment: (SepaPayment) -> Unit,
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
    styles = commonModalStyles(device),
) {

    Wrap {
        UpdateSepaPaymentForm(
            texts = texts * updateSepaPaymentForm,
            sepaPayment = sepaPayment,
            setSepaPayment = setSepaPayment
        )
    }
}

@Markup
fun Storage<Modals<Int>>.showUpdateSepaPaymentModal(
    parentModalId: Int? = null,
    storage: Storage<BankingApplication>,
    texts: Source<Lang.Block>,
    device: Source<DeviceType>,
    sepaPayment: SepaPayment,
    setSepaPayment: (SepaPayment) -> Unit,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(this,
        when (parentModalId) {
            null -> ModalType.Dialog
            else -> ModalType.Child<Int>(parentModalId)
        },
        UpdateSepaPaymentModal(
            this,
            parentModalId,
            texts,
            this@showUpdateSepaPaymentModal,
            storage,
            device,
            sepaPayment,
            setSepaPayment,
            update = update
        )
    ))
}


val updateSepaPaymentModalTexts = Source {
    "dialog" texts {
        "title" colon "Update SEPA payment"
        "okButton" block {
            "title" colon "Ok"
        }
        "cancelButton" block {
            "title" colon "Cancel"
        }
        +updateSepaPaymentFormTexts.emit()
    }
}
