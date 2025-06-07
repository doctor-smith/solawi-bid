package org.solyton.solawi.bid.module.user.component.modal


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
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.PasswordInput
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.device.DeviceType
import org.solyton.solawi.bid.application.ui.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.application.ui.style.form.formDesktopStyle
import org.solyton.solawi.bid.application.ui.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.application.ui.style.form.textInputDesktopStyle
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.user.data.reader.*
import org.solyton.solawi.bid.module.user.service.PasswordCombinationCheck
import org.solyton.solawi.bid.module.user.service.onPasswordCombinationValid
import org.w3c.dom.HTMLElement

@Markup
@Suppress("FunctionName")
fun ChangePasswordModal(
    id: Int,
    texts: Source<Lang.Block>,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    storedPassword: String,
    setUserData: (password: String) -> Unit,
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
    var oldPasswordState by remember{ mutableStateOf("") }
    var newPasswordState by remember { mutableStateOf("") }
    var newPasswordRepeatState by remember { mutableStateOf("") }
    var passwordCombinationCheckState by remember { mutableStateOf<PasswordCombinationCheck>(PasswordCombinationCheck.Empty) }

    val inputs = texts * inputs

    Vertical {
        Div(attrs = { style { formDesktopStyle() } }) {
            Div(attrs = { style { fieldDesktopStyle() } }) {
                Label((inputs * oldPassword * title).emit(), id = "oldPassword", labelStyle = formLabelDesktopStyle)
                PasswordInput(oldPasswordState) {
                    id("oldPassword")
                    style { textInputDesktopStyle() }
                    onInput {
                        oldPasswordState = it.value
                        passwordCombinationCheckState = onPasswordCombinationValid(
                            value = newPasswordState,
                            storedPassword,
                            oldPasswordState,
                            newPasswordState,
                            newPasswordRepeatState
                        ) {
                                pw : String -> setUserData(pw)
                        }
                    }
                }
            }

            Div(attrs = { style { fieldDesktopStyle() } }) {
                Label((inputs * newPassword * title).emit(), id = "password", labelStyle = formLabelDesktopStyle)
                PasswordInput(newPasswordState) {
                    id("password")
                    style { textInputDesktopStyle() }
                    onInput {
                        newPasswordState = it.value
                        passwordCombinationCheckState = onPasswordCombinationValid(
                            value = newPasswordState,
                            storedPassword,
                            oldPasswordState,
                            newPasswordState,
                            newPasswordRepeatState
                        ) {
                                pw : String -> setUserData(pw)
                        }
                    }
                }
            }
            Div(attrs = { style { fieldDesktopStyle() } }) {
                Label((inputs * repeatPassword * title).emit(), id = "repeat-password", labelStyle = formLabelDesktopStyle)
                PasswordInput(newPasswordRepeatState) {
                    id("repeat-password")
                    style { textInputDesktopStyle() }
                    onInput {
                        newPasswordRepeatState = it.value
                        passwordCombinationCheckState = onPasswordCombinationValid(
                            value = newPasswordState,
                            storedPassword,
                            oldPasswordState,
                            newPasswordState,
                            newPasswordRepeatState
                        ) {
                            pw : String -> setUserData(pw)
                        }
                    }
                }
            }

            val  message: String? = messageFrom(
                passwordCombinationCheckState,
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
fun Storage<Modals<Int>>.showChangePasswordModal(
    texts: Source<Lang.Block>,
    device: Source<DeviceType>,
    storedPassword: String,
    setUserData: (password: String) -> Unit,
    cancel: ()->Unit,
    update: ()->Unit,
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        ChangePasswordModal(
            this,
            texts,
            this@showChangePasswordModal,
            device,
            storedPassword,
            setUserData,
            cancel,
            update,
        )
    )
    )
}
