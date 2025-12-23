package org.solyton.solawi.bid.application.ui.page.application.management

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.guard.data.isLoading
import org.evoleq.compose.guard.data.onNullLaunch
import org.evoleq.compose.guard.data.withLoading
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.language.*
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.env.i18nEnvironment
import org.solyton.solawi.bid.application.data.transform.application.management.applicationManagementModule
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.application.i18n.ApplicationLangComponent
import org.solyton.solawi.bid.application.ui.page.application.i18n.BASE_PATH
import org.solyton.solawi.bid.module.application.data.management.availableApplications
import org.solyton.solawi.bid.module.application.i18n.Component
import org.solyton.solawi.bid.module.application.i18n.application
import org.solyton.solawi.bid.module.application.i18n.module
import org.solyton.solawi.bid.module.control.button.DetailsButton
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.dialog.component.showDialogModal
import org.solyton.solawi.bid.module.dialog.i18n.dialogModalTexts
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.guard.onMissing
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.defaultListStyles
import org.solyton.solawi.bid.module.loading.component.Loading
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.permissions.service.contextFromPath
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.SubTitle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.action.organization.readOrganizations
import org.solyton.solawi.bid.module.user.action.permission.readUserPermissionsAction
import org.solyton.solawi.bid.module.application.data.management.applicationManagementModals
import org.solyton.solawi.bid.module.user.data.userActions


@Markup
@Composable
@Suppress("FunctionName")
fun ApplicationManagementPage(storage: Storage<Application>) = withLoading(
    isLoading = isLoading(
        onNullLaunch(
            storage * availablePermissions * contextFromPath("APPLICATION"),
        ) {
            CoroutineScope(Job()).launch { (storage * userIso * userActions).dispatch(readUserPermissionsAction()) }
        },
        onMissing(
            ApplicationLangComponent.ApplicationManagementPage,
            storage * i18N.get
        ) {
            LaunchComponentLookup(
                langComponent = ApplicationLangComponent.ApplicationManagementPage,
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
    onLoading = {
        Loading()
    }
){
    LaunchedEffect(Unit) {
        launch {
            (storage * userIso * userActions).dispatch(readOrganizations())
        }
    }
    // Data
    val device = storage * deviceData * mediaType.get
    val availableApplications = storage * applicationManagementModule * availableApplications
    val modals = storage * applicationManagementModule * applicationManagementModals

    // Texts
    val applicationTexts = storage * i18N * language * subComp(BASE_PATH)
    val texts = storage * i18N * language * component(ApplicationLangComponent.ApplicationManagementPage)
    val pageTitle = texts * title
    val subTitle = texts * subTitle
    val applicationList = texts * subComp("listOfApplications")
    val applicationListHeaders = applicationList * subComp("headers")
    val applicationListActions = applicationList * subComp("actions")

    Page(verticalPageStyle) {
        Wrap {
            PageTitle(pageTitle)
            SubTitle(subTitle)
        }
        ListWrapper {
            TitleWrapper {
                Title { H3 { Text((applicationList * title).emit()) } }
            }
            HeaderWrapper {
                Header {
                    HeaderCell(applicationListHeaders * Component.application * title) { width(40.percent) }
                    HeaderCell(applicationListHeaders * Component.modules * title) { width(40.percent) }
                }
            }
            ListItems(availableApplications) { application ->
                ListItemWrapper {
                    DataWrapper {
                        TextCell(applicationTexts * application(application.name) * title) { width(40.percent) }
                        Div(attrs = {
                            style {
                                width(40.percent)
                                display(DisplayStyle.Flex)
                                flexDirection(FlexDirection.Column)
                            }
                        }) {
                            application.modules.forEach { module ->
                                Div(attrs = {
                                    style {
                                        width(100.percent)
                                        cursor("pointer")
                                    }
                                    onClick {
                                        CoroutineScope(Job()).launch {
                                            navigate("/app/management/application/${application.id}/module/${module.id}")
                                        }
                                    }
                                }) {
                                    Text((applicationTexts * module(application.name, module.name) * title).emit())
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
                            applicationListActions * Component.showDetails * tooltip,
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
                            applicationListActions * Component.edit * tooltip,
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
