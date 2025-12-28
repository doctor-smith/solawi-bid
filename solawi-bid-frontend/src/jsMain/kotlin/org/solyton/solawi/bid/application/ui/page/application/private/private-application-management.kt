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
import org.evoleq.math.times
import org.evoleq.optics.lens.DeepRead
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.AlignSelf
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.alignSelf
import org.jetbrains.compose.web.css.paddingRight
import org.jetbrains.compose.web.css.paddingTop
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.env.i18nEnvironment
import org.solyton.solawi.bid.application.data.transform.application.import
import org.solyton.solawi.bid.application.data.transform.application.management.applicationManagementModule
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.application.i18n.ApplicationLangComponent
import org.solyton.solawi.bid.application.ui.page.application.style.actionsWrapperStyle
import org.solyton.solawi.bid.module.application.action.connectApplicationToOrganization
import org.solyton.solawi.bid.module.application.action.readApplicationContextRelations
import org.solyton.solawi.bid.module.application.action.readApplications
import org.solyton.solawi.bid.module.application.action.readPersonalApplicationOrganizationContextRelations
import org.solyton.solawi.bid.module.application.component.modal.showConnectApplicationToOrganizationModule
import org.solyton.solawi.bid.module.application.data.LifecycleStage
import org.solyton.solawi.bid.module.application.data.management.actions
import org.solyton.solawi.bid.module.application.data.management.applicationManagementActions
import org.solyton.solawi.bid.module.application.data.management.applicationManagementModals
import org.solyton.solawi.bid.module.application.data.management.availableApplications
import org.solyton.solawi.bid.module.application.data.management.personalApplicationContextRelations
import org.solyton.solawi.bid.module.application.i18n.Component
import org.solyton.solawi.bid.module.application.i18n.application
import org.solyton.solawi.bid.application.ui.page.application.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.control.button.ShareNodesButton
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.guard.onMissing
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.defaultListStyles
import org.solyton.solawi.bid.module.loading.component.Loading
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.permissions.service.contextFromPath
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.SubTitle
import org.solyton.solawi.bid.module.style.page.Title
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.action.organization.readOrganizations
import org.solyton.solawi.bid.module.user.action.permission.readUserPermissionsAction
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.organizations
import org.solyton.solawi.bid.module.user.data.userActions
import kotlin.Suppress
import kotlin.collections.associate
import kotlin.collections.filter
import kotlin.collections.joinToString
import kotlin.collections.map
import kotlin.collections.mapNotNull
import kotlin.collections.toBooleanArray
import kotlin.to
import kotlin.with

@Markup
@Composable
@Suppress("FunctionName")
fun PrivateApplicationManagementPage(storage: Storage<Application>) = withLoading(
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
        }.toBooleanArray()
    ),
    onLoading = { Loading() },
){

    val device = storage * deviceData * mediaType.get
    val base = storage * i18N * language * Component.base
    val texts = storage * i18N * language * component(ApplicationLangComponent.PrivateApplicationManagementPage)
    val connectDialogTexts = texts * subComp("dialogs") * subComp("connectApplicationToOrganization")

    val personalApplications = storage * personalApplications
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
            Horizontal {
                PageTitle(texts * title)
            }
            SubTitle(texts * subTitle)
        }
        ListWrapper {
            TitleWrapper { Title("") {  } }
        }
        HeaderWrapper{
            Header {
                HeaderCell(
                    texts * with(Component){listOfApplications * headers * application} * title
                ){ width(40.percent) }
                HeaderCell("Status") {
                    width(5.percent)
                }
                HeaderCell(texts * with(Component){listOfApplications * headers * linkedOrganizations} * title) {
                    width(35.percent)
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
                    ) { width(40.percent) }
                    TextCell(application.state.toString()) { width(5.percent) }
                    TextCell(mapOfLinkedOrganizations[application.id]?.joinToString(", ") { it.name }?:""){
                        width(35.percent)
                    }
                }
                ActionsWrapper({
                    actionsWrapperStyle(this)
                }) {
                    ShareNodesButton(
                        Color.black,
                        Color.white,
                        texts * Component.listOfApplications * Component.actions * subComp("connectApplication") * tooltip,
                        device,
                    ) {
                        modals.showConnectApplicationToOrganizationModule(
                            texts = connectDialogTexts,
                            device = device,
                            styles = {dev -> auctionModalStyles(dev) },
                            application = application,
                            organizations = organizations.read().import(),
                            setOrganizationId = {id -> organizationId = id},
                            cancel = {}
                        ) {
                            CoroutineScope(Job()).launch {
                                (storage * applicationManagementModule * actions).dispatch(
                                    connectApplicationToOrganization(
                                        application.id,
                                        organizationId,
                                        application.modules.filter{
                                                module -> module.state == LifecycleStage.Active
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
