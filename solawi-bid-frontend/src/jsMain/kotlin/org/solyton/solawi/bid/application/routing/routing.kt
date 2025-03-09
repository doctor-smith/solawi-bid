package org.solyton.solawi.bid.application.routing

import androidx.compose.runtime.Composable
import org.evoleq.compose.routing.*
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.device.mediaType
import org.solyton.solawi.bid.application.data.deviceData
import org.solyton.solawi.bid.application.data.env.type
import org.solyton.solawi.bid.application.data.environment
import org.solyton.solawi.bid.application.data.navbar.navBar
import org.solyton.solawi.bid.application.data.userData
import org.solyton.solawi.bid.application.service.seemsToBeLoggerIn
import org.solyton.solawi.bid.application.ui.page.auction.*
import org.solyton.solawi.bid.application.ui.page.dashboard.DashboardPage
import org.solyton.solawi.bid.application.ui.page.login.LoginPage
import org.solyton.solawi.bid.application.ui.page.login.effect.LaunchLogoutEffect
import org.solyton.solawi.bid.application.ui.page.sendbid.SendBidPage
import org.solyton.solawi.bid.application.ui.page.sendbid.ShowQRCodePage
import org.solyton.solawi.bid.application.ui.page.test.FontsPage
import org.solyton.solawi.bid.application.ui.page.test.MobileTestPage
import org.solyton.solawi.bid.application.ui.page.test.TestPage
import org.solyton.solawi.bid.application.ui.page.user.UserManagementPage
import org.solyton.solawi.bid.module.navbar.component.NavBar

@RoutingDsl
@Composable
@Suppress("FunctionName")
fun Routing(storage: Storage<Application>): Routes = Routing("/") {
    route("bid") {
        route("send/:cryptoId") {
            component {
                SendBidPage(storage, "${parameter("cryptoId")}")
            }
        }
        route("qr-code/:cryptoId") {
            component {
                ShowQRCodePage(storage, "${parameter("cryptoId")}")
            }
        }
    }
    route("login") {
        component { LoginPage(storage) }
    }
    route("solyton") {
        wrap {
            access {
                // todo:dev improve it
                when ((storage * userData).read().seemsToBeLoggerIn()) {
                    true -> {
                        //TriggerIsLoggedInEffect(storage)
                        true
                    }
                    false -> when {
                        currentPath().startsWith("/bid/send") -> true
                        currentPath().startsWith("/bid/qr-code") -> true
                        else -> {
                            navigate("/login")
                            false
                        }
                    }
                }
            }
            layout { page -> {
                NavBar(storage, storage * navBar, storage * deviceData * mediaType.get)
                // render page
                page()
            } }

            route("dashboard") {
                component { DashboardPage(storage) }
            }

            route("management") {
                component{
                    UserManagementPage(storage)
                }
            }

            route("auctions") {
                component {
                    AuctionsPage(storage)
                }
                route(":auctionId") {
                    component {
                        AuctionPage(storage, parameter("auctionId")!!)
                    }
                    route("rounds/:roundId") {
                        component {
                            val auctionId = parameter("auctionId")!!
                            val roundId = parameter("roundId")!!

                            RoundPage(
                                storage,
                                auctionId,
                                roundId
                            )
                        }

                        route("evaluation") {
                            component {
                                BidRoundEvaluationPage(
                                    storage,
                                    parameter("auctionId")!!,
                                )
                            }
                        }
                    }
                }
                route("search-bidders") {
                    component {
                        SearchBiddersPage(
                            storage
                        )
                    }
                }

            }
            route("logout") {
                component {
                    Text("Logged out")
                    LaunchLogoutEffect(storage)
                }
            }
        }
    }
    nonProdRoute(
        "test",
        (storage * environment * type).read()
    ){
        route("page") {
            component { TestPage() }
        }
        route("mobile") {
            component { MobileTestPage(storage) }
        }
        route("fonts") {
            component { FontsPage() }
        }
    }
}
