package org.solyton.solawi.bid.application.ui.page.auction.action

import org.evoleq.compose.Markup
import org.evoleq.math.Reader
import org.evoleq.math.Writer
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.auction.Auction
import org.solyton.solawi.bid.module.bid.data.api.ApiRound
import org.solyton.solawi.bid.module.bid.data.api.CreateRound
import org.solyton.solawi.bid.module.bid.data.toDomainType

@Markup
fun createRound(auction: Lens<BidApplication, Auction>) =
    Action<BidApplication, CreateRound, ApiRound>(
        name = "CreateRound",
        reader = auction * Reader{ a: Auction -> CreateRound(a.auctionId) },
        endPoint = CreateRound::class,
        writer = auction * Writer{
                apiRound: ApiRound ->{ a -> a.copy(
                    rounds = listOf(
                        *a.rounds.toTypedArray(),
                        apiRound.toDomainType())
                    )
                }
        }
    )
