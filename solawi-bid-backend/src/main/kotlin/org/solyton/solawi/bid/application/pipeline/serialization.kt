package org.solyton.solawi.bid.application.pipeline

import io.ktor.server.application.*
import kotlinx.serialization.builtins.serializer
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.ktorx.result.serializers
import org.solyton.solawi.bid.module.authentication.data.api.*
import org.solyton.solawi.bid.module.bid.data.api.*

fun Application.installSerializers() {
    // primitive serializers
    serializers[Int::class] = Int.serializer()
    serializers[Boolean::class] = Boolean.serializer()
    serializers[String::class] = String.serializer()
    serializers[Double::class] = Double.serializer()
    // Result serializers
    serializers[Result::class] = ResultSerializer
    serializers[Result.Success::class] = ResultSerializer
    serializers[Result.Failure::class] = ResultSerializer
    serializers[Result.Failure.Message::class] = ResultSerializer
    serializers[Result.Failure.Exception::class] = ResultSerializer

    // General
    serializers[Identifier::class] = Identifier.serializer()
    // Login serializers
    serializers[Login::class] = Login.serializer()
    serializers[LoggedIn::class] = LoggedIn.serializer()
    serializers[RefreshToken::class] = RefreshToken.serializer()
    serializers[AccessToken::class] = AccessToken.serializer()
    // Bid serializers
    serializers[Bid::class] = Bid.serializer()
    serializers[BidRound::class] = BidRound.serializer()
    // Auction
    serializers[Auction::class] = Auction.serializer()
    serializers[CreateAuction::class] = CreateAuction.serializer()
    serializers[GetAuctions::class] = GetAuctions.serializer()
    serializers[Auctions::class] = Auctions.serializer()
    serializers[DeleteAuctions::class] = DeleteAuctions.serializer()
}