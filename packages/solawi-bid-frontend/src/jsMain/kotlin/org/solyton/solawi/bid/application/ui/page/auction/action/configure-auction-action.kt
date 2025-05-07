package org.solyton.solawi.bid.application.ui.page.auction.action

import org.evoleq.compose.Markup
import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.bid.data.api.ApiAuction
import org.solyton.solawi.bid.module.bid.data.api.AuctionDetails
import org.solyton.solawi.bid.module.bid.data.api.ConfigureAuction
import org.solyton.solawi.bid.module.bid.data.toDomainType

@Markup
fun configureAuction(auction: Lens<Application, Auction>) =
    Action<Application, ConfigureAuction, ApiAuction>(
        name = "ConfigureAuction",
        reader = auction * Reader{ a: Auction -> ConfigureAuction(
            a.auctionId,
            a.name,
            a.date,
            AuctionDetails.SolawiTuebingen (
                a.auctionDetails.minimalBid?:0.0,
                a.auctionDetails.benchmark?:0.0,
                a.auctionDetails.targetAmount?:0.0,
                a.auctionDetails.solidarityContribution?:0.0
            )
        ) },
        endPoint = ConfigureAuction::class,
        writer = auction.set contraMap {
            apiAuction: ApiAuction -> apiAuction.toDomainType()
        }
    )