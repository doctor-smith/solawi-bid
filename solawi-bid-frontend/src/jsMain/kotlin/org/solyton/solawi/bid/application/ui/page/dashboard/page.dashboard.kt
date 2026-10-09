package org.solyton.solawi.bid.application.ui.page.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Vertical
import org.evoleq.compose.link.Link
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.language.Lang
import org.evoleq.language.component
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.math.*
import org.evoleq.optics.lens.FilterBy
import org.evoleq.optics.storage.Read
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.env.IsNonProdEnv
import org.solyton.solawi.bid.application.data.transform.application.management.applicationManagementModule
import org.solyton.solawi.bid.application.data.transform.banking.bankingApplicationIso
import org.solyton.solawi.bid.application.data.transform.shares.shareManagementIso
import org.solyton.solawi.bid.application.service.getContextByName
import org.solyton.solawi.bid.application.service.setContext
import org.solyton.solawi.bid.application.ui.page.dashboard.data.DashboardComponent
import org.solyton.solawi.bid.application.ui.page.dashboard.permissions.canAccessApplication
import org.solyton.solawi.bid.module.application.data.application.APPLICATION_MANAGEMENT_APPLICATION_NAME
import org.solyton.solawi.bid.module.application.permission.Context
import org.solyton.solawi.bid.module.banking.action.readPersonalBankAccounts
import org.solyton.solawi.bid.module.banking.application.BANKING_APPLICATION_NAME
import org.solyton.solawi.bid.module.banking.data.bankingApplicationActions
import org.solyton.solawi.bid.module.bid.application.AUCTION_APPLICATION_NAME
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.distribution.application.DISTRIBUTION_APPLICATION_NAME
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.shares.action.readPersonalShareSubscriptions
import org.solyton.solawi.bid.module.shares.application.SHARE_MANAGEMENT_APPLICATION_NAME
import org.solyton.solawi.bid.module.shares.data.shareManagementActions
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.service.user.userIdFromToken
import org.w3c.dom.HTMLElement

@Markup
@Composable
@Suppress("FunctionName")
fun DashboardPage(storage: Storage<Application>) {
    // Effects
    val applicationContext = storage.getContextByName(Context.Application.value)
    if(applicationContext == null) return@DashboardPage

    LaunchedEffect(Unit) {
        launch {
            storage.setContext(Context.Application)
        }
        launch {
            storage * shareManagementIso * shareManagementActions dispatch readPersonalShareSubscriptions()
        }
        launch {
            storage * bankingApplicationIso * bankingApplicationActions dispatch readPersonalBankAccounts()
        }

    }
    // Data
    val userId = storage * userData * userIdFromToken


    // Permissions
    val canAccessPersonalData = true
    val canAccessApplicationManagement = storage * applicationManagementModule * canAccessApplication(APPLICATION_MANAGEMENT_APPLICATION_NAME) * Emit
    val canAccessAuctions = storage * applicationManagementModule * canAccessApplication(AUCTION_APPLICATION_NAME) * Emit
    val canAccessOrganizations = true
    val canAccessManual = true
    val canAccessShareManagement = storage * applicationManagementModule * canAccessApplication(SHARE_MANAGEMENT_APPLICATION_NAME) * Emit
    val canAccessDistributionManagement = storage * applicationManagementModule * canAccessApplication(DISTRIBUTION_APPLICATION_NAME) * Emit
    val canAccessBankingApplication = storage * applicationManagementModule * canAccessApplication(BANKING_APPLICATION_NAME) * Emit
    val hasPersonalShareSubscriptions = Read(storage * personalShareSubscriptions) * IsNotEmpty
    val hasPersonalBankAccounts = Read(storage * bankAccounts * FilterBy { it.userId.value == userId * Emit }) * IsNotEmpty


    // Texts
    val texts = (storage * i18N * language * component(DashboardComponent.Page))
    // val auctionsCard = texts * subComp("auctionsCard")

    Vertical(verticalPageStyle) {
        Wrap{
            H1 { Text((texts * title).emit()) }
        }
        Horizontal({
            flexWrap(FlexWrap.Wrap)
        }) {
            When(canAccessPersonalData) {
                Card({
                    navigate("/app/private/data")
                }) {
                    Wrap { H3 { Text("Persönliche Daten") } }

                    Link("Meine Daten", "/app/private/data")
                }
            }
            When(canAccessApplicationManagement) {
                Card({
                    navigate("/app/management")
                }) {
                    Wrap { H3 { Text("Application Management") } }

                    Link("My Apps", "/app/management/private")
                    Link("User Management", "/app/management/users")
                }
            }
            When(
                !canAccessApplicationManagement &&
                 (storage * environment * IsNonProdEnv * Emit) // Remove this condition when private app management is production ready
            ) {
                Card({
                    navigate("/app/management/private")
                }) {
                    Wrap { H3 { Text("Application Management") } }

                    Link("My Apps", "/app/management/private")
                }
            }
            When(canAccessOrganizations) {
                Card({
                    navigate("/app/management/organizations")
                }) {
                    Wrap { H3 { Text("Organisationen") } }
                    Link("Organization Management", "/app/management/organizations")
                }
            }

            // auctionsCard
            When(canAccessAuctions) {
                Card({
                    navigate("/app/auctions")
                }) {
                    Wrap { H3 { Text("Auktionen") } }

                    Link("Meine Auktionen", "/app/auctions")
                }
/*
                Card({
                    navigate("/app/auctions/search-bidders")
                }) {
                    Wrap { H3 { Text("Bieter Suche") } }
                }
*/
            }
            When(canAccessManual) {
                Card({
                    navigate("/manual")
                }) {
                    Wrap { H3 { Text("Gebrauchsanleitung") } }
                }
            }

            When(canAccessShareManagement  || hasPersonalShareSubscriptions) {
                Card({}) {
                    Wrap { H3 {Text("Anteils Management") }}

                    When(hasPersonalShareSubscriptions) {
                        Link("Meine Anteile", "/app/management/private/shares")
                    }

                    When(canAccessShareManagement) {
                        Link("Anteile in Organisationen verwalten", "/app/management/organizations/with/shares")
                    }
                }
            }

            When(canAccessDistributionManagement) {
                Card({}) {
                    Wrap { H3 {Text("Depot Management") }}

                    Link("Depotverwaltung in Organisationen", "/app/management/organizations/with/distribution")
                }
            }

            When(canAccessBankingApplication || hasPersonalBankAccounts) {
                Card({}) {
                    Wrap { H3 {Text("Bank Applikation") }}

                    When(canAccessBankingApplication) {
                        Link("Bank Angelegenheiten in Organisationen", "/app/management/organizations/with/banking")
                    }
                }
            }
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
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        width(24.percent)
        minWidth(300.px)
        minHeight(200.px)
        marginRight(10.px)
        marginBottom(10.px)
        padding(10.px)
        border {
            width(1.px)
            style(LineStyle.Solid)
            color(Color.black)
            borderRadius(5.px)
        }
        cursor("pointer")
        style()
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
