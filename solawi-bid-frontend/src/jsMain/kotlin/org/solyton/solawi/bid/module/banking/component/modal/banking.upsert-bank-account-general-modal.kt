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
import org.solyton.solawi.bid.module.banking.component.form.BankAccountFormWithUserSearch
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.deviceData
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.legalentity.LegalEntity
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser
import org.solyton.solawi.bid.module.values.LegalEntityId
import org.w3c.dom.HTMLElement

/**
 * Displays a modal window that allows the creation or modification of a bank account.
 *
 * @param id A unique identifier for the modal, used to track and manage its instance.
 * @param texts A localization block providing the text content for the modal.
 * @param modals A storage managing modal states, such as their visibility or active state.
 * @param storage The shared storage containing the application's banking-related data.
 * @param device A data source representing the user's current device type.
 * @param legalEntities A list of users tied to the legal entities eligible for selection in the modal.
 * @param legalEntityId The identifier of the legal entity involved in the bank account operation.
 * @param bankAccount An optional bank account object to prefill the form for editing; null indicates creation of a new account.
 * @param setBankAccount A callback to persist changes to the edited or newly created bank account object.
 * @param update A method invoked to notify about the completion of create or update actions.
 * @return A composable lambda function rendering this modal on the UI.
 */
@Markup
@Suppress("FunctionName")
fun UpsertBankAccountWithUserSearchModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    storage: Storage<BankingApplication>,
    device: Source<DeviceType>,
    legalEntities: List<ManagedUser> = emptyList(),
    legalEntityId: LegalEntityId?,
    bankAccount: BankAccount?,
    setBankAccount: (BankAccount)->Unit,
    isOkButtonDisabled: () -> Boolean = {false},
    hasDescription: Boolean = false,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    id = id,
    modals = modals,
    device = storage * deviceData * mediaType.get,
    onOk = {
        update()
    },
    onCancel = {},
    texts = texts,
    isOkButtonDisabled = isOkButtonDisabled,
    styles = commonModalStyles(device),
) {
    Wrap {
        BankAccountFormWithUserSearch(
            {texts},
            legalEntities,
            legalEntityId,
            bankAccount,
            setBankAccount,
            hasDescription
        )
    }
}

/**
 * Serves as an entry point for displaying a modal that facilitates the creation or modification of a bank account within the application.
 *
 * @param storage A storage instance containing the banking application's current state and data.
 * @param texts A localization language block providing the necessary modal text content.
 * @param device A source for determining the user's device type, enabling device-specific modal rendering.
 * @param legalEntities A list of managed users linked to the legal entities available for selection in the modal.
 * @param legalEntityId The unique identifier of the legal entity whose account is being created or updated.
 * @param bankAccount An optional bank account object to be prefilled for editing; if null, the modal begins in create mode.
 * @param setBankAccount A callback function invoked to persist updates to the bank account object based on user input.
 * @param update A function called to process and finalize the changes when the modal operation is completed.
 */
@Markup
fun Storage<Modals<Int>>.showUpsertBankAccountWithUserSearchModal(
    storage: Storage<BankingApplication>,
    texts: Lang.Block,
    device: Source<DeviceType>,
    legalEntities: List<ManagedUser> = emptyList(),
    legalEntityId: LegalEntityId?,
    bankAccount: BankAccount?,
    setBankAccount: (BankAccount)->Unit = {},
    isOkButtonDisabled: () -> Boolean = {false},
    hasDescription: Boolean = false,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        UpsertBankAccountWithUserSearchModal(
            this,
            texts,
            this@showUpsertBankAccountWithUserSearchModal,
            storage,
            device,
            legalEntities,
            legalEntityId,
            bankAccount,
            setBankAccount,
            isOkButtonDisabled,
            hasDescription,
            update = update
        )
    ) )
}
