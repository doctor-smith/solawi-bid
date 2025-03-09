package org.solyton.solawi.bid.module.user.modal

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.label.Label
import org.evoleq.compose.layout.Vertical
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.PasswordInput
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.application.data.device.DeviceType
import org.solyton.solawi.bid.application.ui.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.application.ui.style.form.formDesktopStyle
import org.solyton.solawi.bid.application.ui.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.application.ui.style.form.textInputDesktopStyle
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.w3c.dom.HTMLElement

@Markup
@Suppress("FunctionName")
fun CreateUserModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    setUserData: (username: String, password: String) -> Unit,
    cancel: ()->Unit,
    update: ()->Unit,
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    id = id,
    modals = modals,
    device = device,

    onOk = {
        update()
    },
    onCancel = {
        cancel()
    },
    texts = texts,
    styles = auctionModalStyles(device),
) {
    var username by remember{ mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordRepeat by remember { mutableStateOf("") }


    Vertical {
        Div(attrs = { style { formDesktopStyle() } }) {
            Div(attrs = { style { fieldDesktopStyle() } }) {
                Label("Username", id = "username", labelStyle = formLabelDesktopStyle)
                TextInput(username) {
                    id("username")
                    style { textInputDesktopStyle() }
                    onInput {
                        username = it.value
                        if(password.isNotBlank() && password == passwordRepeat) {
                            setUserData(username, password)
                        }
                    }
                }
            }

            Div(attrs = { style { fieldDesktopStyle() } }) {
                Label("Passwort", id = "password", labelStyle = formLabelDesktopStyle)
                PasswordInput(password) {
                    id("password")
                    style { textInputDesktopStyle() }
                    onInput {
                        password = it.value
                        if(password.isNotBlank() && password == passwordRepeat) {
                            setUserData(username, password)
                        }
                    }
                }
            }
            Div(attrs = { style { fieldDesktopStyle() } }) {
                Label("Passwort wiederholen", id = "repeat-password", labelStyle = formLabelDesktopStyle)
                PasswordInput(passwordRepeat) {
                    id("repeat-password")
                    style { textInputDesktopStyle() }
                    onInput {
                        passwordRepeat = it.value
                        if(password.isNotBlank() && password == passwordRepeat) {
                            setUserData(username, password)
                        }
                    }
                }
            }

        }
    }
    /*
    // input texts
    val inputs: Lang.Block = texts.component("inputs")

    Div(attrs = {style { org.solyton.solawi.bid.application.ui.style.form.formDesktopStyle() }}) {

        Div(attrs = {style { org.solyton.solawi.bid.application.ui.style.form.fieldDesktopStyle() }}) {
            Label(inputs["title"], id = "name" , labelStyle = formLabelDesktopStyle)
            TextInput((auction * name).read()) {
                id("name")
                style { org.solyton.solawi.bid.application.ui.style.form.textInputDesktopStyle() }
                onInput { (auction * name).write(it.value) }
            }
        }
        Div(attrs = {style { org.solyton.solawi.bid.application.ui.style.form.fieldDesktopStyle() }}) {
            // State
            val initDate = (auction * date).read().format(Locale.Iso)
            var dateString by remember{ mutableStateOf( initDate ) }

            Label(inputs["date"], id = "date" , labelStyle = formLabelDesktopStyle)
            Input(InputType.Date) {
                id("date")
                value(dateString)
                style { org.solyton.solawi.bid.application.ui.style.form.dateInputDesktopStyle() }
                onInput {
                    dateString = it.value
                    (auction * date).write(it.value.parse(Locale.Iso))
                }
            }
        }

        Div(attrs = {style { org.solyton.solawi.bid.application.ui.style.form.fieldDesktopStyle() }}) {
            Label(inputs["benchmark"], id = "benchmark" , labelStyle = formLabelDesktopStyle)
            TextInput (onNullEmpty((auction * auctionDetails * benchmark).read()){it}) {
                id("benchmark")
                style { org.solyton.solawi.bid.application.ui.style.form.numberInputDesktopStyle() }
                onInput {
                    onIsDouble(it.value) {
                        (auction * auctionDetails * benchmark).write(toDouble())
                    }
                }
            }
        }

        Div(attrs = {style { org.solyton.solawi.bid.application.ui.style.form.fieldDesktopStyle() }}) {
            Label(inputs["targetAmount"], id = "targetAmount" , labelStyle = formLabelDesktopStyle)
            TextInput(onNullEmpty((auction * auctionDetails * targetAmount).read()){it}) {
                id("targetAmount")
                style { org.solyton.solawi.bid.application.ui.style.form.numberInputDesktopStyle() }
                onInput {
                    onIsDouble(it.value) {
                        (auction * auctionDetails * targetAmount).write(toDouble())
                    }
                }
            }
        }
        Div(attrs = {style { org.solyton.solawi.bid.application.ui.style.form.fieldDesktopStyle() }}) {
            Label(inputs["solidarityContribution"], id = "solidarityContribution" , labelStyle = formLabelDesktopStyle)
            TextInput(onNullEmpty((auction * auctionDetails * solidarityContribution).read()){it}) {
                id("solidarityContribution")
                style { org.solyton.solawi.bid.application.ui.style.form.numberInputDesktopStyle() }
                onInput {
                    onIsDouble(it.value) {
                        (auction * auctionDetails * solidarityContribution).write(toDouble())
                    }
                }
            }
        }

    }

     */
}

@Markup
fun Storage<Modals<Int>>.showCreateUserModal(
    texts: Lang.Block,
    device: Source<DeviceType>,
    setUserData: (username: String, password: String) -> Unit,
    cancel: ()->Unit,
    update: ()->Unit,
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        CreateUserModal(
            this,
            texts,
            this@showCreateUserModal,
            device,

            setUserData,
            cancel,
            update,
        )
    )
    )
}
