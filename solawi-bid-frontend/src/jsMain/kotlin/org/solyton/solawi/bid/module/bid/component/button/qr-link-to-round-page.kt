package org.solyton.solawi.bid.module.bid.component.button

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.routing.navigate
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.flexShrink
import org.jetbrains.compose.web.dom.Button
import org.solyton.solawi.bid.module.bid.data.BidApplication
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
    // todo:refactor:extract
    Button(
        attrs = {
            style {
                // todo:style:button w.r.t. device
                //buttonStyle(DeviceType.Tablet)()
                flexShrink(0)

            }
            onClick {
                // todo:dev
                // window.open("$frontendBaseUrl/solyton/auctions/${auctionId}/rounds/${round.roundId}", "_blank")
                navigate("/app/auctions/${auctionId}/rounds/${round.roundId}")
            }
        }
    ){
        QRCodeSvg(
            round.roundId,
            "$frontendBaseUrl/bid/send/${round.link}",
            64.0
        )
    }
}
