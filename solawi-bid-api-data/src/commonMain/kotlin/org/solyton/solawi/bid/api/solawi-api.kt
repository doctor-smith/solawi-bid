package org.solyton.solawi.bid.api

import org.evoleq.ktorx.api.Api
import org.solyton.solawi.bid.module.authentication.data.api.LoggedIn
import org.solyton.solawi.bid.module.authentication.data.api.Login
import org.solyton.solawi.bid.module.authentication.data.api.Logout
import org.solyton.solawi.bid.module.authentication.data.api.RefreshToken
import org.solyton.solawi.bid.module.bid.data.api.*

val solawiApi by lazy {
    // Authentication
    Api().post<Login, LoggedIn>(
        key = Login::class,
        url = "login"
    ).post<RefreshToken,LoggedIn>(
        key = RefreshToken::class,
        url = "refresh"
    ).post<Logout, Unit>(
        key = Logout::class,
        url = "logout"
    )

    // Auction
    .post<CreateAuction, Auction>(
        key = CreateAuction::class,
        url = "auction/create"
    ).get<GetAuctions, List<Auction>>(
        key = GetAuctions::class,
        url = "auction/all"
    ).delete<DeleteAuctions, List<Auction>>(
        key = DeleteAuctions::class,
        url ="auction/delete"
    ).patch<UpdateAuctions, List<Auction>>(
        key = UpdateAuctions::class,
        url = "auction/update"
    )
    .post<ImportBidders, Auction>(
        key = ImportBidders::class,
        url = "auction/bidder/import"
    )
    .delete<DeleteBidders, Auction>(
        key = DeleteBidders::class,
        url = "auction/bidder/delete"
    )
    // Auction bid
    .post<Bid,BidRound> (
        key = Bid::class,
        url = "bid/send"
    )
}
