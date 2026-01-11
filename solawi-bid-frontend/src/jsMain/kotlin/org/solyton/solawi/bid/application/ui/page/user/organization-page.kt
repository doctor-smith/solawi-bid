package org.solyton.solawi.bid.application.ui.page.user

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.guard.data.*
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.language.component
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.language.tooltip
import org.evoleq.math.*
import org.evoleq.optics.lens.DeepSearch
import org.evoleq.optics.lens.FilterBy
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.env.i18nEnvironment
import org.solyton.solawi.bid.application.data.i18N
import org.solyton.solawi.bid.application.data.processes
import org.solyton.solawi.bid.application.data.transform.application.management.applicationManagementModule
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.service.organization.importMembersFromCsv
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.application.i18n.ApplicationLangComponent
import org.solyton.solawi.bid.application.ui.page.user.i18n.OrganizationLangComponent
import org.solyton.solawi.bid.application.ui.page.user.style.actionsWrapperStyle
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.application.action.readApplications
import org.solyton.solawi.bid.module.application.action.readPersonalApplicationOrganizationContextRelations
import org.solyton.solawi.bid.module.application.data.management.applicationManagementActions
import org.solyton.solawi.bid.module.application.data.management.applicationOrganizationRelations
import org.solyton.solawi.bid.module.application.data.management.availableApplications
import org.solyton.solawi.bid.module.application.i18n.ApplicationComponent
import org.solyton.solawi.bid.module.application.i18n.application
import org.solyton.solawi.bid.module.application.i18n.module
import org.solyton.solawi.bid.module.control.button.ArrowUpButton
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.control.button.UploadButton
import org.solyton.solawi.bid.module.control.button.UsersButton
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.guard.onMissing
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.defaultListStyles
import org.solyton.solawi.bid.module.loading.component.Loading
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.process.data.process.Process
import org.solyton.solawi.bid.module.process.data.processes.IsInactive
import org.solyton.solawi.bid.module.process.data.processes.IsNotRegistered
import org.solyton.solawi.bid.module.process.data.processes.Register
import org.solyton.solawi.bid.module.process.data.processes.registry
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.action.organization.readOrganizations
import org.solyton.solawi.bid.module.user.action.user.GET_USERS
import org.solyton.solawi.bid.module.user.action.user.READ_USER_PROFILES
import org.solyton.solawi.bid.module.user.action.user.getUsers
import org.solyton.solawi.bid.module.user.action.user.readUserProfiles
import org.solyton.solawi.bid.module.user.component.modal.showImportMembersToOrganizationModal
import org.solyton.solawi.bid.module.user.data.*
import org.solyton.solawi.bid.module.user.data.organization.members
import org.solyton.solawi.bid.module.user.data.organization.name
import org.solyton.solawi.bid.module.user.data.user.organizations
import org.solyton.solawi.bid.module.user.i18n.Component
import org.solyton.solawi.bid.module.user.service.profile.firstAddress
import org.solyton.solawi.bid.module.user.service.profile.fullname
import org.solyton.solawi.bid.application.data.environment as appEnv


@Markup
@Composable
@Suppress("FunctionName", "CognitiveComplexMethod")
fun OrganizationPage(applicationStorage: Storage<Application>, organizationId: String) = withLoading(
    isLoading = isLoading(
        onMissing(
            OrganizationLangComponent.OrganizationPage,
            applicationStorage * userIso * i18n.get
        ) {
            LaunchComponentLookup(
                langComponent = OrganizationLangComponent.OrganizationPage,
                environment = applicationStorage * userIso * environment.get,
                i18n = (applicationStorage * userIso * i18n)
            )
        },
        onEmpty(
            applicationStorage * userIso * user * organizations.get
        ){
            LaunchedEffect(Unit) {
                launch {
                    (applicationStorage * userIso * userActions).dispatch(readOrganizations())
                }
            }
        },
        sequentiallyExecuted(
            onFulfilled(
                {
                    ConditionalActionInput(
                        applicationStorage * userIso * managedUsers.get,
                        applicationStorage * userIso * managedUsers.get
                    )},
                { (applicationStorage * processes * IsNotRegistered(GET_USERS)).emit()  }
            ){
                applicationStorage * processes * Register dispatch Process(GET_USERS)
                LaunchedEffect(Unit) {
                    launch {
                        (applicationStorage * userIso * userActions).dispatch(getUsers("ReactAndDeactivateProcess"))
                    }
                }
            },
            onFulfilled(
                {
                    ConditionalActionInput(
                        applicationStorage * userIso * managedUsers.get map {it.filter{ user -> user.profile != null }},
                        applicationStorage * userIso * managedUsers.get map {it.filter{ user -> user.profile != null }}
                    )},
                {
                    (applicationStorage * processes * registry * Get(GET_USERS) * IsInactive).emit() &&
                    (applicationStorage * processes * IsNotRegistered(READ_USER_PROFILES)).emit()
                }
            ){
                applicationStorage * processes * Register dispatch Process(READ_USER_PROFILES)
                LaunchedEffect(Unit) {
                    launch {
                        val userIds = (applicationStorage * userIso * managedUsers.get) map {it.map {user -> user.id}}
                        (applicationStorage * userIso * userActions).dispatch(readUserProfiles(userIds.emit(), "ReactAndStopProcess"))
                    }
                }
            }
        ),
        onEmpty(applicationStorage * applicationManagementModule * availableApplications.get) {
            LaunchedEffect(Unit) {
                launch {
                    (applicationStorage * applicationManagementModule * applicationManagementActions).dispatch(
                        readApplications
                    )
                }
            }
        },
        onEmpty(applicationStorage * applicationManagementModule * applicationOrganizationRelations.get) {
            LaunchedEffect(Unit) {
                launch {
                    (applicationStorage * applicationManagementModule * applicationManagementActions).dispatch(
                        readPersonalApplicationOrganizationContextRelations()
                    )
                }
            }
        },
        *(applicationStorage * applicationManagementModule * availableApplications).read().map {
            onMissing(
                ApplicationLangComponent.ApplicationDetails(it.name),
                applicationStorage * i18N.get
            ){
                LaunchComponentLookup(
                    langComponent = ApplicationLangComponent.ApplicationDetails(it.name),
                    environment = applicationStorage * appEnv * i18nEnvironment,
                    i18n = (applicationStorage * i18N)
                )
            }
        }.toBooleanArray()
    ),
    onLoading = { Loading() }
) {
    val userModuleStorage = applicationStorage * userIso
    val device = userModuleStorage * deviceData * mediaType

    val organization = userModuleStorage * user * organizations * DeepSearch { it.organizationId == organizationId }
    val members = organization * members
    val memberProfilesMap = (userModuleStorage * managedUsers.get) map { user ->
        user.associateBy({ it.id }) { it.profile }
    }


    val applicationManagementStorage = applicationStorage * applicationManagementModule
    val availableApplications = applicationManagementStorage * availableApplications
    val applicationOrganizationRelations = applicationManagementStorage * applicationOrganizationRelations
    val connectedApplications = availableApplications * FilterBy { app ->
        applicationOrganizationRelations.read().any { it.applicationId == app.id && it.organizationId == organizationId }
    }

    // texts
    val base = applicationStorage * i18N * language * ApplicationComponent.base
    val texts = userModuleStorage * i18n * language * component(OrganizationLangComponent.OrganizationPage)
    val dialogs = texts * subComp("dialogs")
    val importMembersToOrganization = dialogs * subComp("importMembersToOrganization")
    val listOfMembers = texts * subComp("listOfMembers")
    val listOfMembersHeaders = texts * subComp("listOfMembers") * subComp("headers")

    val listOfConnectedApplications = texts * subComp("listOfConnectedApplications")
    val listOfConnectedApplicationsHeaders = listOfConnectedApplications * subComp("headers")
    val listOfConnectedApplicationsActions = listOfConnectedApplications * subComp("actions")

    Page(verticalPageStyle) {
        Wrap {
            Horizontal({
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
            }) {
                PageTitle(organization * name.get)
                Horizontal {
                    ArrowUpButton(
                        Color.black,
                        Color.white,
                        texts * subComp("actions") * subComp("navToManagementPage") * tooltip,
                        { device.read() }
                    ) {
                        navigate("/app/management/organizations")
                    }
                }
            }
        }

        ListWrapper({
            defaultListStyles.listWrapper(this)
            overflowX("auto")
        }) {
            var open by remember { mutableStateOf(false) }
            TitleWrapper {
                Title { H3{ Text((listOfMembers * title).emit()) }}
                SimpleUpDown(open, {open = !open})


            }
            if(open) {
                HeaderWrapper {
                    Header {
                        HeaderCell(listOfMembersHeaders * Component.standard * title) { width(30.percent) }
                        HeaderCell(listOfMembersHeaders * Component.userProfile * title) { width(30.percent) }
                        HeaderCell("Solawi Anteile | Status") { width(40.percent) }
                    }
                    ActionsWrapper({
                        defaultListStyles.actionsWrapper(this)
                        alignSelf(AlignSelf.FlexEnd)
                    }){
                        var csv: String? by remember { mutableStateOf<String?>(null) }
                        UploadButton(
                            color = Color.black,
                            bgColor = Color.white,
                            texts = listOfMembers * Component.actions * Component.importMembersToOrganization * tooltip,
                            deviceType = { device.read() }
                        ) {
                            (userModuleStorage * userModals).showImportMembersToOrganizationModal(
                                texts = importMembersToOrganization.emit(),
                                device = { device.read() },
                                csv = csv,
                                setCsv = {csv = it},
                                isOkButtonDisabled = {csv == null}
                            ) {
                                applicationStorage.importMembersFromCsv(organizationId, csv!!, ';')
                            }
                        }
                    }
                }
                HeaderWrapper {
                    Header {
                        HeaderCell(listOfMembersHeaders * Component.standard * Component.username * title) {
                            width(10.percent); overflow("hidden")
                        }
                        HeaderCell(listOfMembersHeaders * Component.standard * Component.roles * title) { width(20.percent) }
                        HeaderCell(listOfMembersHeaders * Component.userProfile * Component.name * title) { width(10.percent) }
                        HeaderCell(listOfMembersHeaders * Component.userProfile * Component.address * title) { width(20.percent) }
                        HeaderCell("Gemüse") { width(10.percent) }
                        HeaderCell("Eier") { width(10.percent) }
                        HeaderCell("Obst") { width(10.percent) }
                        HeaderCell("Status") { width(10.percent) }

                    }
                }
                ListItemsIndexed(members) { index, member ->
                    ListItemWrapper({
                        listItemWrapperStyle(this, index)
                    }) {
                        DataWrapper {
                            TextCell(member.username) {
                                width(10.percent); minWidth(10.percent); overflow("hidden")
                            }
                            TextCell(member.roles.joinToString(", ") { it.roleName }) {
                                width(20.percent);minWidth(10.percent); overflow("hidden")
                            }
                            val userProfile = (memberProfilesMap * Get(member.memberId)).emit()

                            TextCell(userProfile.fullname()) {
                                width(10.percent);minWidth(10.percent);overflow("hidden")
                            }

                            TextCell(userProfile.firstAddress()) {
                                width(20.percent); minWidth(20.percent);overflow("hidden")
                            }
                            NumberCell(1) { width(10.percent) }
                            NumberCell(0) { width(10.percent) }
                            NumberCell(1) { width(10.percent) }
                            TextCell("Aktiv") { width(10.percent) }
                        }
                        ActionsWrapper({
                            actionsWrapperStyle(this)
                            /*
                        position(Position.Sticky)
                        right(0.px)
                        backgroundColor(Color.white) // Verdeckt den scollenden Inhalt darunter
                        property("z-index", 1) // Bleibt über den anderen Zellen
                        // width(200.px)
*/
                        }) {
                            EditButton(
                                Color.black,
                                Color.white,
                                listOfMembers * Component.actions * Component.edit * tooltip,
                                { device.read() }
                            ) {
                                navigate("/app/management/user/${member.username}")
                            }
                        }
                    }
                }
            }
        }

        ListWrapper({
            defaultListStyles.listWrapper(this)
        }) {
            var open by remember { mutableStateOf(false) }
            TitleWrapper {
                Title { H3{ Text((listOfConnectedApplications * title).emit()) }}
                SimpleUpDown(open, {open = !open})
            }
            if(open) {
                HeaderWrapper {
                    Header {
                        HeaderCell(listOfConnectedApplicationsHeaders * subComp("application") * title ) { width(40.percent) }
                        HeaderCell(listOfConnectedApplicationsHeaders * subComp("modules") * title) { width(40.percent) }
                    }
                }
                ListItemsIndexed(connectedApplications) { index, application ->
                    ListItemWrapper({
                        listItemWrapperStyle(this, index)
                    }) {
                        DataWrapper {
                            TextCell(base * application(application.name) * title) { width(40.percent) }
                            TextCell(application.modules.joinToString(", ") {
                                (base * module(application.name, it.name) * title).emit()
                            }) { width(40.percent) }
                        }
                        ActionsWrapper({
                            actionsWrapperStyle(this)
                        }) {
                            UsersButton(
                                Color.black,
                                Color.white,
                                listOfConnectedApplicationsActions * subComp("manageUserPermissions") * tooltip,
                                { device.read() }
                            ) {
                                navigate("/app/management/private/application/${application.id}/organization/$organizationId")
                            }
                        }
                    }
                }
            }
        }
    }
}
