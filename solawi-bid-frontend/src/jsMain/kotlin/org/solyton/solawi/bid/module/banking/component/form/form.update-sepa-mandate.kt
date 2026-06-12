package org.solyton.solawi.bid.module.banking.component.form

import androidx.compose.runtime.*
import org.evoleq.compose.attribute.disabled
import org.evoleq.compose.form.Form
import org.evoleq.compose.form.field.Field
import org.evoleq.language.Lang
import org.evoleq.language.texts
import org.evoleq.math.Source
import org.evoleq.math.times
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.application.i18n.inputs
import org.solyton.solawi.bid.module.banking.data.MandateReference
import org.solyton.solawi.bid.module.banking.data.sepa.mandate.SepaMandate
import org.solyton.solawi.bid.module.banking.data.sepa.mandate.debtorName
import org.solyton.solawi.bid.module.banking.data.sepa.mandate.mandateReference
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle

@Composable
fun UpdateSepaMandateForm(
    texts: Source<Lang.Block> = updateSepaMandateFormTexts,
    sepaMandate: SepaMandate,
    setSepaMandate: (SepaMandate) -> Unit,
) = Form(formDesktopStyle){
    val inputs = texts * inputs
    // Debtor name
    Field(fieldDesktopStyle) {
        var state by remember { mutableStateOf(sepaMandate.debtorName) }
        TextInput(state) {
            disabled()
            onChange { event ->
                state = event.value
                setSepaMandate(sepaMandate.debtorName { state })
            }
        }
    }
    // Mandate Reference
    Field(fieldDesktopStyle) {
        var state by remember { mutableStateOf(sepaMandate.mandateReference.value) }
        TextInput(state) {
            disabled()
            onChange { event ->
                state = event.value
                setSepaMandate(sepaMandate.mandateReference{MandateReference(state)})
            }
        }
    }
    //
}

val updateSepaMandateFormTexts = Source{ texts {
    "inputs" block {
        "debtorName" block {
            "label" block{
                "title" colon "DebtorName"
            }
        }
        "mandateReference" block {
            "label" block {
                "title" colon "MandateReference"
            }
        }
        "dateSigned" block {
            "label" block {
                "title" colon "Date Signed"
            }
        }
        "status" block {
            "label" block {
                "title" colon "Status"
            }
        }
    }
}}