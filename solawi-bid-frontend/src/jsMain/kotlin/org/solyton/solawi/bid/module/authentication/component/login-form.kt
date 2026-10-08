package org.solyton.solawi.bid.module.authentication.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.dataId
import org.evoleq.compose.form.label.Label
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.language.Lang
import org.evoleq.language.component
import org.evoleq.language.get
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.PasswordInput
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.authentication.data.*
import org.solyton.solawi.bid.module.control.button.SubmitButton
import org.solyton.solawi.bid.module.style.card.cardStyle
import org.solyton.solawi.bid.module.style.form.fieldStyle
import org.solyton.solawi.bid.module.style.form.formControlBarStyle
import org.solyton.solawi.bid.module.style.form.formLabelStyle
import org.solyton.solawi.bid.module.style.form.textInputStyle

@Markup
@Composable
@Suppress("FunctionName")
fun LoginForm(storage: Storage<LoginForm>, login: ()->Unit) {
    val userData = storage * user
    val texts = (storage * texts).read() as Lang.Block
    val loginFields = texts.component("solyton.authentication.login.fields")
    val device = (storage * deviceType).read()

    Div(attrs = {
        style {
            cardStyle()
            maxWidth(400.px)
            width(100.percent)
            boxSizing("border-box")
            padding(if (device > DeviceType.Tablet) 32.px else 20.px)
            borderRadius(12.px)
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
        }
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

        Div(attrs = {style { formControlBarStyle(device)() }}) {
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
