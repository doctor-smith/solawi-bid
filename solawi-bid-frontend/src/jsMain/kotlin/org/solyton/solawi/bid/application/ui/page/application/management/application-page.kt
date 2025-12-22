package org.solyton.solawi.bid.application.ui.page.application.management


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.guard.data.isLoading
import org.evoleq.compose.guard.data.onEmpty
import org.evoleq.compose.guard.data.onNullLaunch
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.language.component
import org.evoleq.language.subComp
import org.evoleq.math.times
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.prism.Either
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.firstByOrNull
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.H4
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.letsPlot.commons.xml.Xml
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.env.i18nEnvironment
import org.solyton.solawi.bid.application.data.transform.application.management.applicationManagementModule
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.application.i18n.ApplicationLangComponent
import org.solyton.solawi.bid.module.application.action.readApplicationContextRelations
import org.solyton.solawi.bid.module.application.action.readApplications
import org.solyton.solawi.bid.module.application.data.management.availableApplications
import org.solyton.solawi.bid.module.application.data.management.personalApplicationContextRelations
import org.solyton.solawi.bid.module.control.button.DetailsButton
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.dialog.component.showDialogModal
import org.solyton.solawi.bid.module.dialog.i18n.dialogModalTexts
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.guard.onMissing
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.defaultListStyles
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.permissions.data.contexts
import org.solyton.solawi.bid.module.permissions.service.contextFromPath
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.user.action.organization.readOrganizations
import org.solyton.solawi.bid.module.user.action.permission.readUserPermissionsAction
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.organizations
import org.solyton.solawi.bid.module.user.data.user.permissions
import org.solyton.solawi.bid.module.application.data.management.modals as applicationManagementModals
import org.solyton.solawi.bid.module.application.data.management.actions as applicationManagementActions
import org.solyton.solawi.bid.module.user.data.actions as userActions


@Markup
@Composable
@Suppress("FunctionName")
fun ApplicationPage(storage: Storage<Application>, applicationId: String) = Div {

    if (isLoading(
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
            onNullLaunch(
                storage * availablePermissions * contextFromPath("APPLICATION"),
            ) {
                CoroutineScope(Job()).launch { (storage * userIso * userActions).dispatch(readUserPermissionsAction()) }
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
        )
    ) return@Div


    LaunchedEffect(Unit) {
        launch {
            (storage * userIso * userActions).dispatch(readOrganizations())
        }
    }

    val device = storage * deviceData * mediaType.get
/*
    val texts = storage * i18N * language * component(ApplicationLangComponent.PrivateApplicationManagementPage)

    val organizations = storage * userIso * user * organizations
    val modals = storage * applicationManagementModule * applicationManagementModals
*/
    val application = storage * applicationManagementModule * availableApplications * FirstBy { it.id == applicationId }

    val contextRelations = (storage * applicationManagementModule * personalApplicationContextRelations).read()
    val defaultContextId = contextRelations.first{ relation -> relation.relatedId == applicationId }.contextId
    val defaultContextPrism = (storage * availablePermissions * contexts).firstByOrNull()
    val defaultContext = defaultContextPrism.match{ it.contextId == defaultContextId }

    Page({verticalPageStyle() }) {
        H1{ Text(application.read().name) }

        H3{ Text("Default Context") }

        ListWrapper {
            TitleWrapper {
                Title { H4{ Text("Roles and Rights") } }
            }
            HeaderWrapper {
                Header {
                    HeaderCell("Role") { width(40.percent) }
                    HeaderCell("Rights") { width(40.percent) }
                }
            }
            if(defaultContext is Either.Right) {
                defaultContext.value.roles.forEach { role ->
                    ListItemWrapper {
                        DataWrapper {
                            TextCell(role.roleName) { width(40.percent) }
                            Div({
                                style {
                                    display(DisplayStyle.Flex)
                                    flexDirection(FlexDirection.Column)
                                    width(40.percent)
                                }
                            }) {
                                role.rights.forEach { right ->
                                    TextCell(right.rightName) { width(100.percent) }
                                }
                            }
                        }
                    }
                }
            } else {
                TextCell("No default context available") { width(100.percent); color(Color.red) }
            }
        }



        ListWrapper {
            TitleWrapper {
                Title { H3{ Text("Modules") }}
            }
            HeaderWrapper {
                Header {
                    TextCell("Module") { width(40.percent) }
                    TextCell("Actions") { width(40.percent) }
                }
            }
            application.read().modules.forEach { module ->
                ListItemWrapper {
                    DataWrapper {
                        TextCell(module.name) { width(40.percent) }

                    }
                    ActionsWrapper({
                        defaultListStyles.actionsWrapper(this)
                        alignSelf(AlignSelf.FlexStart)
                    }) {
                        DetailsButton(
                            Color.black,
                            Color.white,
                            { "Edit Module" },
                            device,
                            ) {
                            CoroutineScope(Job()).launch {
                                navigate(
                                    "/app/management/application/${applicationId}/module/${module.id}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
