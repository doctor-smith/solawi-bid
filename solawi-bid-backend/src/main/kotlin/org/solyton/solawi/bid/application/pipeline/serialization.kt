package org.solyton.solawi.bid.application.pipeline

import io.ktor.server.application.*
import kotlinx.serialization.builtins.serializer
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.ktorx.result.serializers
import org.solyton.solawi.bid.module.authentication.data.api.*
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.permission.data.api.*
import org.solyton.solawi.bid.module.user.data.api.*

fun Application.installSerializers() {
    // primitive serializers
    serializers[Int::class] = Int.serializer()
    serializers[Boolean::class] = Boolean.serializer()
    serializers[String::class] = String.serializer()
    serializers[Double::class] = Double.serializer()
    serializers[Unit::class] = Unit.serializer()
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
    serializers[IsLoggedIn::class] = IsLoggedIn.serializer()
    serializers[LoggedInAs::class] = LoggedInAs.serializer()
    serializers[Logout::class] = Logout.serializer()
    // Bid serializers
    serializers[Bid::class] = Bid.serializer()
    serializers[BidRound::class] = BidRound.serializer()
    serializers[GetRound::class] = GetRound.serializer()
    // Auction
    serializers[Auction::class] = Auction.serializer()
    serializers[CreateAuction::class] = CreateAuction.serializer()
    serializers[GetAuctions::class] = GetAuctions.serializer()
    serializers[Auctions::class] = Auctions.serializer()
    serializers[DeleteAuctions::class] = DeleteAuctions.serializer()
    serializers[UpdateAuctions::class] = UpdateAuctions.serializer()
    serializers[ConfigureAuction::class] = ConfigureAuction.serializer()
    serializers[AuctionDetails::class] = AuctionDetails.serializer()
    serializers[AuctionDetails.SolawiTuebingen::class] = AuctionDetails.SolawiTuebingen.serializer()
    // Auction/Bidders
    serializers[NewBidder::class] = NewBidder.serializer()
    serializers[Bidder::class] = Bidder.serializer()
    serializers[ImportBidders::class] = ImportBidders.serializer()
    serializers[DeleteBidders::class] = DeleteBidders.serializer()
    serializers[BidderInfo::class] = BidderInfo.serializer()
    // Round
    serializers[Round::class] = Round.serializer()
    serializers[BidRound::class] = BidRound.serializer()
    serializers[GetRound::class] = GetRound.serializer()
    serializers[CreateRound::class] = CreateRound.serializer()
    serializers[ChangeRoundState::class] = ChangeRoundState.serializer()
    serializers[BidInfo::class] = BidInfo.serializer()
    serializers[ExportBidRound::class] = ExportBidRound.serializer()
    serializers[BidRoundResults::class] = BidRoundResults.serializer()
    serializers[BidResult::class] = BidResult.serializer()
    serializers[EvaluateBidRound::class] = EvaluateBidRound.serializer()
    serializers[BidRoundEvaluation::class] = BidRoundEvaluation.serializer()
    serializers[PreEvaluateBidRound::class] = PreEvaluateBidRound.serializer()
    serializers[BidRoundPreEvaluation::class] = BidRoundPreEvaluation.serializer()
    serializers[WeightedBid::class] = WeightedBid.serializer()
    serializers[AcceptRound::class] = AcceptRound.serializer()
    serializers[AcceptedRound::class] = AcceptedRound.serializer()
    // Search Bidders
    serializers[BidderMails::class] = BidderMails.serializer()
    serializers[BidderData::class] = BidderData.serializer()
    serializers[SearchBidderData::class] = SearchBidderData.serializer()
    serializers[AddBidders::class] = AddBidders.serializer()

    // UserManagement
    serializers[CreateUser::class] = CreateUser.serializer()
    serializers[User::class] = User.serializer()
    serializers[Users::class] = Users.serializer()
    serializers[GetUsers::class] = GetUsers.serializer()
    serializers[ChangePassword::class] = ChangePassword.serializer()

    // Permissions
    serializers[ReadRightRoleContextsOfUser::class] = ReadRightRoleContextsOfUser.serializer()
    serializers[ReadRightRoleContextsOfUsers::class] = ReadRightRoleContextsOfUsers.serializer()
    serializers[Context::class] = Context.serializer()
    serializers[Role::class] = Role.serializer()
    serializers[Right::class] = Right.serializer()
}
