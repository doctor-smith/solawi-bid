package org.solyton.solawi.bid.module.banking.component.form.sepa

import androidx.compose.runtime.*
import org.evoleq.compose.conditional.When
import org.evoleq.compose.date.format
import org.evoleq.compose.date.parse
import org.evoleq.compose.form.Form
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
import org.evoleq.compose.layout.Horizontal
import org.evoleq.language.Lang
import org.evoleq.language.Locale
import org.evoleq.language.texts
import org.evoleq.math.Source
import org.evoleq.math.times
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.required
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.application.i18n.inputs
import org.solyton.solawi.bid.module.banking.data.sepa.payment.SepaPayment
import org.solyton.solawi.bid.module.banking.i18n.executionDate
import org.solyton.solawi.bid.module.banking.i18n.label
import org.solyton.solawi.bid.module.banking.i18n.title
import org.solyton.solawi.bid.module.banking.i18n.totalAmount
import org.solyton.solawi.bid.module.control.checkbox.CheckBox
import org.solyton.solawi.bid.module.style.form.dateInputDesktopStyle
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle


@Composable
fun BulkUpdateSepaPaymentsForm(
    texts: Source<Lang.Block> = bulkUpdateSepaPaymentsFormTexts,
    sepaPayments: List<SepaPayment>,
    setSepaPayments: (List<SepaPayment>) -> Unit,
) = Form(
    formDesktopStyle
) {
    When(sepaPayments.isEmpty()) {

    }
    When(sepaPayments.isNotEmpty()) {

        var bulkUpdateAmountState by remember { mutableStateOf(false) }
        var bulkUpdateExecutionDateState by remember{mutableStateOf(false)}

        var amountState by remember { mutableStateOf(sepaPayments.first().amount) }
        var executionDateState by remember { mutableStateOf(sepaPayments.first().executionDate) }
        // Fields:
        // amount,
        // execution date: arbitrary date, but with hints on taken dates
        // seq type
        // status
        // Failure reason

        val formInputs = texts * inputs

        Field(fieldDesktopStyle) {
            Horizontal({
                alignSelf(AlignSelf.Start)
                gap(5.px)
            }) {
                CheckBox(
                    bulkUpdateAmountState
                ) {
                    bulkUpdateAmountState = it
                }
                Label(
                    text = formInputs * totalAmount * label * title,
                    id = "label.total-amount-of-payment",
                    labelStyle = formLabelDesktopStyle,
                    isRequired = bulkUpdateAmountState
                )
            }
            TextInput(amountState.toString()) {
                if(bulkUpdateAmountState) required()
                if(!bulkUpdateAmountState) disabled()
                style {
                    width(100.percent)
                }
                id("input.total-amount-of-payment")
                onInput { event ->
                    amountState = event.value.toDoubleOrNull() ?: 0.0
                    setSepaPayments(sepaPayments.map{it.copy(amount = amountState)})
                }
            }
        }
        Field(fieldDesktopStyle) {
            Horizontal({
                alignSelf(AlignSelf.Start)
                gap(5.px)
            }) {
                CheckBox(
                    bulkUpdateExecutionDateState
                ) {
                    bulkUpdateExecutionDateState = it
                }
                Label(
                    text = formInputs * executionDate * label * title,
                    id = "label.execution-date",
                    labelStyle = formLabelDesktopStyle,
                    isRequired = bulkUpdateExecutionDateState
                )
            }
            key(executionDateState) {
                Input(InputType.Date) {
                    if(!bulkUpdateExecutionDateState) disabled()
                    id("input.execution-date")
                    // dataId("sepa.form.input.date.start")
                    value((executionDateState).format(Locale.Iso))
                    style {
                        alignSelf(AlignSelf.Start)
                        dateInputDesktopStyle()
                    }
                    onInput { event ->
                        executionDateState = event.value.parse(Locale.Iso)
                        setSepaPayments(sepaPayments.map{it.copy(executionDate = executionDateState) })
                    }
                }
            }
        }
    }

}


val bulkUpdateSepaPaymentsFormTexts by lazy {
    Source {
        "bulkUpdateSepaPaymentsForm" texts {
            "inputs" block {
                "totalAmount" block {
                    "label" block {
                        "title" colon "Total amount of payment"
                    }
                }
                "executionDate" block {
                    "label" block {
                        "title" colon "Execution date"
                    }
                }
                "seqType" block {
                    "label" block {
                        "title" colon "Seq type"
                    }
                }
                "status" block {
                    "label" block {
                        "title" colon "Status"
                    }
                }
                "failureReason" block {
                    "label" block {
                        "title" colon "Failure reason"
                    }
                }
            }
        }
    }
}
