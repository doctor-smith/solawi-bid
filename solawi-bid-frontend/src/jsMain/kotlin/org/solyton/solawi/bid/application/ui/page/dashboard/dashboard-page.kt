package org.solyton.solawi.bid.application.ui.page.dashboard

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Vertical
import org.evoleq.compose.link.Link
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.language.Lang
import org.evoleq.language.component
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.deviceData
import org.solyton.solawi.bid.application.data.i18N
import org.solyton.solawi.bid.application.data.transform.application.management.applicationManagementModule
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.service.setContext
import org.solyton.solawi.bid.application.ui.page.dashboard.data.DashboardComponent
import org.solyton.solawi.bid.application.ui.page.dashboard.permissions.canAccessApplication
import org.solyton.solawi.bid.module.application.permission.Context
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.data.reader.hasOneOfTheRoles
import org.w3c.dom.HTMLElement

@Markup
@Composable
@Suppress("FunctionName")
fun DashboardPage(storage: Storage<Application>) {
    storage.setContext(Context.Application)
    // Effects

    // Data

    // Permissions
    val canAccessApplicationManagement = (storage * applicationManagementModule * canAccessApplication("APPLICATION_MANAGEMENT")).emit()
    val canAccessAuctions = (storage * applicationManagementModule * canAccessApplication("AUCTIONS")).emit()
    // Texts
    val texts = (storage * i18N * language * component(DashboardComponent.Page))
    // val auctionsCard = texts * subComp("auctionsCard")

    Vertical(verticalPageStyle) {
        Wrap{
            H1 { Text((texts * title).emit()) }
        }
        Horizontal(/*{justifyContent(JustifyContent.SpaceEvenly)}*/) {
            if(canAccessApplicationManagement) {
                Card({
                    navigate("/app/management")
                }) {
                    Wrap { H3 { Text("Application Management") } }

                    Link("My Apps", "/app/management/private")
                    Link("User Management", "/app/management/users")
                    Link("Organization Management", "/app/management/organizations")
                }
            }

            // auctionsCard
            if(canAccessAuctions) {
                Card({
                    navigate("/app/auctions")
                }) {
                    Wrap { H3 { Text("Auktionen") } }
                    /*
                AuctionsCard(
                    storage = storage,
                    texts = auctionsCard
                )

                 */
                }


                Card({
                    navigate("/app/auctions/search-bidders")
                }) {
                    Wrap { H3 { Text("Bieter Suche") } }
                }

                Card({
                    navigate("/manual")
                }) {
                    Wrap { H3 { Text("Gebrauchsanleitung") } }
                }
            }
            /*
            Wrap({width(25.percent)}) {
                Text("Management")
            }

             */
        }
    }

}

@Markup
@Composable
@Suppress("FunctionName")
fun Card(
    onClick: ()->Unit,
    style: StyleScope.()->Unit = {},
    content: @Composable ElementScope<HTMLElement>.()->Unit
) = Div({
    onClick { onClick() }
    style{
        style()
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        width(25.percent)
        minHeight(200.px)
        marginRight(10.px)
        padding(10.px)
        border {
            width(1.px)
            style(LineStyle.Solid)
            color(Color.black)
            borderRadius(5.px)
        }
        cursor("pointer")
    }
}) {
    content()
}


@Markup
@Composable
@Suppress("FunctionName")
fun AuctionsCard(storage: Storage<Application>, texts: Source<Lang.Block>) {
    val navButton: Source<Lang.Block> = texts * subComp("navButton")
    StdButton(
        navButton * title,
        storage * deviceData * mediaType.get)
    {
        navigate("/app/auctions")
    }
}
