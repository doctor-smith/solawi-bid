package org.solyton.solawi.bid.application.ui.page.user

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Space
import org.evoleq.compose.layout.Vertical
import org.evoleq.device.data.mediaType
import org.evoleq.iql.data.Query
import org.evoleq.iql.dsl.query
import org.evoleq.language.component
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.math.*
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.user.effect.trigger
import org.solyton.solawi.bid.application.ui.page.user.i18n.UserLangComponent
import org.solyton.solawi.bid.module.application.permission.AppRight
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.control.button.AnglesLeftButton
import org.solyton.solawi.bid.module.control.button.AnglesRightButton
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.i18n.data.componentLoaded
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.permissions.data.contextId
import org.solyton.solawi.bid.module.permissions.service.contextFromPath
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.action.user.createUser
import org.solyton.solawi.bid.module.user.action.user.userQuery
import org.solyton.solawi.bid.module.user.component.modal.showCreateUserModal
import org.solyton.solawi.bid.module.user.data.*
import org.solyton.solawi.bid.module.user.data.api.CreateUser
import org.solyton.solawi.bid.module.user.data.api.UserStatus
import org.solyton.solawi.bid.module.user.data.reader.isNotGranted

@Markup
@Composable
@Suppress("FunctionName", "CognitiveComplexMethod")
fun UserManagementPage(storage: Storage<Application>) = Div {

    val scope = rememberCoroutineScope()

    // Data
    val environment = storage * environment
    val applicationContextId = storage * availablePermissions * contextFromPath("APPLICATION") * assureValue() * contextId.get
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

    var pageSizeState by remember { mutableStateOf(20) }
    var pageOffsetState by remember { mutableStateOf(0L) }
    var queryState by remember(
        pageSizeState,
        pageOffsetState
    ) {
           mutableStateOf<Query>(
            query{

                select("user")
                /*
                where {
                    any("userProfiles") {
                        p("UserProfile.firstName") eq "Florian"
                    }
                }

                 */
                asc("user.username")
                page(
                    pageSizeState,
                    pageOffsetState
                )
            }
        )
    }

    LaunchedEffect(queryState) {
        launch {
            val action = userQuery(
                queryState
            )
            trigger(action) on storage
        }
    }



    // State
    var useR by remember { mutableStateOf<CreateUser?>(null) }
    val loaded = (storage * i18n * componentLoaded(UserLangComponent.UserManagementPage)).emit()
    if(!loaded) return@Div
    // Markup
    Vertical(verticalPageStyle) {
        Wrap {
            Horizontal(styles = { justifyContent(JustifyContent.SpaceBetween); width(100.percent) }) {
                H1 { Text((texts * title).emit()) }
                Horizontal {
                    AnglesLeftButton(
                        color = Color.black,
                        bgColor = Color.white,
                        texts = { "Previous $pageSizeState users" },
                        deviceType = storage * deviceData * mediaType.get,
                    ) {
                        val newOffset = pageOffsetState - pageSizeState

                        pageOffsetState = when{
                            newOffset > 0 -> newOffset
                            else -> 0
                        }
                    }
                    AnglesRightButton(
                        color = Color.black,
                        bgColor = Color.white,
                        texts = {"Next $pageSizeState users"},
                        deviceType = storage * deviceData * mediaType.get,
                    ) {
                        pageOffsetState += pageSizeState
                    }
                    StdButton(
                        buttons * subComp("createUser") * title,
                        (storage * deviceData * mediaType.get),
                        // (storage * context * current).read()
                        (storage * isNotGranted(AppRight.Application.Users.manage, applicationContextId)).emit()
                    ) {
                        (storage * modals).showCreateUserModal(
                            texts = dialogs * subComp("createUser"),
                            device = storage * deviceData * mediaType.get,
                            styles = {dev -> auctionModalStyles(dev) },
                            isOkButtonDisabled = { useR == null },
                            setUserData = {username, password ->
                                try {
                                    useR = CreateUser(username, password, UserStatus.ACTIVE)
                                } catch (_: Exception) { }
                            },

                            cancel = {}
                        ) {
                            scope.launch {
                                val user = requireNotNull(useR) { "useR is null" }
                                val action = createUser(user)
                                trigger(action) on storage
                            }
                        }
                    }
                }
            }
        }
        Wrap {
            H2{Text((registeredUsers * title).emit())}

            (storage * managedUsers).read().drop(pageOffsetState.toInt()).take(pageSizeState).forEach { user ->
                Wrap({marginTop(10.px)}){ Horizontal {

                    P { Text(user.username) }
                    Space()
                    Horizontal {
                        StdButton(
                            registeredUsers * subComp("buttons") * subComp("edit") * title,
                            storage * deviceData * mediaType.get,
                            (storage * isNotGranted(AppRight.Application.Users.manage, applicationContextId)).emit()
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
