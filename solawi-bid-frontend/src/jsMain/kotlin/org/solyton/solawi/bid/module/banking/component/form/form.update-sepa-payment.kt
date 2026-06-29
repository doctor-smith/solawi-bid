package org.solyton.solawi.bid.module.banking.component.form

import androidx.compose.runtime.*
import org.evoleq.compose.form.Form
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
import org.evoleq.language.Lang
import org.evoleq.language.texts
import org.evoleq.math.Source
import org.evoleq.math.times
import org.jetbrains.compose.web.attributes.required
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.application.i18n.inputs
import org.solyton.solawi.bid.module.banking.data.sepa.payment.SepaPayment
import org.solyton.solawi.bid.module.banking.i18n.label
import org.solyton.solawi.bid.module.banking.i18n.title
import org.solyton.solawi.bid.module.banking.i18n.totalAmount
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle

@Composable
fun UpdateSepaPaymentForm(
    texts: Source<Lang.Block> = updateSepaPaymentFormTexts,
    sepaPayment: SepaPayment,
    setSepaPayment: (SepaPayment) -> Unit,
) = Form(
    formDesktopStyle
) {
    var amountState by remember{ mutableStateOf(sepaPayment.amount) }

    // Fields:
    // amount,
    // execution date: arbitrary date, but with hints on taken dates
    // seq type
    // status
    // Failure reason

    val formInputs = texts * inputs

    Field(fieldDesktopStyle) {
        Label(
            text = formInputs * totalAmount * label * title,
            id = "label.total-amount-of-payment",
            labelStyle = formLabelDesktopStyle,
            isRequired = true
        )
        TextInput(amountState.toString()) {
            required()
            id("input.total-amount-of-payment")
            onInput {
                amountState = it.value.toDoubleOrNull() ?: 0.0
                setSepaPayment(sepaPayment.copy(amount = amountState))
            }
        }
    }
}

val updateSepaPaymentFormTexts by lazy {
    Source {
        "updateSepaPaymentForm" texts {
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
