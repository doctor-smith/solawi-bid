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
import org.evoleq.language.title
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.dom.*
import org.solyton.solawi.bid.application.data.device.DeviceType
import org.solyton.solawi.bid.application.ui.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.application.ui.style.form.formDesktopStyle
import org.solyton.solawi.bid.application.ui.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.application.ui.style.form.textInputDesktopStyle
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.user.data.reader.errors
import org.solyton.solawi.bid.module.user.data.reader.inputs
import org.solyton.solawi.bid.module.user.data.reader.repeatPassword
import org.solyton.solawi.bid.module.user.service.PasswordCombinationCheck
import org.solyton.solawi.bid.module.user.service.onPasswordCombinationValid
import org.w3c.dom.HTMLElement
import org.solyton.solawi.bid.module.user.data.reader.password as passwordReader
import org.solyton.solawi.bid.module.user.data.reader.username as usernameReader

@Markup
@Suppress("FunctionName")
fun CreateUserModal(
    id: Int,
    texts: Source<Lang.Block>,
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
    texts = texts.emit(),
    styles = auctionModalStyles(device),
) {
    var username by remember{ mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordRepeat by remember { mutableStateOf("") }
    var passwordCombinationCheck by remember { mutableStateOf<PasswordCombinationCheck>(PasswordCombinationCheck.Empty) }

    val inputs = texts * inputs

    Vertical {
        Div(attrs = { style { formDesktopStyle() } }) {
            Div(attrs = { style { fieldDesktopStyle() } }) {
                Label((inputs * usernameReader * title).emit(), id = "username", labelStyle = formLabelDesktopStyle)
                TextInput(username) {
                    id("username")
                    style { textInputDesktopStyle() }
                    onInput {
                        username = it.value
                        passwordCombinationCheck = onPasswordCombinationValid(
                            value = password,
                            null,
                            null,
                            password,
                            passwordRepeat
                        ) {
                                pw : String -> setUserData(username, pw)
                        }
                    }
                }
            }

            Div(attrs = { style { fieldDesktopStyle() } }) {
                Label((inputs * passwordReader * title).emit(), id = "password", labelStyle = formLabelDesktopStyle)
                PasswordInput(password) {
                    id("password")
                    style { textInputDesktopStyle() }
                    onInput {
                        password = it.value
                        if(password.isNotBlank() && password == passwordRepeat) {
                            setUserData(username, password)
                        }
                        passwordCombinationCheck = onPasswordCombinationValid(
                            value = password,
                            null,
                            null,
                            password,
                            passwordRepeat
                        ) {
                                pw : String -> setUserData(username, pw)
                        }

                    }
                }
            }
            Div(attrs = { style { fieldDesktopStyle() } }) {
                Label((inputs * repeatPassword * title).emit(), id = "repeat-password", labelStyle = formLabelDesktopStyle)
                PasswordInput(passwordRepeat) {
                    id("repeat-password")
                    style { textInputDesktopStyle() }
                    onInput {
                        passwordRepeat = it.value
                        passwordCombinationCheck = onPasswordCombinationValid(
                            value = password,
                            null,
                            null,
                            password,
                            passwordRepeat
                        ) {
                                pw : String -> setUserData(username, pw)
                        }
                    }
                }
            }

            val  message: String? = messageFrom(
                passwordCombinationCheck,
                texts * errors
            )

            if(message != null) {
                Div({ style { color(Color.crimson) } }){
                    Text(message)
                }
            }

        }
    }
}

@Markup
fun Storage<Modals<Int>>.showCreateUserModal(
    texts: Source<Lang.Block>,
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
