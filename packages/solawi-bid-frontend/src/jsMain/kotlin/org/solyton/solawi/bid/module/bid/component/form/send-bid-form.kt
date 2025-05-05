package org.solyton.solawi.bid.module.bid.component.form

import androidx.compose.runtime.*
import io.ktor.util.*
import org.evoleq.compose.Markup
import org.evoleq.compose.label.Label
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.application.data.device.DeviceType
import org.solyton.solawi.bid.application.ui.style.form.*
import org.solyton.solawi.bid.module.bid.data.Bid
import org.solyton.solawi.bid.module.bid.service.isDecimal
import org.solyton.solawi.bid.module.bid.service.toDecimal
import org.solyton.solawi.bid.module.control.button.SubmitButton

@Markup
@Composable
@Suppress("FunctionName")
fun SendBidForm(device: DeviceType, sendBid: (Bid)->Unit)  {

    Div(attrs = {
        style { formStyle(device)() }
    }) {
        // State
        var email by remember { mutableStateOf("") }
        var emailValid by remember { mutableStateOf(true) }
        var amount by remember { mutableStateOf("0,00") }
        var amountValid by remember { mutableStateOf(true) }

        Div(attrs = { style { fieldStyle(device)() } }) {
            // todo:i18n
            Label("Email Adresse", id = "email", labelStyle = formLabelStyle(device))
            TextInput(email) {
                id("email")
                style { textInputStyle(device)() }
                onInput {
                    email = it.value
                    emailValid = it.value.isEmail()
                }
            }
            // todo:i18n
            P(attrs={style{color(if(!emailValid){Color.crimson}else{Color.transparent})}}){Text("Keine valide Email Adresse")}
        }
        Div(attrs = { style { fieldStyle(device)() } }) {

            // todo:i18n
            Label("Betrag (pro Anteil)", id = "amount", labelStyle = formLabelStyle(device))
            TextInput(amount) {
                id("amount")
                style { textInputStyle(device)() }
                onInput {
                    amount = it.value
                    amountValid = it.value.isDecimal(2)
                }
            }
            // todo:i18n
            P(attrs={style{color(if(!amountValid){Color.crimson}else{Color.transparent})}}){Text("Kein valider Betrag")}
            Div(attrs = { style { formControlBarStyle(device)() } }) {
                SubmitButton(
                    {"Gebot senden"},
                    device,
                    !(amountValid && emailValid)
                ) {
                    sendBid(
                        Bid(
                            email.toLowerCasePreservingASCIIRules(),
                            amount.toDecimal()
                        )
                    )
                }
            }

        }
    }
}

fun String.isEmail(): Boolean {
    val regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
    return regex.matches(this)
}
