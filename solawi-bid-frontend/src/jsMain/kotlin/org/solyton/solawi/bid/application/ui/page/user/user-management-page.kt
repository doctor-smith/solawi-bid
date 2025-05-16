package org.solyton.solawi.bid.application.ui.page.user

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Space
import org.evoleq.compose.layout.Vertical
import org.evoleq.language.component
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.math.Reader
import org.evoleq.math.emit
import org.evoleq.math.on
import org.evoleq.math.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
//import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.device.mediaType
import org.solyton.solawi.bid.application.data.i18N
import org.solyton.solawi.bid.application.effect.trigger
import org.solyton.solawi.bid.application.permission.Right
import org.solyton.solawi.bid.application.service.useI18nTransform
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.user.action.createUser
import org.solyton.solawi.bid.application.ui.page.user.action.getUsers
import org.solyton.solawi.bid.application.ui.page.user.effect.trigger
import org.solyton.solawi.bid.application.ui.page.user.i18n.UserLangComponent
import org.solyton.solawi.bid.application.ui.style.page.verticalPageStyle
import org.solyton.solawi.bid.application.ui.style.wrap.Wrap
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.error.component.showErrorModal
import org.solyton.solawi.bid.module.error.lang.errorModalTexts
import org.solyton.solawi.bid.module.i18n.data.componentLoaded
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.user.data.api.CreateUser
import org.solyton.solawi.bid.module.permissions.service.isNotGranted
import org.solyton.solawi.bid.module.user.component.modal.showCreateUserModal
import org.solyton.solawi.bid.module.user.data.*

@Markup
@Composable
@Suppress("FunctionName")
fun UserManagementPage(storage: Storage<Application>) = Div {

    // Data
    val environment = storage * environment

    // Data / I18N
    val texts = storage * i18n * language * component(UserLangComponent.UserManagementPage)
    val buttons = texts * subComp("buttons")
    val dialogs = texts * subComp("dialogs")
    val registeredUsers = texts * subComp("registeredUsers")

    // Effect
    LaunchComponentLookup(
        langComponent = UserLangComponent.UserManagementPage,
        environment = Reader{ environment.read() },
        i18n = (storage * i18n)
    )

    LaunchedEffect(Unit) {
        launch {
            val action = getUsers()
            trigger(action) on storage
        }
    }

    // State
    var useR by remember { mutableStateOf(CreateUser("", "")) }
    val loaded = (storage * i18n * componentLoaded(UserLangComponent.UserManagementPage)).emit()
    if(!loaded) return@Div
    // Markup
    Vertical(verticalPageStyle) {
        Wrap {
            Horizontal(styles = { justifyContent(JustifyContent.SpaceBetween); width(100.percent) }) {
                H1 { Text((texts * title).emit()) }
                Horizontal {
                    StdButton(
                        buttons * subComp("createUser") * title,
                        (storage * deviceData * mediaType.get),
                        (storage * user.get ).emit().isNotGranted(Right.Application.Users.manage)
                    ) {
                        (storage * modals).showCreateUserModal(
                            texts = dialogs * subComp("createUser"),
                            device = storage * deviceData * mediaType.get,
                            setUserData = {username, password -> useR = CreateUser(username, password) },
                            cancel = {}
                        ) {
                            CoroutineScope(Job()).launch {
                                val action = createUser(useR)
                                trigger(action) on storage
                            }
                        }
                    }
                }
            }
        }
        Wrap {
            H2{Text((registeredUsers * title).emit())}
            (storage * managedUsers).read().forEach { user ->
                Wrap({marginTop(10.px)}){ Horizontal {

                    P { Text(user.username) }
                    Space()
                    Horizontal {
                        StdButton(
                            registeredUsers * subComp("buttons") * subComp("edit") * title,
                            storage * deviceData * mediaType.get,
                            true
                        ){}
                        StdButton(
                            registeredUsers * subComp("buttons") * subComp("delete") * title,
                            storage * deviceData * mediaType.get,
                            true
                        ){}
                    }
                } }
            }
        }
    }
}