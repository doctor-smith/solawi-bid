package org.solyton.solawi.bid.application.ui.page.auction

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.PropertiesStyles
import org.evoleq.compose.layout.PropertyStyles
import org.evoleq.compose.layout.Vertical
import org.evoleq.language.Locale
import org.evoleq.language.component
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.math.emit
import org.evoleq.math.map
import org.evoleq.math.times
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.actions
import org.solyton.solawi.bid.application.data.auctions
import org.solyton.solawi.bid.application.data.i18N
import org.solyton.solawi.bid.application.ui.page.auction.action.readAuctions
import org.solyton.solawi.bid.application.ui.style.page.verticalPageStyle
import org.solyton.solawi.bid.application.ui.style.wrap.Wrap
import org.solyton.solawi.bid.module.bid.component.AuctionDetails
import org.solyton.solawi.bid.module.bid.component.BidRoundList
import org.solyton.solawi.bid.module.bid.component.button.CreateNewRoundButton
import org.solyton.solawi.bid.module.bid.component.button.ImportBiddersButton
import org.solyton.solawi.bid.module.bid.component.button.UpdateAuctionButton
import org.solyton.solawi.bid.module.bid.data.api.AddBidders
import org.solyton.solawi.bid.module.bid.data.api.NewBidder
import org.solyton.solawi.bid.module.bid.data.reader.BidComponent
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.data.locale

val auctionPropertiesStyles = PropertiesStyles(
    containerStyle = { width(40.percent) },
    propertyStyles = PropertyStyles(
        keyStyle = { width(50.percent) },
        valueStyle = { width(50.percent) }
    )
)

@Markup
@Composable
@Suppress("FunctionName")
fun AuctionPage(storage: Storage<Application>, auctionId: String) = Div {
    // Effects
    LaunchedEffect(Unit) {
        (storage * actions).read().emit(readAuctions())
    }
    // Data
    var newBidders by remember { mutableStateOf<List<NewBidder>>(listOf()) }
    var addBidders by remember { mutableStateOf<AddBidders>(AddBidders()) }
    val auction = auctions * FirstBy { it.auctionId == auctionId }
    // Texts
    val texts = (storage * i18N * language * component(BidComponent.AuctionPage))
    val details = texts * subComp("details")
    val buttons = texts * subComp("buttons")

    // Markup
    Vertical(verticalPageStyle) {
        Wrap { Horizontal(styles = { justifyContent(JustifyContent.SpaceBetween); width(100.percent) }) {
            H1 { Text(with((storage * auction).read()) { name }) }
            Horizontal {
                UpdateAuctionButton(
                    storage = storage,
                    auction = auction,
                    texts = buttons * subComp("updateAuction"),
                    dataId = "auction-page.button.configure-auction"
                )
                ImportBiddersButton(
                    storage = storage,
                    newBidders = Storage<List<NewBidder>>(
                        read = { newBidders },
                        write = { newBidders = it }
                    ),
                    addBidders = Storage<AddBidders>(
                        read = { addBidders },
                        write = { addBidders = it }
                    ),
                    auction = auction,
                    texts = buttons * subComp("importBidders"),
                    dataId = "auction-page.button.import-bidders"
                )
                CreateNewRoundButton(
                    storage = storage,
                    auction = auction,
                    texts = buttons * subComp("createRound"),
                    dataId = "auction-page.button.create-round"
                )
            }
        } }
        //LineSeparator()
        Wrap { H2 { Text((details * title).emit()) } }
        Wrap { Horizontal {
            AuctionDetails(
                storage * auction,
                storage * i18N * locale.get map {l -> Locale.from(l)},
                details,
                auctionPropertiesStyles
            )
        } }

        // LineSeparator()

        Wrap {
            BidRoundList(
                storage = storage,
                auction = auction,
                (storage * i18N * language * component(BidComponent.Round))
            )
        }
    }
}
