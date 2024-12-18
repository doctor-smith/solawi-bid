package org.solyton.solawi.bid.application.serialization

import kotlinx.serialization.builtins.serializer
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.ktorx.result.add
import org.evoleq.ktorx.result.serializers
import org.solyton.solawi.bid.module.authentication.data.api.LoggedIn
import org.solyton.solawi.bid.module.authentication.data.api.Login
import org.solyton.solawi.bid.module.authentication.data.api.Logout
import org.solyton.solawi.bid.module.authentication.data.api.RefreshToken
import org.solyton.solawi.bid.module.bid.data.api.*


fun installSerializers() { if(serializers.isEmpty()) {

    serializers {
        // Standard
        add<Int>(Int.serializer())
        add<Long>(Long.serializer())
        add<String>(String.serializer())
        add<Boolean>(Boolean.serializer())
        add<Double>(Double.serializer())
        add(Nothing::class, Unit.serializer())
       // add(List::class, ListSerializer)
        //...

        // Result
        add<Result<*>>(ResultSerializer)
        add<Result.Failure>(Result.Failure.serializer())
        add<Result.Failure.Message>(Result.Failure.Message.serializer())
        add(Result.Success::class, ResultSerializer)



        // Authorization
        add<Login>(Login.serializer())
        add<LoggedIn>(LoggedIn.serializer())
        add<RefreshToken>(RefreshToken.serializer())
        add<Logout>(Logout.serializer())

        // Auctions
        add<CreateAuction>(CreateAuction.serializer())
        add<Auction>(Auction.serializer())
        add<GetAuctions>(GetAuctions.serializer())
        add<Auctions>(Auctions.serializer())
        add<DeleteAuctions>(DeleteAuctions.serializer())
        add<UpdateAuctions>(UpdateAuctions.serializer())
        // Bid / Bidder
        add<Bid>(Bid.serializer())
        add<Bidder>(Bidder.serializer())
        add<NewBidder>(NewBidder.serializer())
        add<ImportBidders>(ImportBidders.serializer())
        add<DeleteBidders>(DeleteBidders.serializer())
        add<BidRound>(BidRound.serializer())
    }
} }
