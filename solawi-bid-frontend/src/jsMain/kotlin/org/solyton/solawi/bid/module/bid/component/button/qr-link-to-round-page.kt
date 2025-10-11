package org.solyton.solawi.bid.module.bid.component.button

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.disabled
import org.evoleq.compose.routing.navigate
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.api.RoundState
import org.solyton.solawi.bid.module.bid.data.auction.Auction
import org.solyton.solawi.bid.module.bid.data.auction.auctionId
import org.solyton.solawi.bid.module.bid.data.bidround.Round
import org.solyton.solawi.bid.module.qrcode.QRCodeSvg

@Markup
@Composable
@Suppress("FunctionName")
fun QRLinkToRoundPageButton(
    storage: Storage<BidApplication>,
    auction: Lens<BidApplication, Auction>,
    round: Round,
    frontendBaseUrl: String,
) {
    val auctionId = (storage * auction * auctionId).read()
    val isDisabled = RoundState.fromString(round.state) in setOf(
        RoundState.Closed,
        RoundState.Frozen,
        RoundState.Evaluated
    )
    // todo:refactor:extract
    Button(
        attrs = {
            if(isDisabled) disabled()
            style {
                if(isDisabled) {
                    property("opacity", 0.5)
                    cursor("not-allowed")
                } else {
                    cursor("pointer")
                }
                // flexShrink(0)
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
            }
            onClick {

                // todo:dev
                // openUrlInNewTab("$frontendBaseUrl/app/auctions/${auctionId}/rounds/${round.roundId}")
                navigate("/app/auctions/${auctionId}/rounds/${round.roundId}")
            }
        }
    ){
        QRCodeSvg(
            round.roundId,
            "$frontendBaseUrl/bid/send/${round.link}",
            32.0 //64.0
        )
    }
}
