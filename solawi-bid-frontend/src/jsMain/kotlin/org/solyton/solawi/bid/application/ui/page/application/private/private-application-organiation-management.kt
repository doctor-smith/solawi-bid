package org.solyton.solawi.bid.application.ui.page.application.private

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.guard.data.isLoading
import org.evoleq.compose.guard.data.onEmpty
import org.evoleq.compose.guard.data.onNullLaunch
import org.evoleq.compose.guard.data.withLoading
import org.evoleq.compose.layout.Horizontal
import org.evoleq.device.data.mediaType
import org.evoleq.language.component
import org.evoleq.language.subComp
import org.evoleq.language.subTitle
import org.evoleq.language.title
import org.evoleq.language.tooltip
import org.evoleq.math.emit
import org.evoleq.math.map
import org.evoleq.math.times
import org.evoleq.optics.lens.DeepRead
import org.evoleq.optics.lens.DeepSearch
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.api.solawiApi
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.env.i18nEnvironment
import org.solyton.solawi.bid.application.data.transform.application.management.applicationManagementModule
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.application.i18n.ApplicationLangComponent
import org.solyton.solawi.bid.application.ui.page.application.style.actionsWrapperStyle
import org.solyton.solawi.bid.application.ui.page.application.style.listItemWrapperStyle
import org.solyton.solawi.bid.application.ui.page.user.action.getUsers
import org.solyton.solawi.bid.module.application.action.readApplicationContextRelations
import org.solyton.solawi.bid.module.application.action.readApplications
import org.solyton.solawi.bid.module.application.action.readPersonalApplicationOrganizationContextRelations
import org.solyton.solawi.bid.module.application.component.modal.CheckedUserRole
import org.solyton.solawi.bid.module.application.component.modal.showManageUserPermissionsModule
import org.solyton.solawi.bid.module.application.data.management.applicationManagementActions
import org.solyton.solawi.bid.module.application.data.management.applicationManagementModals
import org.solyton.solawi.bid.module.application.data.management.availableApplications
import org.solyton.solawi.bid.module.application.data.management.personalApplicationContextRelations
import org.solyton.solawi.bid.module.application.i18n.Component
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.control.button.UserLockButton
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.guard.onMissing
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.loading.component.Loading
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.permission.data.api.PutUserRoleContext
import org.solyton.solawi.bid.module.permissions.data.contexts
import org.solyton.solawi.bid.module.permissions.service.contextFromPath
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.SubTitle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.action.organization.readOrganizations
import org.solyton.solawi.bid.module.user.action.permission.putUsersRoleContext
import org.solyton.solawi.bid.module.user.action.permission.readPermissionsOfUsersAction
import org.solyton.solawi.bid.module.user.action.permission.readUserPermissionsAction
import org.solyton.solawi.bid.module.user.data.organization.members
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.organizations
import org.solyton.solawi.bid.module.user.data.userActions

@Markup
@Composable
@Suppress("FunctionName")
fun PrivateApplicationOrganizationManagementPage(
    storage: Storage<Application>,
    applicationId: String,
    organizationId: String
) = withLoading(
    isLoading = isLoading(
        onNullLaunch(
            storage * availablePermissions * contextFromPath("APPLICATION"),
        ){
            CoroutineScope(Job()).launch { (storage * userIso * userActions ).dispatch(readUserPermissionsAction()) }
        },
        onEmpty(storage * applicationManagementModule * availableApplications.get) {
            CoroutineScope(Job()).launch {
                (storage * applicationManagementModule * applicationManagementActions).dispatch(
                    readApplications
                )
            }
        },
        onEmpty(storage * applicationManagementModule * personalApplicationContextRelations.get) {
            CoroutineScope(Job()).launch {
                (storage * applicationManagementModule * applicationManagementActions).dispatch(
                    readApplicationContextRelations
                )
            }
        },
        onEmpty(storage * applicationManagementModule * applicationOrganizationRelations.get) {
            CoroutineScope(Job()).launch {
                (storage * applicationManagementModule * applicationManagementActions).dispatch(
                    readPersonalApplicationOrganizationContextRelations()
                )
            }
        },
        onEmpty(storage * userIso * user * organizations.get) {
            CoroutineScope(Job()).launch {
                (storage * userIso * userActions).dispatch(
                    readOrganizations()
                )
            }
        },
        onEmpty((storage * managedUsers.get )) {
            CoroutineScope(Job()).launch {
                (storage * userIso * userActions).dispatch(
                    getUsers()
                )
            }
        },
        onEmpty((storage * managedUsers.get ) map {users -> users.filter{
            it.permissions.contexts.isNotEmpty()
        }}) {
            CoroutineScope(Job()).launch {
                (storage * userIso * userActions).dispatch(
                    readPermissionsOfUsersAction()
                )
            }
        },
        onMissing(
            ApplicationLangComponent.PrivateApplicationOrganizationManagementPage,
            storage * i18N.get
        ) {
            LaunchComponentLookup(
                langComponent = ApplicationLangComponent.PrivateApplicationOrganizationManagementPage,
                environment = storage * environment * i18nEnvironment,
                i18n = (storage * i18N)
            )
        },
        *(storage * applicationManagementModule * availableApplications).read().map {
            onMissing(
                ApplicationLangComponent.ApplicationDetails(it.name),
                storage * i18N.get
            ){
                LaunchComponentLookup(
                    langComponent = ApplicationLangComponent.ApplicationDetails(it.name),
                    environment = storage * environment * i18nEnvironment,
                    i18n = (storage * i18N)
                )
            }
        }.toBooleanArray()
    ),
    onLoading = { Loading() },
){

    val device = storage * deviceData * mediaType.get

    // val base = storage * i18N * language * Component.base
    val texts = storage * i18N * language * component(ApplicationLangComponent.PrivateApplicationOrganizationManagementPage)
    // val connectDialogTexts = texts * subComp("dialogs") * subComp("connectApplicationToOrganization")


    val app = storage * applicationManagementModule * availableApplications * FirstBy { it.id == applicationId }
    val organization = storage * userIso * user * organizations * DeepSearch { it.organizationId == organizationId }

    val personalApplications = storage * personalApplications
    val organizations = storage * userIso * user * organizations

    val organizationRelations = storage * applicationManagementModule * applicationOrganizationRelations

    /*
    val mapOfLinkedOrganizations = personalApplications.read().associate { application ->
        application.id to organizationRelations.read()
            .filter { it.applicationId == application.id }
            .mapNotNull { relation -> (organizations * DeepRead { org -> org.organizationId == relation.organizationId }).emit() }
    }

     */

    val applicationOrganizationContextId = organizationRelations.read().first {
        it.applicationId == applicationId && it.organizationId == organizationId
    }.contextId

    val context = storage * availablePermissions * contexts * DeepSearch { it.contextId == applicationOrganizationContextId }

    val users = storage * managedUsers
    val permissionsMap = users.read().associate {
        user -> user.username to user.permissions.contexts.firstOrNull {
            it.contextId == applicationOrganizationContextId
        }
    }.filterValues { it != null }

    Page(verticalPageStyle) {
        Wrap {
            Horizontal {
                PageTitle(texts * title)
            }
            SubTitle(texts * subTitle)
        }
        Wrap {
            H2 { Text("Application: ${(app * org.solyton.solawi.bid.module.application.data.application.name.get).emit()}") }
            H2 { Text("Organization: ${(organization * org.solyton.solawi.bid.module.user.data.organization.name.get).emit()}") }
        }
        ListWrapper {
            TitleWrapper { Title{ H3{Text((texts * subComp("listOfMembers") * title).emit()) } } }
            HeaderWrapper {
                Header{
                    HeaderCell(texts * subComp("listOfMembers") * subComp("headers") * subComp("username") * title){width(20.percent)}
                    HeaderCell(texts * subComp("listOfMembers") * subComp("headers") * subComp("roles") * title){width(80.percent)}
                }
            }
            ListItemsIndexed(organization * members) { index, member ->
                ListItemWrapper(
                    {listItemWrapperStyle(this, index)}
                ) {
                    DataWrapper {
                        TextCell(member.username){width(20.percent)}
                        TextCell(permissionsMap[member.username]!!.roles.joinToString(", "){it.roleName}){width(80.percent)}
                    }
                    ActionsWrapper(
                        actionsWrapperStyle
                    ) {
                        val userRoleIds = permissionsMap[member.username]!!.roles.map{it.roleId}
                        var checkedRoles: List<CheckedUserRole> by remember {
                            mutableStateOf( context.read().roles.map {
                                when {
                                    it.roleId in userRoleIds -> CheckedUserRole(it, true)
                                    else -> CheckedUserRole(it, false)
                                }
                            })
                        }
                        UserLockButton(
                            Color.black,
                            Color.white,
                            texts * subComp("listOfMembers") * subComp("actions") * subComp("manageUserPermissions") * tooltip map{ it.replace("\$user" , member.username) },
                            device,
                        ) {
                            (storage * applicationManagementModule * applicationManagementModals).showManageUserPermissionsModule(
                                texts * subComp("dialogs") * subComp("manageUsersPermissions"),
                                device,
                                {dev -> auctionModalStyles(dev) },
                                checkedRoles,
                                {r -> checkedRoles = r},
                                {},
                            ) {
                                CoroutineScope(Job()).launch {
                                    // set user roles in context

                                    (storage * userIso * userActions).dispatch(putUsersRoleContext(
                                        member.memberId,
                                        applicationOrganizationContextId,
                                        checkedRoles.filter { it.checked }.map { it.role.roleId }
                                    ))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
