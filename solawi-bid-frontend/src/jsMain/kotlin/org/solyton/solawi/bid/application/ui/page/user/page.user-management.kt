package org.solyton.solawi.bid.application.ui.page.user

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.effect.LaunchedEffectOnSource
import org.evoleq.compose.guard.data.isLoading
import org.evoleq.compose.guard.data.withLoading
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Vertical
import org.evoleq.device.data.mediaType
import org.evoleq.iql.dsl.query
import org.evoleq.language.component
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.math.*
import org.evoleq.optics.storage.Read
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.user.data.UiState
import org.solyton.solawi.bid.application.ui.page.user.data.filter
import org.solyton.solawi.bid.application.ui.page.user.data.filter.firstName
import org.solyton.solawi.bid.application.ui.page.user.data.filter.lastName
import org.solyton.solawi.bid.application.ui.page.user.data.filter.status
import org.solyton.solawi.bid.application.ui.page.user.data.filter.username
import org.solyton.solawi.bid.application.ui.page.user.data.sort.lastName
import org.solyton.solawi.bid.application.ui.page.user.data.sort.status
import org.solyton.solawi.bid.application.ui.page.user.data.sort.username
import org.solyton.solawi.bid.application.ui.page.user.data.sortOrder
import org.solyton.solawi.bid.application.ui.page.user.effect.trigger
import org.solyton.solawi.bid.application.ui.page.user.i18n.UserLangComponent
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.application.permission.AppRight
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.control.button.*
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.guard.onMissing
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.ListStyles
import org.solyton.solawi.bid.module.loading.component.Loading
import org.solyton.solawi.bid.module.permissions.data.contextId
import org.solyton.solawi.bid.module.permissions.service.contextFromPath
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.action.user.createUser
import org.solyton.solawi.bid.module.user.action.user.readUserProfiles
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
    withLoading(
        isLoading = isLoading(
            onMissing(
                UserLangComponent.UserManagementPage,
                storage * i18n.get
            )   {
                // Effect
                LaunchComponentLookup(
                    langComponent = UserLangComponent.UserManagementPage,
                    environment = Reader{ environment.read() },
                    i18n = (storage * i18n)
                )
            }
        ),
        onLoading = {Loading()}
    ) {

    // Data
    val applicationContextId = storage * availablePermissions * contextFromPath("APPLICATION") * assureValue() * contextId.get
    // Data / I18N
    val texts = storage * i18n * language * component(UserLangComponent.UserManagementPage)
    val buttons = texts * subComp("buttons")
    val dialogs = texts * subComp("dialogs")
    val registeredUsers = texts * subComp("registeredUsers")


    val managedUserIds = Read(storage * managedUsers) map {users -> users.map{ user -> user.id}}

    LaunchedEffectOnSource(managedUserIds) {
        if(managedUserIds.emit().isEmpty()) return@LaunchedEffectOnSource
        storage * userActions dispatch readUserProfiles(managedUserIds.emit())
    }

    var uiState by remember{
        mutableStateOf(UiState())
    }

    val query = remember(
        uiState.filter,
        uiState.sortOrder,
        uiState.pageSize,
        uiState.pageOffset
    ) {
        query{
            select("user")
            val query = uiState.filter.asQuery()
            if(query != null ) where{
                query()
            }
            val sortOrder = uiState.sortOrder.asQuerySortOrder()
            sortOrder()
            page(
                uiState.pageSize,
                uiState.pageOffset
            )
        }
    }

    LaunchedEffect(query) {
        launch {
            val action = userQuery(
                query
            )
            trigger(action) on storage
        }
    }

    // State
    var useR by remember { mutableStateOf<CreateUser?>(null) }
    // Markup
    Vertical(verticalPageStyle) {
        Wrap {
            H1 { Text((texts * title).emit()) }
        }

        val listStyles = ListStyles().modifyFilterWrapper{
            width(100.percent)
            paddingLeft(20.px)
        }

        ListWrapper(listStyles.listWrapper) {
            TitleWrapper {
                Horizontal(styles = { justifyContent(JustifyContent.SpaceBetween); width(100.percent) }) {
                    Title { H2 {  Text((registeredUsers * title).emit()) }}
                    Horizontal {
                        AnglesLeftButton(
                            color = Color.black,
                            bgColor = Color.white,
                            texts = { "Previous ${uiState.pageSize} users" },
                            deviceType = storage * deviceData * mediaType.get,
                        ) {
                            val newOffset = uiState.pageOffset - uiState.pageSize

                            uiState = uiState.copy(pageOffset = when{
                                newOffset > 0 -> newOffset
                                else -> 0
                            })
                        }
                        AnglesRightButton(
                            color = Color.black,
                            bgColor = Color.white,
                            texts = {"Next ${uiState.pageSize} users"},
                            deviceType = storage * deviceData * mediaType.get,
                        ) {
                            uiState = uiState.copy(
                                pageOffset = uiState.pageOffset + uiState.pageSize
                            )
                        }
                        PlusButton(
                            color = Color.black,
                            bgColor = Color.white,
                            buttons * subComp("createUser") * title,
                            (storage * deviceData * mediaType.get),
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
            FilterWrapper(listStyles.filterWrapper) {
                Filter(listStyles.modifyFilter { width(20.percent) }.filter) {
                    TextFilter(
                        title = "Username",
                        state = uiState.filter.username?:"",
                        refreshOnInput = true,
                        ignoreCase = false,
                    ){ text, bool -> uiState = uiState.filter{
                        username {
                            text.ifBlank { null }
                        }
                    }
                } }
                Filter(listStyles.modifyFilter { width(10.percent) }.filter) {
                    TextFilter(
                        title = "Status",
                        state = uiState.filter.status?:"",
                        refreshOnInput = true,
                        ignoreCase = false,
                    ){ text, bool -> uiState = uiState.filter{
                        status{
                            text.ifBlank { null }
                        }
                    } }
                }
                Filter(listStyles.modifyFilter { width(10.percent) }.filter) {
                    TextFilter(
                        title = "Firstname",
                        state = uiState.filter.firstName?:"",
                        refreshOnInput = true,
                        ignoreCase = false,
                    ){ text, bool -> uiState = uiState.filter{
                        firstName{
                            text.ifBlank { null }
                        }
                    }}
                }
                Filter(listStyles.modifyFilter { width(10.percent) }.filter) {
                    TextFilter(
                        title = "Lastname",
                        state = uiState.filter.lastName?:"",
                        refreshOnInput = true,
                        ignoreCase = false,
                    ){ text, bool -> uiState = uiState.filter{
                        lastName{
                            text.ifBlank { null }
                        }
                    } }
                }
            }

            HeaderWrapper(listStyles.headerWrapper) {
                Header(listStyles.header) {
                    HeaderCellWithActions(
                        text = {"Username"},
                        styles = HeaderCellStyles().width(20.percent),
                        ordering = { SortByDrop{ order: SortOrder ->
                            uiState = uiState.sortOrder {
                                username { order }
                            }
                        } }
                    )
                    HeaderCellWithActions(
                        text = {"Status"},
                        styles = HeaderCellStyles().width(10.percent),
                        ordering = { SortByDrop{ order: SortOrder ->
                            uiState = uiState.sortOrder {
                                status { order }
                            }
                        } }
                    )
                    HeaderCellWithActions(
                        text = {"Lastname, Firstname"},
                        styles = HeaderCellStyles().width(10.percent),
                        ordering = { SortByDrop{ order: SortOrder ->
                            uiState = uiState.sortOrder {
                                lastName { order }
                            }
                        } }
                    )
                    // HeaderCell("Name"){width(20.percent)}
                }
            }

            ListItemsIndexed((storage * managedUsers).read()/*.drop(pageOffsetState.toInt()).take(pageSizeState)*/) {index, item ->
                ListItemWrapper({ listItemWrapperStyle(index) }) {
                    DataWrapper(listStyles.dataWrapper) {
                        TextCell(item.username) { width(20.percent) }
                        TextCell(item.status.name) { width(10.percent) }
                        TextCell(
                            item.profile?.let{ "${it.lastname}, ${it.firstname}"}?: ""
                        ) {width(20.percent)}
                    }
                    ActionsWrapper(listStyles.actionsWrapper) {
                        EditButton(color = Color.black,
                            bgColor = Color.white,
                            registeredUsers * subComp("buttons") * subComp("edit") * title,
                            storage * deviceData * mediaType.get,
                            (storage * isNotGranted(AppRight.Application.Users.manage, applicationContextId)).emit()
                        ){}
                        XMarkButton(
                            color = Color.black,
                            bgColor = Color.white,
                            registeredUsers * subComp("buttons") * subComp("delete") * title,
                            storage * deviceData * mediaType.get,
                            true
                        ){}
                    }
                }
            }
        }
    }
}
}
