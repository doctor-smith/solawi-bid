package org.solyton.solawi.bid.module.user.component.form

import androidx.compose.runtime.*
import org.evoleq.compose.form.label.Label
import org.evoleq.compose.layout.Vertical
import org.evoleq.compose.symbols.WarnLi
import org.evoleq.language.Lang
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.language.valueOf
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.dom.*
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.form.textInputDesktopStyle
import org.solyton.solawi.bid.module.user.component.modal.messageFrom
import org.solyton.solawi.bid.module.user.data.api.UpdateUser
import org.solyton.solawi.bid.module.user.data.reader.*
import org.solyton.solawi.bid.module.user.data.user.User
import org.solyton.solawi.bid.module.user.service.PasswordCombinationCheck
import org.solyton.solawi.bid.module.user.service.onPasswordCombinationValid
import org.solyton.solawi.bid.module.values.Password
import org.solyton.solawi.bid.module.values.Username
import org.solyton.solawi.bid.module.user.data.reader.username as usernameReader

@Composable
fun UpdateUserForm(
    texts: Source<Lang.Block>,
    user: User,
    setUser: (user: UpdateUser) -> Unit,
) {
    var username by remember{ mutableStateOf(user.username) }
    var oldPasswordState by remember{ mutableStateOf("") }
    var newPasswordState by remember { mutableStateOf("") }
    var newPasswordRepeatState by remember { mutableStateOf("") }
    var passwordCombinationCheckState by remember { mutableStateOf<PasswordCombinationCheck>(PasswordCombinationCheck.Empty) }

    val inputs = texts * inputs
    val hints = texts * subComp("hints")
    val hintsTitle = hints * title
    val authenticationRequired = hints * valueOf("authenticationRequired")
    val loginCredentialsChanged = hints * valueOf("loginCredentialsChanged")

    Vertical {

        Div(attrs = { style { formDesktopStyle() } }) {
            P { Text(hintsTitle.emit()) }
            Ul(attrs = {
                style {
                    paddingLeft(1.5.em)
                }
            }) {
                WarnLi { Text(authenticationRequired.emit()) }
                WarnLi { Text(loginCredentialsChanged.emit()) }
            }

            Div(attrs = { style { fieldDesktopStyle() } }) {
                Label(
                    text = (inputs * usernameReader * title).emit(),
                    id = "username",
                    labelStyle = formLabelDesktopStyle,
                    isRequired = true
                )
                TextInput(username) {
                    // disabled()
                    id("username")
                    style { textInputDesktopStyle() }
                    onInput {
                        username = it.value
                        setUser( UpdateUser(
                                oldUsername = Username(user.username),
                                newUsername = Username(it.value),
                                oldPassword = Password(oldPasswordState),
                                newPassword = Password(newPasswordState)
                        ) )
                    }
                }
            }

            Div(attrs = { style { fieldDesktopStyle() } }) {
                Label(
                    text = (inputs * oldPassword * title).emit(),
                    id = "oldPassword",
                    labelStyle = formLabelDesktopStyle,
                    isRequired = true
                )
                PasswordInput(oldPasswordState) {
                    id("oldPassword")
                    style { textInputDesktopStyle() }
                    onInput {
                        oldPasswordState = it.value
                        passwordCombinationCheckState = onPasswordCombinationValid(
                            value = oldPasswordState,
                            oldPasswordState,
                            oldPasswordState,
                            newPasswordState,
                            newPasswordRepeatState
                        ) {
                                pw : String -> setUser( UpdateUser(
                                    oldUsername = Username(user.username),
                                    newUsername = Username(username),
                                    oldPassword = Password(pw),
                                    newPassword = Password(newPasswordState)
                                ) )
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
                            oldPasswordState,// storedPassword,
                            oldPasswordState,
                            newPasswordState,
                            newPasswordRepeatState
                        ) {
                                pw : String -> setUser( UpdateUser(
                                    oldUsername = Username(user.username),
                                    newUsername = Username(username),
                                    oldPassword = Password(oldPasswordState),
                                    newPassword = Password(pw)
                                ) )
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
                            oldPasswordState, //storedPassword,
                            oldPasswordState,
                            newPasswordState,
                            newPasswordRepeatState
                        ) {
                                pw : String -> setUser( UpdateUser(
                                    oldUsername = Username(user.username),
                                    newUsername = Username(username),
                                    oldPassword = Password(oldPasswordState),
                                    newPassword = Password(newPasswordState)
                                ) )
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
