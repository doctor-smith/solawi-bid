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
import org.evoleq.iql.data.Query
import org.evoleq.iql.dsl.FilterBuilder
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
import org.solyton.solawi.bid.module.user.service.profile.fullname


data class UserFilterQuery(
    val username: String? = null,
    val status: String? = null,
    val firstName: String? = null,
    val lastName : String? = null,
) {
    fun isNotNull() = listOfNotNull(username, status, firstName, lastName).isNotEmpty()

    fun asQuery(): (FilterBuilder.() -> Unit)? = when {
        isNotNull() -> {
            {
                username?.let {
                    p("user.username") contains it
                }
                status?.let { p("user.status") containsIgnoreCase   it }
                firstName?.let {
                    any("userProfiles") {
                        p("userProfile.first_name") containsIgnoreCase   it
                    }
                }
                lastName?.let {
                    any("userProfiles") {
                        p("userProfile.last_name") containsIgnoreCase  it
                    }
                }
            }
        }

        else -> null
    }
}


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

        storage * userActions dispatch readUserProfiles(managedUserIds.emit())
    }



    var filterState by remember{
        mutableStateOf(UserFilterQuery())
    }
    var pageSizeState by remember { mutableStateOf(20) }
    var pageOffsetState by remember { mutableStateOf(0L) }
    var queryState by remember(
        filterState,
        pageSizeState,
        pageOffsetState
    ) {
           mutableStateOf<Query>(
            query{
                select("user")
                val query = filterState.asQuery()
                if(query != null ) where{
                    query()
                }
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
    // Markup
    Vertical(verticalPageStyle) {
        Wrap {
            H1 { Text((texts * title).emit()) }
        }

        val listStyles = ListStyles().modifyFilterWrapper{
            width(80.percent)
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
                        state = filterState.username?:"",
                        refreshOnInput = true,
                        ignoreCase = false,
                    ){ text, bool -> filterState = filterState.copy(username = when{
                        text.isBlank() -> null
                        else -> text
                    }) }
                }
                Filter(listStyles.modifyFilter { width(10.percent) }.filter) {
                    TextFilter(
                        title = "Status",
                        state = filterState.status?:"",
                        refreshOnInput = true,
                        ignoreCase = false,
                    ){ text, bool -> filterState = filterState.copy(status = when{
                        text.isBlank() -> null
                        else -> text
                    }) }
                }
                Filter(listStyles.modifyFilter { width(10.percent) }.filter) {
                    TextFilter(
                        title = "Firstname",
                        state = filterState.firstName?:"",
                        refreshOnInput = true,
                        ignoreCase = false,
                    ){ text, bool -> filterState = filterState.copy(firstName = when{
                        text.isBlank() -> null
                        else -> text
                    }) }
                }
                Filter(listStyles.modifyFilter { width(10.percent) }.filter) {
                    TextFilter(
                        title = "Lastname",
                        state = filterState.lastName?:"",
                        refreshOnInput = true,
                        ignoreCase = false,
                    ){ text, bool -> filterState = filterState.copy(lastName = when{
                        text.isBlank() -> null
                        else -> text
                    }) }
                }
            }

            HeaderWrapper(listStyles.headerWrapper) {
                Header(listStyles.header) {
                    HeaderCell("username") { width(20.percent) }
                    HeaderCell("status") { width(10.percent) }
                    HeaderCell("Name") { width(20.percent) }
                }
            }

            ListItemsIndexed((storage * managedUsers).read()/*.drop(pageOffsetState.toInt()).take(pageSizeState)*/) {index, item ->
                ListItemWrapper({ listItemWrapperStyle(index) }) {
                    DataWrapper(listStyles.dataWrapper) {
                        TextCell(item.username) { width(20.percent) }
                        TextCell(item.status.name) { width(10.percent) }
                        TextCell(
                            item.profile?.fullname() ?: ""
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
