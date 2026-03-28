package org.solyton.solawi.bid.module.banking.component.modal

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
import org.solyton.solawi.bid.module.banking.component.form.BankAccountForm
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.deviceData
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.values.LegalEntityId
import org.w3c.dom.HTMLElement


@Markup
@Suppress("FunctionName")
fun UpsertBankAccountModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    storage: Storage<BankingApplication>,
    device: Source<DeviceType>,
    legalEntity: LegalEntityId,
    bankAccount: BankAccount?,
    setBankAccount: (BankAccount)->Unit,
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
) {
    Wrap {
        BankAccountForm(
            {texts},
            legalEntity,
            bankAccount,
            setBankAccount
        )
    }
}

@Markup
fun Storage<Modals<Int>>.showUpsertBankAccountModal(
    storage: Storage<BankingApplication>,
    texts: Lang.Block,
    device: Source<DeviceType>,
    legalEntityId: LegalEntityId,
    bankAccount: BankAccount?,
    setBankAccount: (BankAccount)->Unit = {},
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        UpsertBankAccountModal(
            this,
            texts,
            this@showUpsertBankAccountModal,
            storage,
            device,
            legalEntityId,
                    bankAccount,
            setBankAccount,
            update = update
        )
    ) )
}
