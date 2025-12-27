package org.solyton.solawi.bid.application.ui.page.user

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.guard.data.isLoading
import org.evoleq.compose.guard.data.onEmpty
import org.evoleq.compose.guard.data.withLoading
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.language.component
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.language.tooltip
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.lens.DeepSearch
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.env.i18nEnvironment
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.user.i18n.OrganizationLangComponent
import org.solyton.solawi.bid.module.control.button.ArrowUpButton
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.guard.onMissing
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.defaultListStyles
import org.solyton.solawi.bid.module.loading.component.Loading
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.action.organization.readOrganizations
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.module.user.data.deviceData
import org.solyton.solawi.bid.module.user.data.environment
import org.solyton.solawi.bid.module.user.data.i18n
import org.solyton.solawi.bid.module.user.data.organization.members
import org.solyton.solawi.bid.module.user.data.organization.name
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.organizations
import org.solyton.solawi.bid.module.user.data.userActions
import org.solyton.solawi.bid.module.user.i18n.Component


@Markup
@Composable
@Suppress("FunctionName")
fun OrganizationPage(storage: Storage<Application>, organizationId: String) = withLoading(
    isLoading = isLoading(
        onMissing(
            OrganizationLangComponent.OrganizationPage,
            storage * i18n.get
        ) {
            LaunchComponentLookup(
                langComponent = OrganizationLangComponent.OrganizationPage,
                environment = storage * environment.get,
                i18n = (storage * i18n)
            )
        },
        onEmpty(
            storage * user * organizations.get
        ){
            LaunchedEffect(Unit) {
                launch {
                    (storage * userActions).dispatch(readOrganizations())
                }
            }
        },

    ),
    onLoading = { Loading() }
) {
    val device = storage * deviceData * mediaType

    val organization = storage * user * organizations * DeepSearch { it.organizationId == organizationId }
    val members = organization * members

    // texts
    val texts = storage * i18n * language * component(OrganizationLangComponent.OrganizationPage)
    val listOfMembers = texts * subComp("listOfMembers")
    val listOfMembersHeaders = texts * subComp("listOfMembers") * subComp("headers")


    Page(verticalPageStyle) {
        Wrap {
            Horizontal({
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
            }) {
                PageTitle(organization * name.get)
                Horizontal {
                    ArrowUpButton(
                        Color.black,
                        Color.white,
                        texts * subComp("actions") * subComp("navToManagementPage") * tooltip,
                        { device.read() }
                    ) {
                        navigate("/app/management/organizations")
                    }
                }
            }
        }
        /*
        ListWrapper {
            TitleWrapper { Title {  H3{ Text("Applications") }} }
            HeaderWrapper {
                Header {
                    HeaderCell("Name") { width(40.percent) }
                    HeaderCell("Modules") { width(40.percent) }
                }
            }

        }

         */

        ListWrapper {
            TitleWrapper { Title { H3{ Text((listOfMembers * title).emit()) }}}
            HeaderWrapper { Header {
                HeaderCell(listOfMembersHeaders * Component.standard * title){width(30.percent)}
                HeaderCell(listOfMembersHeaders * Component.userProfile * title){width(30.percent)}
                HeaderCell("Solawi Anteile | Status"){width(40.percent)}
            } }
            HeaderWrapper { Header{
                HeaderCell(listOfMembersHeaders * Component.standard * Component.username * title) {
                    width(10.percent);  overflow("hidden")
                }
                HeaderCell(listOfMembersHeaders * Component.standard * Component.roles * title){width(20.percent)}
                HeaderCell(listOfMembersHeaders * Component.userProfile * Component.name * title){width(10.percent)}
                HeaderCell(listOfMembersHeaders * Component.userProfile * Component.address * title){width(20.percent)}
                HeaderCell("Gemüse"){width(10.percent)}
                HeaderCell("Eier"){width(10.percent)}
                HeaderCell("Obst"){width(10.percent)}
                HeaderCell("Status"){width(10.percent)}

            } }
            ListItems(members) { member ->
                ListItemWrapper {
                    DataWrapper{
                        TextCell(member.username){
                            width(10.percent); minWidth(10.percent); overflow("hidden")
                        }
                        TextCell(member.roles.joinToString(", "){it.roleName}){
                            width(20.percent);minWidth(10.percent); overflow("hidden")
                        }
                        TextCell("Vorname + Nachname"){
                            width(10.percent);minWidth(10.percent);overflow("hidden")
                        }
                        TextCell("Adam-Müller-Guttenbrunn-Str. 5, 72827 Wannweil"){
                            width(20.percent); minWidth(20.percent);overflow("hidden")
                        }
                        NumberCell(1){width(10.percent)}
                        NumberCell(0){width(10.percent)}
                        NumberCell(1){width(10.percent)}
                        TextCell("Aktiv"){width(10.percent)}
                    }
                    ActionsWrapper({
                        defaultListStyles.actionsWrapper(this)
                        alignSelf(AlignSelf.FlexStart)
                    }) {
                        EditButton(
                            Color.black,
                            Color.white,
                            listOfMembers * Component.actions * Component.edit * tooltip,
                            {device.read()}
                        ) {
                            navigate("/app/management/user/${member.username}")
                        }
                    }
                }
            }
        }

    }
}
