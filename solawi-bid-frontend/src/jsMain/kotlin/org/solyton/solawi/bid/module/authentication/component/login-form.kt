package org.solyton.solawi.bid.module.authentication.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.dataId
import org.evoleq.compose.label.Label
import org.evoleq.language.Lang
import org.evoleq.language.component
import org.evoleq.language.get
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.PasswordInput
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.application.ui.style.form.*
import org.solyton.solawi.bid.module.authentication.data.*
import org.solyton.solawi.bid.module.control.button.SubmitButton

@Markup
@Composable
@Suppress("FunctionName")
fun LoginForm(storage: Storage<LoginForm>, login: ()->Unit) {
    val userData = storage * user
    val texts = (storage * texts).read() as Lang.Block
    val loginFields = texts.component("solyton.authentication.login.fields")
    val device = (storage * deviceType).read()

    Div(attrs = {
        style { formStyle(device)() }
    }) {
        Div(attrs = { style { fieldStyle(device)() } }) {
            Label(loginFields["username"], id = "username", labelStyle = formLabelStyle(device))

            TextInput((userData * username).read()) {
                style { textInputStyle(device)() }
                dataId("login-form.input.username")
                id("username")
                onInput {
                    (userData * username).write(it.value)
                }
            }
        }
        Div(attrs = { style { fieldStyle(device)() } }) {
            Label(loginFields["password"], id = "password", labelStyle = formLabelStyle(device))
            PasswordInput((userData * password).read()) {
                style { textInputStyle(device)() }
                attr("data-id", "login-form.input.password")
                id("password")
                onInput { (userData * password).write(it.value) }
            }
        }

        Div(attrs = {style { formControlBarStyle(device) }}) {
            val buttonTexts = texts.component("solyton.authentication.login.buttons")

            SubmitButton(
                texts = { _-> buttonTexts["ok"]},
                deviceType = device,
                dataId = "login-form.submit-button",
            ) {
                login()
            }
            /* Deactivate for the moment
            todo:dev reactivate
            Button{
                Text("Registrieren")
            }
             */
        }
    }
}

