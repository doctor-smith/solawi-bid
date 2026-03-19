package org.solyton.solawi.bid.application.ui.page.application.private

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.guard.data.isLoading
import org.evoleq.compose.guard.data.onNullLaunch
import org.evoleq.compose.guard.data.withLoading
import org.evoleq.compose.layout.Horizontal
import org.evoleq.device.data.mediaType
import org.evoleq.language.*
import org.evoleq.math.DeepRead
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.lens.FilterBy
import org.evoleq.optics.storage.ActionEnvelope
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.storage.times
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.env.i18nEnvironment
import org.solyton.solawi.bid.application.data.transform.application.import
import org.solyton.solawi.bid.application.data.transform.application.management.applicationManagementModule
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.application.i18n.ApplicationLangComponent
import org.solyton.solawi.bid.application.ui.page.application.style.actionsWrapperStyle
import org.solyton.solawi.bid.application.ui.page.application.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.application.action.*
import org.solyton.solawi.bid.module.application.component.modal.showConnectApplicationToOrganizationModule
import org.solyton.solawi.bid.module.application.data.LifecycleStage
import org.solyton.solawi.bid.module.application.data.management.actions
import org.solyton.solawi.bid.module.application.data.management.applicationManagementModals
import org.solyton.solawi.bid.module.application.data.management.applicationOrganizationRelations
import org.solyton.solawi.bid.module.application.data.management.availableApplications
import org.solyton.solawi.bid.module.application.i18n.Component
import org.solyton.solawi.bid.module.application.i18n.application
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.control.button.*
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.guard.onMissing
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.loading.component.Loading
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.permissions.service.contextFromPath
import org.solyton.solawi.bid.module.process.service.process.runProcesses
import org.solyton.solawi.bid.module.process.service.process.sequence
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.SubTitle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.action.organization.READ_ORGANIZATIONS
import org.solyton.solawi.bid.module.user.action.organization.readOrganizations
import org.solyton.solawi.bid.module.user.action.permission.readUserPermissionsAction
import org.solyton.solawi.bid.module.user.action.user.GET_USERS
import org.solyton.solawi.bid.module.user.action.user.READ_USER_PROFILES
import org.solyton.solawi.bid.module.user.action.user.getUsers
import org.solyton.solawi.bid.module.user.action.user.readUserProfiles
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.organizations
import org.solyton.solawi.bid.module.user.data.userActions

@Markup
@Composable
@Suppress("FunctionName")
fun PrivateApplicationManagementPage(storage: Storage<Application>) {
    val scope = rememberCoroutineScope()
    withLoading(
    isLoading = isLoading(
        onNullLaunch(
            storage * availablePermissions * contextFromPath("APPLICATION"),
        ){
            scope.launch { (storage * userIso * userActions ).dispatch(readUserPermissionsAction()) }
        },
        onMissing(
            ApplicationLangComponent.PrivateApplicationManagementPage,
            storage * i18N.get
        ) {
            LaunchComponentLookup(
                langComponent = ApplicationLangComponent.PrivateApplicationManagementPage,
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
        }.toBooleanArray(),
        *storage.runProcesses(
            ActionEnvelope(
                userIso * readOrganizations(),
                READ_ORGANIZATIONS,
            ),
            sequence(
                ActionEnvelope(
                    userIso * getUsers(),
                    GET_USERS,
                ),
                ActionEnvelope(
                    userIso * readUserProfiles(emptyList()),
                    READ_USER_PROFILES,
                )
            ),
            ActionEnvelope(
                applicationManagementModule * readApplications,
                READ_APPLICATIONS,
            ),
            ActionEnvelope(
                applicationManagementModule * readPersonalApplicationContextRelations,
                READ_PERSONAL_APPLICATION_CONTEXT_RELATIONS,
            ),
            ActionEnvelope(
                applicationManagementModule * readPersonalApplicationOrganizationContextRelations(),
                READ_PERSONAL_APPLICATION_ORGANIZATION_CONTEXT_RELATIONS,
            )
        ),
    ),
    onLoading = { Loading() },
){
        val device = storage * deviceData * mediaType.get
        val base = storage * i18N * language * Component.base
        val texts = storage * i18N * language * component(ApplicationLangComponent.PrivateApplicationManagementPage)
        val connectDialogTexts = texts * subComp("dialogs") * subComp("connectApplicationToOrganization")

        val personalApplications = storage * personalApplications

        // todo:dev don't show Main Application to every one!! For the moment we just filter it out
        val availableApplications = storage * applicationManagementModule * availableApplications * FilterBy {
            app -> app.name != "APPLICATION_MANAGEMENT"  && app.id !in personalApplications.read().map { it.id }
        }
        val organizations = storage * userIso * user * organizations
        val modals = storage * applicationManagementModule * applicationManagementModals

        val organizationRelations = storage * applicationManagementModule * applicationOrganizationRelations

        val mapOfLinkedOrganizations = personalApplications.read().associate { application ->
            application.id to organizationRelations.read()
                .filter { it.applicationId == application.id }
                .mapNotNull { relation -> (organizations * DeepRead { org -> org.organizationId == relation.organizationId }).emit() }
        }

        // state
        var organizationId by remember { mutableStateOf("") }

        Page(verticalPageStyle) {
            Wrap {
                Horizontal() {
                    PageTitle(texts * title)
                }
                SubTitle(texts * subTitle)
            }
            ListWrapper {
                TitleWrapper { H3{
                    Text((texts * with(Component){listOfApplications * titles * bookedApplicationsTitle}).emit())
                } }
                HeaderWrapper{
                    Header {
                        HeaderCell(
                            texts * with(Component){listOfApplications * headers * application} * title
                        ){ width(35.percent) }
                        HeaderCell("Status") {
                            width(10.percent)
                        }
                        HeaderCell(texts * with(Component){listOfApplications * headers * linkedOrganizations} * title) {
                            width(55.percent)
                        }
                    }
                }
                ListItemsIndexed(personalApplications) { index, application ->
                    ListItemWrapper({
                        listItemWrapperStyle(this , index)
                    }) {
                        DataWrapper {
                            TextCell(
                                base * application(application.name) * title
                            ) { width(35.percent) }
                            TextCell(application.state.toString()) { width(10.percent) }
                            TextCell(mapOfLinkedOrganizations[application.id]?.joinToString(", ") { it.name }?:""){
                                width(55.percent)
                            }
                        }
                        ActionsWrapper({
                            actionsWrapperStyle(this)
                        }) {
                            When(application.state == LifecycleStage.Registered) {
                                PlayButton(
                                    color = Color.black,
                                    bgColor = Color.white,
                                    texts = texts * with(Component){listOfApplications * actions * subComp("subscribeApplication")} * tooltip,
                                    deviceType = device,
                                    isDisabled = true,
                                ) {
                                    scope.launch { (storage * applicationManagementModule * actions).dispatch(
                                        subscribeApplications(listOf(application.id))
                                    ) }
                                }
                                ClockButton(
                                    color = Color.black,
                                    bgColor = Color.white,
                                    texts = texts * with(Component){listOfApplications * actions * subComp("startTrialOfApplication")} * tooltip,
                                    deviceType = device,
                                    isDisabled = false
                                ) {
                                    scope.launch { (storage * applicationManagementModule * actions).dispatch(
                                        startTrialsOfApplications(listOf(application.id))
                                    ) }
                                }
                            }
                            When(application.state == LifecycleStage.Trialing) {
                                PlayButton(
                                    color = Color.black,
                                    bgColor = Color.white,
                                    texts = texts * with(Component){listOfApplications * actions * subComp("subscribeApplication")} * tooltip,
                                    deviceType = device,
                                ) {
                                    scope.launch { (storage * applicationManagementModule * actions).dispatch(
                                        subscribeApplications(listOf(application.id))
                                    ) }
                                }
                            }
                            When(application.state == LifecycleStage.Active) {
                                XMarkButton(
                                    color = Color.black,
                                    bgColor = Color.white,
                                    texts = texts * with(Component){listOfApplications * actions * subComp("cancelApplicationSubscription")} * tooltip,
                                    deviceType = device,
                                    isDisabled = true,
                                ) {
                                    /*
                                    scope.launch { (storage * applicationManagementModule * actions).dispatch(
                                        subscribeApplications(listOf(application.id))
                                    ) }

                                     */
                                }

                                ShareNodesButton(
                                    Color.black,
                                    Color.white,
                                    texts * Component.listOfApplications * Component.actions * subComp("connectApplication") * tooltip,
                                    device,
                                ) {
                                    modals.showConnectApplicationToOrganizationModule(
                                        texts = connectDialogTexts,
                                        device = device,
                                        styles = { dev -> auctionModalStyles(dev) },
                                        application = application,
                                        organizations = organizations.read().import(),
                                        setOrganizationId = { id -> organizationId = id },
                                        cancel = {}
                                    ) {
                                        scope.launch {
                                            (storage * applicationManagementModule * actions).dispatch(
                                                connectApplicationToOrganization(
                                                    application.id,
                                                    organizationId,
                                                    application.modules.filter { module ->
                                                        module.state == LifecycleStage.Active
                                                    }.map { it.id }
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            When(availableApplications.read().isNotEmpty()) {
                ListWrapper {
                    TitleWrapper { H3 { Text((texts * with(Component) { listOfApplications * titles * availableApplicationsTitle }).emit()) } }

                    HeaderWrapper {
                        Header {
                            HeaderCell(texts * with(Component) { listOfApplications * headers * application } * title) {
                                width(35.percent)
                            }
                            HeaderCell(texts * with(Component) { listOfApplications * headers * applicationDescription } * title) {
                                width(65.percent)
                            }
                        }
                    }

                    ListItemsIndexed(availableApplications) { index, application ->
                        ListItemWrapper({
                            listItemWrapperStyle(this, index)
                        }) {
                            DataWrapper {
                                TextCell(
                                    base * application(application.name) * title
                                ) { width(35.percent) }
                                TextCell(
                                    "Bla bla "//base * application(application.name) * org.evoleq.language.description
                                ) { width(65.percent) }
                            }
                            ActionsWrapper() {
                                DetailsButton(
                                    color = Color.black,
                                    bgColor = Color.white,
                                    texts = texts * with(Component) { listOfApplications * actions * showDetails } * tooltip,
                                    deviceType = device,
                                    isDisabled = false,

                                    ) {

                                }
                                val scope = rememberCoroutineScope()
                                PlusButton(
                                    color = Color.black,
                                    bgColor = Color.white,
                                    texts = texts * with(Component) { listOfApplications * actions * registerForApplication } * tooltip,
                                    deviceType = device,
                                    isDisabled = false,

                                    ) {
                                    scope.launch {
                                        (storage * applicationManagementModule * actions).dispatch(
                                            registerForApplications(
                                                listOf(application.id)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

