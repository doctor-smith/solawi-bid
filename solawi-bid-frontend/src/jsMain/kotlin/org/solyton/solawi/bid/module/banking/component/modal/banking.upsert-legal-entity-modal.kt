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
import org.evoleq.language.component
import org.evoleq.language.subComp
import org.evoleq.math.Source
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.dom.ElementScope
import org.solyton.solawi.bid.module.banking.component.form.BankAccountForm
import org.solyton.solawi.bid.module.banking.component.form.LegalEntityForm
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.deviceData
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.creditor.identifier.CreditorIdentifier
import org.solyton.solawi.bid.module.banking.data.legalentity.LegalEntity
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.values.LegalEntityId
import org.w3c.dom.HTMLElement


@Markup
@Suppress("FunctionName")
fun UpsertLegalEntityModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    storage: Storage<BankingApplication>,
    device: Source<DeviceType>,
    partyId: LegalEntityId,
    legalEntity: LegalEntity?,
    creditorIdentifier: CreditorIdentifier?,
    setLegalEntity: (LegalEntity, CreditorIdentifier?)->Unit,
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
    val inputs = texts.component("inputs")
    Wrap {
        LegalEntityForm(
            {inputs} ,
            partyId,
            legalEntity,
            creditorIdentifier,
            setLegalEntity
        )
    }
}

@Markup
fun Storage<Modals<Int>>.showUpsertLegalEntityModal(
    storage: Storage<BankingApplication>,
    texts: Lang.Block,
    device: Source<DeviceType>,
    partyId: LegalEntityId,
    creditorIdentifier: CreditorIdentifier?,
    legalEntity: LegalEntity?,
    setLegalEntity: (LegalEntity, CreditorIdentifier?)->Unit,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        UpsertLegalEntityModal(
            this,
            texts,
            this@showUpsertLegalEntityModal,
            storage,
            device,
            partyId,
            legalEntity,
            creditorIdentifier,
            setLegalEntity,
            update = update
        )
    ) )
}
