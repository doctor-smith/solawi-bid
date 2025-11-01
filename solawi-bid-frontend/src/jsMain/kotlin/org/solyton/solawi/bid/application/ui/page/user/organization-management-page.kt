package org.solyton.solawi.bid.application.ui.page.user

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.guard.data.isLoading
import org.evoleq.compose.guard.data.onNullLaunch
import org.evoleq.compose.layout.Horizontal
import org.evoleq.device.data.mediaType
import org.evoleq.language.component
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.language.tooltip
import org.evoleq.math.Reader
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.on
import org.evoleq.math.times
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.storage.split
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.I
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.user.effect.trigger
import org.solyton.solawi.bid.application.ui.page.user.i18n.OrganizationLangComponent
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.control.button.DetailsButton
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.control.button.PlusButton
import org.solyton.solawi.bid.module.control.button.TrashCanButton
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.guard.onMissing
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.ListStyles
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.permissions.service.contextFromPath
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.action.organization.*
import org.solyton.solawi.bid.module.user.action.permission.readUserPermissionsAction
import org.solyton.solawi.bid.module.user.component.modal.showCreateChildOrganizationModal
import org.solyton.solawi.bid.module.user.component.modal.showCreateOrganizationModal
import org.solyton.solawi.bid.module.user.component.modal.showUpdateOrganizationModal
import org.solyton.solawi.bid.module.user.data.*
import org.solyton.solawi.bid.module.user.data.api.organization.CreateChildOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.CreateOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.UpdateOrganization
import org.solyton.solawi.bid.module.user.data.organization.Organization
import org.solyton.solawi.bid.module.user.data.organization.name
import org.solyton.solawi.bid.module.user.data.organization.organizationId
import org.solyton.solawi.bid.module.user.data.organization.subOrganizations
import org.solyton.solawi.bid.module.user.data.reader.isNotGranted
import org.solyton.solawi.bid.module.user.data.user.organizations
import org.solyton.solawi.bid.module.user.permission.OrganizationRight

@Markup
@Composable
@Suppress("FunctionName")
fun OrganizationManagementPage(storage: Storage<Application>) = Div {
    if(isLoading(
            onNullLaunch(
                storage * availablePermissions * contextFromPath("APPLICATION"),
            ){
                rememberCoroutineScope().launch { (storage * actions).dispatch(readUserPermissionsAction()) }
            },
        onMissing(
            OrganizationLangComponent.OrganizationManagementPage,
            storage * i18n.get,
        ) {
            LaunchComponentLookup(
                langComponent = OrganizationLangComponent.OrganizationManagementPage,
                environment = storage * environment.get,
                i18n = (storage * i18n)
            )
        }
    )) return@Div

    LaunchedEffect(Unit) {
        (storage * actions).dispatch(readOrganizations())
    }

    // Data / I18N
    val texts = storage * i18n * language * component(OrganizationLangComponent.OrganizationManagementPage)
    val buttons = texts * subComp("buttons")
    val dialogs = texts * subComp("dialogs")

    // Permission
    val cannotCreateOrganization = Source { false }



    Page(verticalPageStyle){
        Wrap{ Horizontal(styles = { justifyContent(JustifyContent.FlexStart); alignItems(AlignItems.Center); width(100.percent); gap(20.px) }) {
            H1 { Text((texts * title).emit()) }

            var createOrganizationData by remember{ mutableStateOf(CreateOrganization("")) }
            PlusButton(
                Color.black,
                Color.white,
                (buttons * subComp("createOrganization") * tooltip),
                storage * deviceData * mediaType.get,
                cannotCreateOrganization.emit(),
                "organization-management-page.button.create-organization",
            ) {
                // open "create organization dialog"
                (storage * modals).showCreateOrganizationModal(
                    dialogs * subComp("createOrganization"),
                    storage * deviceData * mediaType.get,
                    styles = {dev -> auctionModalStyles(dev) },
                    setOrganizationData = { name -> createOrganizationData = createOrganizationData.copy(name = name) },
                    cancel = {}
                ) {
                    CoroutineScope(Job()).launch {
                        val action = createOrganization(createOrganizationData.name)
                        trigger(action) on storage
                    }
                }
            }
        } }

        Wrap { ListOfOrganizations(storage) }
    }
}

@Markup
@Composable
@Suppress("FunctionName")
fun ListOfOrganizations(storage: Storage<Application>) {

    // val texts = storage * i18n * language * component(OrganizationLangComponent.OrganizationManagementPage)
    val organizations = user * organizations

    val listStyles = ListStyles()
    ListWrapper {
        // todo:i18n
        TitleWrapper { Title { H2{ Text("Liste der Organisationen") } } }
        HeaderWrapper { Header {
            // todo:i18n
            HeaderCell("Organisation") {width(50.percent) }
        } }

        // Treelike list of organizations
        (storage * organizations).split().forEach { organization ->
            OrganizationItems(
                listStyles,
                organizations,
                organization,
                storage
            )
        }
    }
}

@Markup
@Composable
@Suppress("FunctionName")
fun OrganizationItems(listStyles: ListStyles, organizations: Lens<Application, List<Organization>>, organization: Storage<Organization>, storage: Storage<Application>, deepth: Int = 0) {
    // State
    var opened by remember { mutableStateOf(false) }

    // Data
    val texts = storage * i18n * language * component(OrganizationLangComponent.OrganizationManagementPage)
    val dialogs = texts * subComp("dialogs")

    val organizationId = (organization * organizationId).read()
    val organizationName = (organization * name).read()
    val organizationContextId = organization.read().contextId

    val hasSubOrganizations = (organization * subOrganizations * Reader { list:List<Organization> -> list.isNotEmpty() })

    ListItemWrapper(listStyles.listItemWrapper) {
        DataWrapper() {
            val offset = (deepth * 2.5)
            Div({style { width(offset.percent); color(Color.transparent) }}){ "-" }
            if(hasSubOrganizations.emit() ) {
                SimpleUpDown(opened) { opened = !opened}
            }
            TextCell( organizationName ){
                width((50 - offset).percent)
            }
        }
        ActionsWrapper {
            var createChildOrganization by remember{ mutableStateOf(
                CreateChildOrganization(
                    organizationId,
                    ""
                    //organizationName
                )
            )}
            PlusButton(
                Color.black,
                Color.white,
                { "Sub-Organisation anlegen" },
                storage * deviceData * mediaType.get,
                (storage * isNotGranted(OrganizationRight.Organization.create, organizationContextId )).emit(),
                "organization-management-page.organization-list.button.create-child-organization",
            ) {
                // open "create organization dialog"
                (storage * modals).showCreateChildOrganizationModal(
                    dialogs * subComp("createOrganization"),
                    storage * deviceData * mediaType.get,
                    styles = {dev -> auctionModalStyles(dev) },
                    setOrganizationData = { name -> createChildOrganization = createChildOrganization.copy(name = name) },
                    cancel = {}
                ) {
                    CoroutineScope(Job()).launch {
                        val action = createChildOrganization(
                            createChildOrganization.name,
                            organizations * FirstBy { it.organizationId == organizationId },
                        )
                        trigger(action) on storage
                    }
                }
            }

            DetailsButton(
                Color.black,
                Color.white,
                // todo:i18n
                {"Details"},
                storage * deviceData * mediaType.get,
                (storage * isNotGranted(OrganizationRight.Organization.read, organizationContextId, )).emit(),
            ) {

                console.log("Details button clicked")
            }

            var updateOrganization by remember{ mutableStateOf(
                UpdateOrganization(
                    organizationId,
                    organizationName
                )
            )}
            EditButton(
                    Color.black,
            Color.white,
                // todo:i18n
            {"Bearbeiten"},
            storage * deviceData * mediaType.get,
                (storage * isNotGranted(OrganizationRight.Organization.update, organizationContextId, )).emit(),
            ) {

                (storage * modals).showUpdateOrganizationModal(
                    dialogs * subComp("updateOrganization"),
                    storage * deviceData * mediaType.get,
                    styles = {dev -> auctionModalStyles(dev) },
                    organization.read(),
                    setOrganizationData = { name -> updateOrganization = updateOrganization.copy(name = name) },
                    cancel = {}
                ) {
                    CoroutineScope(Job()).launch {
                        val action = updateOrganization(
                            updateOrganization.name,
                            organizations * FirstBy { it.organizationId == organizationId }
                        )
                        trigger(action) on storage
                    }
                }
            }
            TrashCanButton(
                Color.black,
                Color.white,
                // todo:i18n
                {"Löschen"},
                storage * deviceData * mediaType.get,
                (storage * isNotGranted(OrganizationRight.Organization.delete, organizationContextId, )).emit(),
            ) {
                CoroutineScope(Job()).launch {
                    val action = deleteOrganization(organizationId)
                    trigger(action) on storage
                }
            }
        }
    }
    if(!opened) return
    (organization * subOrganizations).split().forEach {
        subOrg -> OrganizationItems(
        listStyles,
        organizations * FirstBy { o -> o.organizationId == organizationId } * subOrganizations,
        subOrg,
        storage,
        deepth + 1
        )
    }
}

@Markup
@Composable
@Suppress("FunctionName")
fun SimpleUpDown(open: Boolean, toggle: () -> Unit) =  Span({
    onClick { toggle() }
    style {
        width(1.5.em)
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.Center)
        alignItems(AlignItems.Center)
        paddingRight(5.px)
    }
}) {
    val icon = if(open) "fa-chevron-down" else "fa-chevron-right"
    I({classes("fa-solid", icon)}){}
}
