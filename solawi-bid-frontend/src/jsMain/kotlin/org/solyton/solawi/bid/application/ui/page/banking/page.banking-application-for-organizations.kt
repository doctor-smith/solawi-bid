package org.solyton.solawi.bid.application.ui.page.banking

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.transform.banking.bankingApplicationIso
import org.solyton.solawi.bid.module.banking.data.application.deviceData
import org.solyton.solawi.bid.module.banking.data.application.fiscalYears
import org.solyton.solawi.bid.module.banking.data.bankingApplicationActions
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
fun BankingApplicationForOrganizationsPage(storage: Storage<Application>, providerId: ProviderId, up: String) {
    // val scope = rememberCoroutineScope()

    val bankingApplicationStorage = storage * bankingApplicationIso
    val bankingApplicationActions = bankingApplicationStorage * bankingApplicationActions
    val deviceType = bankingApplicationStorage * deviceData * mediaType.get

    val fiscalYears = bankingApplicationStorage * fiscalYears

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
                PageTitle("Banking for Organizations")
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
            SubTitle("Manage your banking for Organizations")
        }
    }
}
