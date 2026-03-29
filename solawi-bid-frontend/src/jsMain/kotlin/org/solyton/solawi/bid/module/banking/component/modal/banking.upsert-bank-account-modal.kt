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

/**
 * Displays a modal for creating or updating a bank account.
 *
 * @param id The unique identifier for the modal.
 * @param texts The content and localization texts for the modal.
 * @param modals A storage mechanism for managing modal states.
 * @param storage The primary storage containing application state.
 * @param device The source of the current device type.
 * @param legalEntity The associated legal entity identifier for the bank account.
 * @param bankAccount The current bank account being modified, or null for creating a new account.
 * @param setBankAccount A callback function to update the bank account state.
 * @param update A function to trigger updates after completing the operation.
 * @return A composable lambda function to render the modal.
 */
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
    isOkButtonDisabled: () -> Boolean = {false},
    hasDescription: Boolean = false,
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
    isOkButtonDisabled = isOkButtonDisabled,
    styles = commonModalStyles(device),
) {
    Wrap {
        BankAccountForm(
            {texts},
            legalEntity,
            bankAccount,
            setBankAccount,
            hasDescription,
        )
    }
}

/**
 * Displays the modal to create or update a bank account within the application.
 *
 * @param storage A reference to the storage containing the banking application data.
 * @param texts The language block containing localized text for the modal.
 * @param device A source of the device type information to adjust the modal's behavior or appearance based on the user's device.
 * @param legalEntityId The identifier of the legal entity associated with the bank account.
 * @param bankAccount The bank account object to edit, or null if creating a new bank account.
 * @param setBankAccount A callback function to handle updates to the bank account object.
 * @param update A function to invoke when updates are made and need processing.
 */
@Markup
fun Storage<Modals<Int>>.showUpsertBankAccountModal(
    storage: Storage<BankingApplication>,
    texts: Lang.Block,
    device: Source<DeviceType>,
    legalEntityId: LegalEntityId,
    bankAccount: BankAccount?,
    setBankAccount: (BankAccount)->Unit = {},
    isOkButtonDisabled: () -> Boolean = {false},
    hasDescription: Boolean = false,
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
            isOkButtonDisabled,
            hasDescription,
            update = update
        )
    ) )
}
