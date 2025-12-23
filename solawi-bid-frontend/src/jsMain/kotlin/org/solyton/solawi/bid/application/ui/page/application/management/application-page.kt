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
import org.evoleq.compose.guard.data.withLoading
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.language.component
import org.evoleq.language.description
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.language.tooltip
import org.evoleq.math.emit
import org.evoleq.math.map
import org.evoleq.math.times
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.prism.Either
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.firstByOrNull
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
import org.solyton.solawi.bid.module.application.action.readApplicationContextRelations
import org.solyton.solawi.bid.module.application.action.readApplications
import org.solyton.solawi.bid.module.application.data.application.modules
import org.solyton.solawi.bid.module.application.data.management.availableApplications
import org.solyton.solawi.bid.module.application.data.management.personalApplicationContextRelations
import org.solyton.solawi.bid.module.application.i18n.Component
import org.solyton.solawi.bid.module.application.i18n.Component.editContext
import org.solyton.solawi.bid.module.application.i18n.application
import org.solyton.solawi.bid.module.application.i18n.module
import org.solyton.solawi.bid.module.control.button.ArrowUpButton
import org.solyton.solawi.bid.module.control.button.DetailsButton
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.guard.onMissing
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.defaultListStyles
import org.solyton.solawi.bid.module.loading.component.Loading
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.permissions.data.contexts
import org.solyton.solawi.bid.module.permissions.service.contextFromPath
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.SubTitle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.action.organization.readOrganizations
import org.solyton.solawi.bid.module.user.action.permission.readUserPermissionsAction
import org.solyton.solawi.bid.module.application.data.management.actions as applicationManagementActions
import org.solyton.solawi.bid.module.user.data.actions as userActions


@Markup
@Composable
@Suppress("FunctionName")
fun ApplicationPage(storage: Storage<Application>, applicationId: String) = withLoading(
    isLoading = isLoading(
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
            ApplicationLangComponent.ApplicationPage,
            storage * i18N.get
        ) {
            LaunchComponentLookup(
                langComponent = ApplicationLangComponent.ApplicationPage,
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
    onLoading = {Loading()}
){
    /*
    LaunchedEffect(Unit) {
        launch {
            (storage * userIso * userActions).dispatch(readOrganizations())
        }
    }
    
     */

    // Data
    val device = storage * deviceData * mediaType.get

    val application = storage * applicationManagementModule * availableApplications * FirstBy { it.id == applicationId }

    val contextRelations = (storage * applicationManagementModule * personalApplicationContextRelations).read()
    val defaultContextId = contextRelations.first{ relation -> relation.relatedId == applicationId }.contextId
    val defaultContextPrism = (storage * availablePermissions * contexts).firstByOrNull()
    val defaultContext = defaultContextPrism.match{ it.contextId == defaultContextId }

    // Texts
    val applicationTexts = storage * i18N * language * subComp(BASE_PATH)
    val texts = storage * i18N * language * component(ApplicationLangComponent.ApplicationPage)
    val pageTitle = texts * title
    val allApps = texts * Component.actions * Component.navToAppManPage
    val listOfModules = texts * Component.listOfModules
    val defaultContextTexts = texts * Component.defaultContext

    Page({verticalPageStyle() }) {
        Wrap {
            Horizontal(styles = {
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                width(100.percent)
            }) {
                PageTitle( pageTitle map{ "$it: ${(applicationTexts * application(application.read().name) * title).emit()}"})
                Horizontal {
                    ArrowUpButton(
                        Color.black,
                        Color.white,
                        allApps * tooltip,
                        device,
                        false,
                    ) {
                        navigate("/app/management")
                    }
                }
            }
            SubTitle(applicationTexts * application(application.read().name) * description)
        }

        ListWrapper{
            TitleWrapper {
                Title { H3{ Text((listOfModules * title).emit()) }}
            }
            HeaderWrapper {
                Header {
                    HeaderCell(listOfModules * Component.headers * Component.module * title) { width(40.percent) }
                }
            }
            ListItems(application * modules) { module ->
                ListItemWrapper {
                    DataWrapper {
                        TextCell(
                            applicationTexts *
                            module(application.read().name, module.name) *
                            title
                        ) { width(40.percent) }

                    }
                    ActionsWrapper({
                        defaultListStyles.actionsWrapper(this)
                        alignSelf(AlignSelf.FlexStart)
                    }) {
                        DetailsButton(
                            Color.black,
                            Color.white,
                            listOfModules * Component.actions * Component.showDetails * tooltip,
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

        ListWrapper {
            TitleWrapper({
                defaultListStyles.titleWrapper(this)
                justifyContent(JustifyContent.SpaceBetween)
            }) {
                Title { H3{ Text((defaultContextTexts * title).emit()) } }
                Horizontal {
                    EditButton(
                        Color.black,
                        Color.white,
                        defaultContextTexts * Component.actions * editContext * tooltip,
                        device,
                    ) {}
                }
            }
            HeaderWrapper {
                Header {
                    HeaderCell(defaultContextTexts * Component.headers * Component.role * title) { width(40.percent) }
                    HeaderCell(defaultContextTexts * Component.headers * Component.rights * title) { width(40.percent) }
                }
            }
            if(defaultContext is Either.Right) {
                ListItems(defaultContext.value.roles) { role ->
                    ListItemWrapper {
                        DataWrapper {
                            TextCell(role.roleName) { width(40.percent) }
                            Div({style {
                                display(DisplayStyle.Flex)
                                flexDirection(FlexDirection.Row)
                                flexWrap(FlexWrap.Wrap)
                                width(60.percent)
                                flexShrink(0)
                            }}) { role.rights.forEach { right ->
                                TextCell(right.rightName,right.rightName){
                                    flexWrap(FlexWrap.Wrap)
                                    width(31.percent)
                                    padding(1.percent)
                                    flexShrink(0)
                                    overflow("hidden")
                                }
                            } }
                        }
                        ActionsWrapper({
                            defaultListStyles.actionsWrapper(this)
                            alignSelf(AlignSelf.FlexStart)
                        }) {
                            EditButton(
                                Color.black,
                                Color.white,
                                defaultContextTexts * Component.actions * Component.editRole * tooltip,
                                device,
                            ) {}
                        }
                    }
                }
            } else {
                TextCell("No default context available") { width(100.percent); color(Color.red) }
            }
        }
    }
}
