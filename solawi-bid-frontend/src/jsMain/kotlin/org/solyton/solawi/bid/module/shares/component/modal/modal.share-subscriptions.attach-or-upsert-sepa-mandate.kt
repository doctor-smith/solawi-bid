package org.solyton.solawi.bid.module.shares.component.modal



import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.layout.Horizontal
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
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.RadioInput
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.banking.component.form.sepa.UpsertSepaMandateForm
import org.solyton.solawi.bid.module.banking.component.form.sepa.updateSepaMandateFormTexts
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.deviceData
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.sepa.collection.SepaCollection
import org.solyton.solawi.bid.module.banking.data.sepa.mandate.SepaMandate
import org.solyton.solawi.bid.module.control.dropdown.Dropdown
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.w3c.dom.HTMLElement

sealed class AttachOrUpsertSepaMandateModalMode {
    object Attach : AttachOrUpsertSepaMandateModalMode()
    object Upsert : AttachOrUpsertSepaMandateModalMode()
}

data class AttachOrUpsertSepaMandateModalConfiguration(
    val mode: AttachOrUpsertSepaMandateModalMode
)

@Markup
@Suppress("FunctionName", "UnusedParameter")
fun AttachOrUpsertSepaMandateModal(
    id: Int,
    parentModalId: Int? = null,
    texts: Source<Lang.Block>,
    modals: Storage<Modals<Int>>,
    storage: Storage<BankingApplication>,
    device: Source<DeviceType>,
    bankAccount: BankAccount?,
    collections: List<SepaCollection>,
    usersSepaMandates: List<SepaMandate>,
    mode: AttachOrUpsertSepaMandateModalMode = AttachOrUpsertSepaMandateModalMode.Upsert,
    setMode: (AttachOrUpsertSepaMandateModalMode) -> Unit,
    sepaMandate: SepaMandate?,
    setSepaMandate: (SepaMandate) -> Unit,
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
        var modeState by remember {mutableStateOf(mode)}
        When(usersSepaMandates.isNotEmpty()) {

            val options = usersSepaMandates.associateBy { it.mandateReference.value }
            var selected by remember{ mutableStateOf(sepaMandate?.mandateReference?.value?: options.keys.firstOrNull()) }
            Text("The user has the following sepa mandates:")
            Dropdown(
                options = options,
                selected = selected

            ) {
                (_, value) -> selected = value.mandateReference.value
            }
            Horizontal({
                padding(10.px)
            }) {
                RadioInput(modeState is AttachOrUpsertSepaMandateModalMode.Attach) {
                    onClick{
                        modeState = AttachOrUpsertSepaMandateModalMode.Attach
                        setMode(AttachOrUpsertSepaMandateModalMode.Attach)
                    }
                }
                Text("Attach existing sepa mandate")
            }
            Horizontal({
                padding(10.px)
            }) {
                RadioInput(modeState is AttachOrUpsertSepaMandateModalMode.Upsert) {
                    onClick {
                        modeState = AttachOrUpsertSepaMandateModalMode.Upsert
                        setMode(AttachOrUpsertSepaMandateModalMode.Upsert)
                    }
                }
                Text("Create or update sepa mandate")
            }


        }
        When(usersSepaMandates.isEmpty()) {
            Text("The user has no associated sepa mandates")
        }

        When(modeState is AttachOrUpsertSepaMandateModalMode.Upsert) {
            UpsertSepaMandateForm(
                texts = texts,
                bankAccount = bankAccount,
                collections = collections,
                sepaMandate = sepaMandate,
                setSepaMandate = setSepaMandate
            )
        }
    }
}

@Markup
fun Storage<Modals<Int>>.showAttachOrUpsertSepaMandateModal(
    parentModalId: Int? = null,
    storage: Storage<BankingApplication>,
    texts: Source<Lang.Block>,
    device: Source<DeviceType>,
    bankAccount: BankAccount?,
    collections: List<SepaCollection>,
    usersSepaMandates: List<SepaMandate>,
    mode: AttachOrUpsertSepaMandateModalMode = AttachOrUpsertSepaMandateModalMode.Upsert,
    setMode: (AttachOrUpsertSepaMandateModalMode) -> Unit,
    sepaMandate: SepaMandate?,
    setSepaMandate: (SepaMandate) -> Unit,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(this,
        when (parentModalId) {
            null -> ModalType.Dialog
            else -> ModalType.Child<Int>(parentModalId)
        },
        AttachOrUpsertSepaMandateModal(
            this,
            parentModalId,
            texts,
            this@showAttachOrUpsertSepaMandateModal,
            storage,
            device,
            bankAccount,
            collections,
            usersSepaMandates,
            mode,
            setMode,
            sepaMandate,
            setSepaMandate,
            update = update
        )
    ))
}


val upsertSepaMandateModalTexts = Source {
    "dialog" texts {
        "title" colon "Attach, Create or Update SEPA mandate"
        "okButton" block {
            "title" colon "Ok"
        }
        "cancelButton" block {
            "title" colon "Cancel"
        }
        add(updateSepaMandateFormTexts.emit())
    }
}
