package org.solyton.solawi.bid.module.banking.component.form.sepa

import androidx.compose.runtime.*
import kotlinx.datetime.LocalDate
import org.evoleq.compose.date.format
import org.evoleq.compose.date.parse
import org.evoleq.compose.form.Form
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
import org.evoleq.kotlinx.date.now
import org.evoleq.language.Lang
import org.evoleq.language.Locale
import org.evoleq.language.subComp
import org.evoleq.math.Source
import org.evoleq.math.times
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.required
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.application.i18n.inputs
import org.solyton.solawi.bid.module.banking.component.modal.sepa.mergeSepaMessagesFormTexts
import org.solyton.solawi.bid.module.banking.data.RemittanceInformation
import org.solyton.solawi.bid.module.banking.data.sepa.message.SepaMessage
import org.solyton.solawi.bid.module.banking.i18n.executionDate
import org.solyton.solawi.bid.module.banking.i18n.label
import org.solyton.solawi.bid.module.banking.i18n.title
import org.solyton.solawi.bid.module.style.form.dateInputDesktopStyle
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle


data class MergeSepaMessagesData(
    val sepaMessages: List<SepaMessage>,
    val executionDate: LocalDate?,
    val remittanceInformation: RemittanceInformation?
)

@Composable
fun MergeSepaMessagesForm(
    texts: Source<Lang.Block> = mergeSepaMessagesFormTexts,
    data: MergeSepaMessagesData,
    setData: (MergeSepaMessagesData) -> Unit
) = Form(formDesktopStyle){

    val formInputs = texts * inputs

    var executionDateState by remember{mutableStateOf<LocalDate>(data.executionDate?:now().date)}
    var remittanceInformationState by remember{mutableStateOf<RemittanceInformation>(data.remittanceInformation?:RemittanceInformation(""))}

    Field(fieldDesktopStyle) {
        Label(
            text = formInputs * executionDate * label * title,
            id = "label.execution-date",
            labelStyle = formLabelDesktopStyle,
            isRequired = true
        )
        key(executionDateState) {
            Input(InputType.Date) {
                id("input.execution-date")
                // dataId("sepa.form.input.date.start")
                value((executionDateState).format(Locale.Iso))
                style { dateInputDesktopStyle() }
                onInput {
                    executionDateState = it.value.parse(Locale.Iso)
                    setData(data.copy(
                        executionDate = executionDateState,
                        remittanceInformation = remittanceInformationState
                    ))
                }
            }
        }

        Field(fieldDesktopStyle) {
            Label(
                text = formInputs * subComp("remittanceInformation") * label * title,
                id = "label.remittance-information",
                labelStyle = formLabelDesktopStyle,
                isRequired = true
            )
            TextInput(remittanceInformationState.value) {
                required()
                id("input.remittance-information")
                onInput {
                    remittanceInformationState = RemittanceInformation(it.value)
                    setData(data.copy(
                        executionDate = executionDateState,
                        remittanceInformation = remittanceInformationState
                    ))
                }
            }
        }
    }
}
