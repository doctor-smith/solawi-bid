package org.solyton.solawi.bid.application.ui.page.application.private

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.guard.data.isLoading
import org.evoleq.compose.guard.data.onNullLaunch
import org.evoleq.device.data.mediaType
import org.evoleq.language.component
import org.evoleq.language.subComp
import org.evoleq.math.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.env.i18nEnvironment
import org.solyton.solawi.bid.application.data.transform.application.import
import org.solyton.solawi.bid.application.data.transform.application.management.applicationManagementModule
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.application.i18n.ApplicationLangComponent
import org.solyton.solawi.bid.module.application.action.connectApplicationToOrganization
import org.solyton.solawi.bid.module.application.component.modal.showConnectApplicationToOrganizationModule
import org.solyton.solawi.bid.module.application.data.LifecycleStage
import org.solyton.solawi.bid.module.application.data.management.actions
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.guard.onMissing
import org.solyton.solawi.bid.module.permissions.service.contextFromPath
import org.solyton.solawi.bid.module.user.action.organization.readOrganizations
import org.solyton.solawi.bid.module.user.action.permission.readUserPermissionsAction
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.organizations
import org.solyton.solawi.bid.module.application.data.management.applicationManagementModals
import org.solyton.solawi.bid.module.user.data.userActions

@Markup
@Composable
@Suppress("FunctionName")
fun PrivateApplicationManagementPage(storage: Storage<Application>) = Div {

    if(isLoading(
        onNullLaunch(
            storage * availablePermissions * contextFromPath("APPLICATION"),
        ){
            CoroutineScope(Job()).launch { (storage * userIso * userActions ).dispatch(readUserPermissionsAction()) }
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
        }
    )) return@Div

    LaunchedEffect(Unit) {
        launch {
            (storage * userIso * userActions).dispatch(readOrganizations())
        }
    }

    val device = storage * deviceData * mediaType.get

    val texts = storage * i18N * language * component(ApplicationLangComponent.PrivateApplicationManagementPage)
    val connectDialogTexts = texts * subComp("dialogs") * subComp("connectApplicationToOrganization")

    val personalApplications = storage * personalApplications
    val organizations = storage * userIso * user * organizations
    val modals = storage * applicationManagementModule * applicationManagementModals
    Ul {
        var organizationId by remember { mutableStateOf("") }
        personalApplications.read().forEach { application ->

            Li(attrs = {

                onClick {
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
            }) { Text(application.name) }
        }
    }
}
