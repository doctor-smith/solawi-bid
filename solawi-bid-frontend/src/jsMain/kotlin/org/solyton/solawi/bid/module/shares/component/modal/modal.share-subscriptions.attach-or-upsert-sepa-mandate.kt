package org.solyton.solawi.bid.module.shares.component.modal



import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
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
import org.evoleq.uuid.NIL_UUID
import org.jetbrains.compose.web.css.*
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
import org.solyton.solawi.bid.module.control.dropdown.DropdownStyles
import org.solyton.solawi.bid.module.control.dropdown.SimpleUpDown
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.w3c.dom.HTMLElement

sealed class AttachOrUpsertSepaMandateModalMode {
    data object Attach : AttachOrUpsertSepaMandateModalMode()
    data object Upsert : AttachOrUpsertSepaMandateModalMode()
}

enum class MandateDialogAction {
    ATTACH_EXISTING,
    EDIT_EXISTING,
    CREATE_NEW
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
    setSepaMandate: (SepaMandate?) -> Unit,
    update: () -> Unit
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
        val options = remember(usersSepaMandates) {
            usersSepaMandates.associateBy { it.mandateReference.value }
        }
        var selectedRef by remember {
            mutableStateOf(sepaMandate?.mandateReference?.value ?: options.keys.firstOrNull())
        }

        val initialAction = when {
            usersSepaMandates.isEmpty() -> MandateDialogAction.CREATE_NEW
            mode is AttachOrUpsertSepaMandateModalMode.Attach -> MandateDialogAction.ATTACH_EXISTING
            sepaMandate != null && sepaMandate.sepaMandateId.value != NIL_UUID -> MandateDialogAction.EDIT_EXISTING
            else -> MandateDialogAction.CREATE_NEW
        }
        var selectedAction by remember { mutableStateOf(initialAction) }

        When(usersSepaMandates.isNotEmpty()) {
            Field(fieldDesktopStyle) {
                Label(
                    text = "Mode",
                    id = "mode",
                    labelStyle = formLabelDesktopStyle,
                    isRequired = false
                )
                Horizontal({ padding(10.px); gap(10.px) }) {
                    RadioInput(selectedAction == MandateDialogAction.ATTACH_EXISTING) {
                        onClick {
                            selectedAction = MandateDialogAction.ATTACH_EXISTING
                            setMode(AttachOrUpsertSepaMandateModalMode.Attach)
                            setSepaMandate(options[selectedRef])
                        }
                    }
                    Text("Attach existing")

                    RadioInput(selectedAction == MandateDialogAction.EDIT_EXISTING) {
                        onClick {
                            selectedAction = MandateDialogAction.EDIT_EXISTING
                            setMode(AttachOrUpsertSepaMandateModalMode.Upsert)
                            setSepaMandate(options[selectedRef])
                        }
                    }
                    Text("Edit existing")

                    RadioInput(selectedAction == MandateDialogAction.CREATE_NEW) {
                        onClick {
                            selectedAction = MandateDialogAction.CREATE_NEW
                            setMode(AttachOrUpsertSepaMandateModalMode.Upsert)
                            setSepaMandate(null)
                        }
                    }
                    Text("Create new")
                }
            }

            When(selectedAction in listOf(MandateDialogAction.ATTACH_EXISTING, MandateDialogAction.EDIT_EXISTING)) {
                Field(fieldDesktopStyle) {
                    Label(
                        text = "Selected SEPA mandate",
                        id = "selected-sepa-mandate",
                        labelStyle = formLabelDesktopStyle,
                        isRequired = true
                    )
                    Dropdown(
                        options = options,
                        selected = selectedRef,
                        styles = DropdownStyles().modifyContainerStyle {
                            width(100.percent)
                        },
                        iconContent = { open -> SimpleUpDown(open) }
                    ) { (_, value) ->
                        selectedRef = value.mandateReference.value
                        setSepaMandate(value)
                    }
                }
            }
        }

        When(usersSepaMandates.isEmpty()) {
            Text("The user has no associated sepa mandates. Please create a new one:")
        }

        val isReadOnly = selectedAction == MandateDialogAction.ATTACH_EXISTING
        val currentMandate = when (selectedAction) {
            MandateDialogAction.ATTACH_EXISTING, MandateDialogAction.EDIT_EXISTING -> options[selectedRef] ?: sepaMandate
            MandateDialogAction.CREATE_NEW -> null
        }

        UpsertSepaMandateForm(
            texts = texts,
            bankAccount = bankAccount,
            collections = collections,
            sepaMandate = currentMandate,
            isReadOnly = isReadOnly,
            setSepaMandate = { updated -> setSepaMandate(updated) }
        )
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
    setSepaMandate: (SepaMandate?) -> Unit,
    update: () -> Unit
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


val attachOrUpsertSepaMandateModalTexts = Source {
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
