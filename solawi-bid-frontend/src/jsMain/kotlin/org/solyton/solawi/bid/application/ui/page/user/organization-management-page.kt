package org.solyton.solawi.bid.application.ui.page.user

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.guard.data.isLoading
import org.evoleq.compose.layout.Horizontal
import org.evoleq.device.data.mediaType
import org.evoleq.language.component
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.language.tooltip
import org.evoleq.math.Source
import org.evoleq.math.assureValue
import org.evoleq.math.emit
import org.evoleq.math.on
import org.evoleq.math.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.user.action.createUser
import org.solyton.solawi.bid.application.ui.page.user.effect.trigger
import org.solyton.solawi.bid.application.ui.page.user.i18n.OrganizationLangComponent
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.control.button.PlusButton
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.guard.onMissing
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.permissions.data.contextId
import org.solyton.solawi.bid.module.permissions.service.contextFromPath
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.user.action.organization.createOrganization
import org.solyton.solawi.bid.module.user.action.organization.readOrganizations
import org.solyton.solawi.bid.module.user.component.modal.showCreateOrganizationModal
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.actions
import org.solyton.solawi.bid.module.user.data.api.organization.CreateOrganization
import org.solyton.solawi.bid.module.user.data.deviceData
import org.solyton.solawi.bid.module.user.data.environment
import org.solyton.solawi.bid.module.user.data.i18n
import org.solyton.solawi.bid.module.user.data.modals
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.organizations

@Markup
@Composable
@Suppress("FunctionName")
fun OrganizationManagementPage(storage: Storage<Application>) = Div {
    LaunchedEffect(Unit) {
        (storage * actions).dispatch(readOrganizations())
    }
    // Effects
    if(isLoading(
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


    // Data

    // State
    var createOrganizationData by remember{ mutableStateOf(CreateOrganization("")) }

    // val environment = storage * environment
    // val applicationContextId = storage * availablePermissions * contextFromPath("APPLICATION") * assureValue() * contextId.get
    val organizations = storage * user * organizations
    // Data / I18N
    val texts = storage * i18n * language * component(OrganizationLangComponent.OrganizationManagementPage)
    val buttons = texts * subComp("buttons")
    val dialogs = texts * subComp("dialogs")

    // Permission
    val cannotCreateOrganization = Source { false }



    Page(verticalPageStyle){
        Horizontal(styles = { justifyContent(JustifyContent.FlexStart); alignItems(AlignItems.Center); width(100.percent); gap(20.px) }) {
            H1 { Text((texts * title).emit()) }

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
        }

        // Treelike list of organizations
        organizations.read().forEach { organization ->
            Text(organization.name + " --- ")
        }
    }
}
