package org.solyton.solawi.bid.application.ui.page.auction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.math.emit
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.context
import org.solyton.solawi.bid.application.data.transform.bid.bidApplicationIso
import org.solyton.solawi.bid.module.bid.data.deviceData
import org.solyton.solawi.bid.module.context.data.isEmpty
import org.solyton.solawi.bid.module.control.button.ArrowUpButton
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.SubTitle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.values.ProviderId


@Markup
@Composable
@Suppress("FunctionName")
fun AuctionsForOrganizationsPage(storage: Storage<Application>, providerId: ProviderId, up: String) {
    if((storage * context * isEmpty()).emit()) return
    // val scope = rememberCoroutineScope()

    val auctionApplicationStorage = storage * bidApplicationIso
    // val applicationActions = auctionApplicationStorage * bidActions
    val deviceType = auctionApplicationStorage * deviceData * mediaType.get


    LaunchedEffect(providerId) {
        launch {
            // bankingApplicationActions dispatch readFiscalYears(providerId.value)
        }
    }


    Page(verticalPageStyle) {
        Wrap {
            Horizontal(styles = {
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                width(100.percent)
            }) {
                PageTitle("Auction Management for Organizations")
                Horizontal {
                    ArrowUpButton(
                        Color.black,
                        Color.white,
                        { "UP" },
                        deviceType,
                        false,
                    ) {
                        navigate(up)
                    }
                }
            }
            SubTitle("Auction Management for Organizations")
        }
    }
}
