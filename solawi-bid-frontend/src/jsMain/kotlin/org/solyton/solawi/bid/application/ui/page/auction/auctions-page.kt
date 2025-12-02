package org.solyton.solawi.bid.application.ui.page.auction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.guard.data.isLoading
import org.evoleq.compose.guard.data.onEmpty
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Vertical
import org.evoleq.compose.layout.VerticalAccent
import org.evoleq.device.data.mediaType
import org.evoleq.language.component
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.math.Reader
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.transform.bid.bidApplicationIso
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.service.useI18nTransform
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.module.bid.action.readAuctions
import org.solyton.solawi.bid.module.bid.component.AuctionList
import org.solyton.solawi.bid.module.bid.component.button.CreateAuctionButton
import org.solyton.solawi.bid.module.bid.component.form.DEFAULT_AUCTION_ID
import org.solyton.solawi.bid.module.bid.data.*
import org.solyton.solawi.bid.module.bid.data.reader.BidComponent
import org.solyton.solawi.bid.module.user.data.actions as userActions
import org.solyton.solawi.bid.module.error.component.showErrorModal
import org.solyton.solawi.bid.module.error.lang.errorModalTexts
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.guard.onMissing
import org.solyton.solawi.bid.module.style.layout.accent.vertical.verticalAccentStyles
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.action.organization.readOrganizations

@Markup
@Composable
@Suppress("FunctionName")
fun AuctionsPage(storage: Storage<Application>) = Div {
    // Bid Application Storage
    val bidApplicationStorage = storage * bidApplicationIso
    // Effect
    LaunchedEffect(Unit) {
        (bidApplicationStorage * actions).dispatch(readAuctions())
    }

    LaunchedEffect(Unit) {
        (storage * userIso * userActions).dispatch(readOrganizations())
    }

    if(isLoading(
        onMissing(
            BidComponent.AuctionsPage,
            bidApplicationStorage * i18N.get,
        ) {
            LaunchComponentLookup(
                BidComponent.AuctionsPage,
                storage  * Reader { app: Application -> app.environment.useI18nTransform() },
                bidApplicationStorage * i18N
            )
        }
    )) return@Div

    // Data
    val auction = auctions * FirstBy { it.auctionId == DEFAULT_AUCTION_ID }

    // Texts
    val texts = (bidApplicationStorage * i18N * language * component(BidComponent.AuctionsPage))

    // Markup
    Vertical(style = verticalPageStyle) {
        VerticalAccent(
            verticalAccentStyles(bidApplicationStorage * deviceData * mediaType.get)
        ) {
        Wrap {
            Horizontal(styles = { justifyContent(JustifyContent.FlexStart); alignItems(AlignItems.Center); width(100.percent); gap(20.px) }) {
                H1({style { marginLeft(20.px) }}) { Text((texts * title).emit()) }
                CreateAuctionButton(
                    storage = bidApplicationStorage,
                    auction = auction,
                    texts = texts * subComp("buttons") * subComp("createAuction")
                )
            }
        }
        Wrap{ AuctionList(
            bidApplicationStorage * auctions,
            bidApplicationStorage * user.get,
            bidApplicationStorage * i18N,
            bidApplicationStorage * modals,
            bidApplicationStorage * deviceData * mediaType.get
        ) {
            CoroutineScope(Job()).launch {
                val actions = (bidApplicationStorage * actions).read()
                try {
                    actions.dispatch(it)
                } catch (exception: Exception) {
                    (bidApplicationStorage * modals).showErrorModal(
                        texts = errorModalTexts(
                            exception.message ?: exception.cause?.message ?: "Cannot Emit action '${it.name}'"
                        ),
                        device = bidApplicationStorage * deviceData * mediaType.get,
                    )
                }
            }
        }}
    } }
}
