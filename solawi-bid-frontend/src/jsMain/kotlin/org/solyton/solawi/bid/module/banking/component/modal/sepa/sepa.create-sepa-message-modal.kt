package org.solyton.solawi.bid.module.banking.component.modal.sepa


import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.disabled
import org.evoleq.compose.conditional.When
import org.evoleq.compose.form.Form
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.marginLeft
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.banking.data.sepa.collection.SepaCollection
import org.solyton.solawi.bid.module.banking.data.sepa.message.SepaMessage
import org.solyton.solawi.bid.module.control.checkbox.CheckBox
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.form.textInputDesktopStyle
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.w3c.dom.HTMLElement

data class SepaMessageModalData(
    val sepaCollection: SepaCollection,
    val messages: List<SepaMessage>,
    val remittanceInformation: String? = null
)

@Markup
@Suppress("FunctionName")
fun UpsertSepaMessageModal(
    id: Int,
    parentModalId: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    data: SepaMessageModalData,
    setData: (SepaMessageModalData) -> Unit,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    type = ModalType.Child<Int>(parentModalId),
    id = id,
    modals = modals,
    device = device,
    onOk = {
        update()
    },
    onCancel = {},
    texts = texts,
    styles = commonModalStyles(device).modifyContainerStyle {
        width(80.percent)
        marginLeft(5.percent)
    },
) {
    var dataState by remember { mutableStateOf(data) }
    var useDefault by remember { mutableStateOf(true) }
    var updateExistingMessage by remember { mutableStateOf(false) }


    Wrap {
        Form(formDesktopStyle) {
            Field(fieldDesktopStyle) {
                Horizontal({
                    JustifyContent.FlexStart
                }) {
                    CheckBox(
                        checked = updateExistingMessage,
                        onClick = { isChecked ->
                            updateExistingMessage = isChecked
                        }
                    )
                    Label("Update Existing Message", id = "update-existing-message", labelStyle = formLabelDesktopStyle)
                }
            }
        }

        When(updateExistingMessage) {
            Form(formDesktopStyle) {
                Field(fieldDesktopStyle) {
                    Text("TOTO - Update Existing Message - A list of messages will be displayed here. So that the user can select the message to update")

                }

            }
        }
        When(!updateExistingMessage) {


            Form(formDesktopStyle) {
                Horizontal({JustifyContent.FlexStart}) {
                    Field(fieldDesktopStyle) {
                        Horizontal {
                            CheckBox(
                                checked = useDefault,
                                onClick = { isChecked ->
                                    useDefault = isChecked
                                    if (isChecked) {
                                        dataState = dataState.copy(remittanceInformation = null)
                                        setData(dataState)
                                    } else {
                                        dataState =
                                            dataState.copy(remittanceInformation = data.sepaCollection.remittanceInformation.value)
                                        setData(dataState)
                                    }
                                }
                            )
                            Label("Use Default", id = "use-default", labelStyle = formLabelDesktopStyle)
                        }
                    }
                    Field(fieldDesktopStyle) {
                        Label(
                            "Remittance Information",
                            id = "remittance-information",
                            labelStyle = formLabelDesktopStyle
                        )
                        TextInput(dataState.remittanceInformation ?: "") {
                            placeholder(data.sepaCollection.remittanceInformation.value)
                            if (useDefault) disabled()
                            id("remittance-information")
                            style { textInputDesktopStyle() }
                            onInput {
                                dataState = dataState.copy(remittanceInformation = it.value)
                                setData(dataState)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Markup
fun Storage<Modals<Int>>.showUpsertSepaMessageModal(
    parentModalId: Int,
    texts: Lang.Block,
    device: Source<DeviceType>,
    data: SepaMessageModalData,
    setData: (SepaMessageModalData) -> Unit,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(this,
        ModalType.Child<Int>(parentModalId),
        UpsertSepaMessageModal(
            id = this,
            parentModalId = parentModalId,
            texts = texts,
            modals = this@showUpsertSepaMessageModal,
            device = device,
            data = data,
            setData = setData,
            update = update
        )
    ) )
}
