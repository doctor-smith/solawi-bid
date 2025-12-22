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
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.language.component
import org.evoleq.language.title
import org.evoleq.language.tooltip
import org.evoleq.math.emit
import org.evoleq.math.map
import org.evoleq.math.times
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.times
import org.evoleq.optics.prism.Either
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.firstByOrNull
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.H4
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.env.i18nEnvironment
import org.solyton.solawi.bid.application.data.transform.application.management.applicationManagementModule
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.application.i18n.ApplicationLangComponent
import org.solyton.solawi.bid.module.application.action.readApplicationContextRelations
import org.solyton.solawi.bid.module.application.action.readApplications
import org.solyton.solawi.bid.module.application.action.readModuleContextRelations
import org.solyton.solawi.bid.module.application.data.application.modules
import org.solyton.solawi.bid.module.application.data.application.name
import org.solyton.solawi.bid.module.application.data.management.availableApplications
import org.solyton.solawi.bid.module.application.data.management.personalModuleContextRelations
import org.solyton.solawi.bid.module.application.data.management.personalApplicationContextRelations
import org.solyton.solawi.bid.module.application.i18n.Component
import org.solyton.solawi.bid.module.application.i18n.Component.editContext
import org.solyton.solawi.bid.module.control.button.ArrowUpButton
import org.solyton.solawi.bid.module.application.data.application.Application as App
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
fun ModulePage(storage: Storage<Application>, applicationId: String, moduleId: String) = Div { when {
    isLoading(
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
        onEmpty(storage * applicationManagementModule * personalModuleContextRelations.get) {
            CoroutineScope(Job()).launch {
                (storage * applicationManagementModule * applicationManagementActions).dispatch(
                    readModuleContextRelations
                )
            }
        },
        onNullLaunch(
            storage * availablePermissions * contextFromPath("APPLICATION"),
        ) {
            CoroutineScope(Job()).launch { (storage * userIso * userActions).dispatch(readUserPermissionsAction()) }
        },
        onMissing(
            ApplicationLangComponent.ModulePage,
            storage * i18N.get
        ) {
            LaunchComponentLookup(
                langComponent = ApplicationLangComponent.ModulePage,
                environment = storage * environment * i18nEnvironment,
                i18n = (storage * i18N)
            )
        }
    ) -> Loading()
    else -> {
        LaunchedEffect(Unit) {
            launch {
                (storage * userIso * userActions).dispatch(readOrganizations())
            }
        }

        val device = storage * deviceData * mediaType.get

        val texts = storage * i18N * language * component(ApplicationLangComponent.ModulePage)
        val pageTitle = texts * title
        val parentApp = texts * Component.actions * Component.navToParentApplication
        val defaultContextTexts = texts * Component.defaultContext
        /*
    val organizations = storage * userIso * user * organizations
    val modals = storage * applicationManagementModule * applicationManagementModals
    */
        val managementStorage = storage * applicationManagementModule
        val application = availableApplications * FirstBy<App> { it.id == applicationId }

        val module = application * modules * FirstBy { it.id == moduleId }

        val appContextRelations =
            (storage * applicationManagementModule * personalApplicationContextRelations).read()
        val defaultAppContextId =
            appContextRelations.first { relation -> relation.relatedId == applicationId }.contextId

        val contextRelations = (storage * applicationManagementModule * personalModuleContextRelations).read()
        val defaultModuleContextId = contextRelations.first { relation -> relation.relatedId == moduleId }.contextId


        val defaultAppContextPrism = (storage * availablePermissions * contexts).firstByOrNull()
        val defaultAppContext = defaultAppContextPrism.match { it.contextId == defaultAppContextId }
        require(defaultAppContext is Either.Right) { "No default app context available" }
        val defaultModuleContext =
            defaultAppContext.value.children.firstOrNull { it.contextId == defaultModuleContextId }

        Page({ verticalPageStyle() }) {
            Wrap {
                Horizontal(styles = {
                    justifyContent(JustifyContent.SpaceBetween)
                    alignItems(AlignItems.Center)
                    width(100.percent)
                }) {
                    PageTitle(pageTitle map { "$it ${(managementStorage * module).read().name}" })
                    Horizontal {
                        ArrowUpButton(
                            Color.black,
                            Color.white,
                            parentApp map { "$it ${(managementStorage * application * name).read()}" },
                            device,
                        ) {
                            navigate("/app/management/application/$applicationId")
                        }
                    }
                }
                SubTitle("Module Description ...")
            }



            ListWrapper {
                TitleWrapper({
                    defaultListStyles.titleWrapper(this)
                    justifyContent(JustifyContent.SpaceBetween)
                }) {
                    Title { H3 { Text((defaultContextTexts * title).emit()) } }
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
                ListItems(defaultModuleContext?.roles ?: emptyList()) { role ->
                    ListItemWrapper {
                        DataWrapper {
                            TextCell(role.roleName) { width(40.percent) }
                            Div({
                                style {
                                    display(DisplayStyle.Flex)
                                    flexDirection(FlexDirection.Row)
                                    flexWrap(FlexWrap.Wrap)
                                    width(60.percent)
                                    flexShrink(0)
                                }
                            }) {
                                role.rights.forEach { right ->
                                    TextCell(right.rightName, right.rightName) {
                                        flexWrap(FlexWrap.Wrap)
                                        width(31.percent)
                                        padding(1.percent)
                                        flexShrink(0)
                                        overflow("hidden")
                                    }
                                }
                            }
                        }
                        ActionsWrapper({
                            defaultListStyles.actionsWrapper(this)
                            alignSelf(AlignSelf.FlexStart)
                        }) {
                            EditButton(
                                Color.black,
                                Color.white,
                                defaultContextTexts * Component.actions * editContext * tooltip,
                                device,
                            ) {

                            }
                        }
                    }
                }
            }
        }
    }}
}

