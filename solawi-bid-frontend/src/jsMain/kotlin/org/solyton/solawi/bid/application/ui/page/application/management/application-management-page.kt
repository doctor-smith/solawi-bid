package org.solyton.solawi.bid.application.ui.page.application.management

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.guard.data.isLoading
import org.evoleq.compose.guard.data.onNullLaunch
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.language.component
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.env.i18nEnvironment
import org.solyton.solawi.bid.application.data.transform.application.management.applicationManagementModule
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.application.i18n.ApplicationLangComponent
import org.solyton.solawi.bid.module.application.data.management.availableApplications
import org.solyton.solawi.bid.module.control.button.DetailsButton
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.dialog.component.showDialogModal
import org.solyton.solawi.bid.module.dialog.i18n.dialogModalTexts
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.guard.onMissing
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.defaultListStyles
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.permissions.service.contextFromPath
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.user.action.organization.readOrganizations
import org.solyton.solawi.bid.module.user.action.permission.readUserPermissionsAction
import org.solyton.solawi.bid.module.application.data.management.modals as applicationManagementModals
import org.solyton.solawi.bid.module.user.data.actions as userActions


@Markup
@Composable
@Suppress("FunctionName")
fun ApplicationManagementPage(storage: Storage<Application>) = Div {

    if (isLoading(
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

    val texts = storage * i18N * language * component(ApplicationLangComponent.PrivateApplicationManagementPage)

    val availableApplications = storage * applicationManagementModule * availableApplications
    val modals = storage * applicationManagementModule * applicationManagementModals
    Page(verticalPageStyle) {
        H1{ Text("Application Management") }
        ListWrapper {
            TitleWrapper {
                H3{ Title { Text("Applications") } }
            }
            HeaderWrapper {
                Header {
                    HeaderCell("Application") { width(40.percent) }
                    HeaderCell("Modules") { width(40.percent) }
                }
            }
            availableApplications.read().forEach { application ->
                ListItemWrapper {
                    DataWrapper {
                        TextCell(application.name) { width(40.percent) }
                        Div(attrs = {
                            style {
                                width(40.percent)
                                display(DisplayStyle.Flex)
                                flexDirection(FlexDirection.Column)
                            }
                        }) {
                            application.modules.forEach { module ->
                                Div(attrs = {
                                    style { width(100.percent) }
                                }) {
                                    Text(module.name)
                                }
                            }
                        }
                    }
                    ActionsWrapper({
                        defaultListStyles.actionsWrapper(this)
                        alignSelf(AlignSelf.FlexStart)
                    }) {
                        DetailsButton(
                            Color.black,
                            Color.white,
                            { "Show Application Details" },
                            device,
                        ) {
                            CoroutineScope(Job()).launch {
                                navigate(
                                    "/app/management/application/${application.id}",
                                )
                            }
                        }
                        EditButton(
                            Color.black,
                            Color.white,
                            { "Edit Application" },
                            device,

                            ) {
                            modals.showDialogModal(
                                texts = dialogModalTexts("Not Implemented"),
                                device = device,
                                dataId = "application-management.page.edit-application.not-implemented",
                            ) {
                                CoroutineScope(Job()).launch {}
                            }
                        }
                    }
                }
            }
        }
    }
}
