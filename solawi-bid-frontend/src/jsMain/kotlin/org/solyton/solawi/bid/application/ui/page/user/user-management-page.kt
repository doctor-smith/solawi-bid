package org.solyton.solawi.bid.application.ui.page.user

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Space
import org.evoleq.compose.layout.Vertical
import org.evoleq.language.Lang
import org.evoleq.language.component
import org.evoleq.math.emit
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.device.mediaType
import org.solyton.solawi.bid.application.permission.Right
import org.solyton.solawi.bid.application.ui.page.user.action.createUser
import org.solyton.solawi.bid.application.ui.page.user.action.getUsers
import org.solyton.solawi.bid.application.ui.style.page.verticalPageStyle
import org.solyton.solawi.bid.application.ui.style.wrap.Wrap
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.error.component.showErrorModal
import org.solyton.solawi.bid.module.error.lang.errorModalTexts
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.user.data.api.CreateUser
import org.solyton.solawi.bid.module.user.isNotGranted
import org.solyton.solawi.bid.module.user.modal.showCreateUserModal

@Markup
@Composable
@Suppress("FunctionName")
fun UserManagementPage(storage: Storage<Application>) = Div {

    var user by remember { mutableStateOf(CreateUser("", "")) }

    LaunchedEffect(Unit) {
        launch {

            val action = getUsers()
            val actions = (storage * actions).read()
            try {
                actions.emit( action )
            } catch(exception: Exception) {
                (storage * modals).showErrorModal(
                    errorModalTexts(exception.message?:exception.cause?.message?:"Cannot Emit action '${action.name}'"),
                    storage * deviceData * mediaType.get
                )
            }
        }
    }


    // Markup
    Vertical(verticalPageStyle) {
        Wrap {
            Horizontal(styles = { justifyContent(JustifyContent.SpaceBetween); width(100.percent) }) {
                // todo:i18n
                H1 { Text("User Management") }
                Horizontal {
                    StdButton(
                        {"Nutzer erstellen"},
                        (storage * deviceData * mediaType.get),
                        (storage * userData.get ).emit().isNotGranted(Right.Application.Users.manage)
                    ) {
                        (storage * modals).showCreateUserModal(
                            texts = ((storage * i18N * language).read() as Lang.Block).component("solyton.auction.updateDialog"),
                            device = storage * deviceData * mediaType.get,
                            setUserData = {username, password -> user = CreateUser(username, password) },
                            cancel = {}
                        ) {
                            CoroutineScope(Job()).launch {
                                val action = createUser(user)
                                val actions = (storage * actions).read()
                                try {
                                    actions.emit( action )
                                } catch(exception: Exception) {
                                    (storage * modals).showErrorModal(
                                        errorModalTexts(exception.message?:exception.cause?.message?:"Cannot Emit action '${action.name}'"),
                                        storage * deviceData * mediaType.get
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Wrap {
            H2{Text("Registrierte Nutzer")}
            (storage * managedUsers).read().forEach { user ->
                Wrap({marginTop(10.px)}){ Horizontal {

                    P { Text(user.username) }
                    Space()
                    Horizontal {
                        StdButton(
                            {"Edit"},
                            storage * deviceData * mediaType.get,
                            true
                        ){}
                        StdButton(
                            {"Delete"},
                            storage * deviceData * mediaType.get,
                            true
                        ){}
                    }
                } }
            }
        }
    }
}