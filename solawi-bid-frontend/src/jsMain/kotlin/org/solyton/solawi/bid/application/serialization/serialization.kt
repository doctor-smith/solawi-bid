package org.solyton.solawi.bid.application.serialization

import kotlinx.serialization.builtins.serializer
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.ktorx.result.add
import org.evoleq.ktorx.result.serializers
import org.solyton.solawi.bid.module.authentication.data.api.*
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.permission.data.api.*
import org.solyton.solawi.bid.module.user.data.api.*


fun installSerializers() { if(serializers.isEmpty()) {

    serializers {
        // Standard
        add<Int>(Int.serializer())
        add<Long>(Long.serializer())
        add<String>(String.serializer())
        add<Boolean>(Boolean.serializer())
        add<Double>(Double.serializer())
        add<Unit>(Unit.serializer())
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
        add<IsLoggedIn>(IsLoggedIn.serializer())
        add<LoggedInAs>(LoggedInAs.serializer())

        // Auctions
        add<CreateAuction>(CreateAuction.serializer())
        add<Auction>(Auction.serializer())
        add<AuctionDetails>(AuctionDetails.serializer())
        add<AuctionDetails.SolawiTuebingen>(AuctionDetails.SolawiTuebingen.serializer())
        add<GetAuctions>(GetAuctions.serializer())
        add<Auctions>(Auctions.serializer())
        add<DeleteAuctions>(DeleteAuctions.serializer())
        add<UpdateAuctions>(UpdateAuctions.serializer())
        add<ConfigureAuction>(ConfigureAuction.serializer())

        // Bid / Bidder
        add<Bid>(Bid.serializer())
        add<Bidder>(Bidder.serializer())
        add<BidderInfo>(BidderInfo.serializer())
        add<NewBidder>(NewBidder.serializer())
        add<ImportBidders>(ImportBidders.serializer())
        add<DeleteBidders>(DeleteBidders.serializer())
        add<BidRound>(BidRound.serializer())

        // Round
        add<Round>(Round.serializer())
        add<GetRound>(GetRound.serializer())
        add<CreateRound>(CreateRound.serializer())
        add<ChangeRoundState>(ChangeRoundState.serializer())
        add<BidInfo>(BidInfo.serializer())
        add<ExportBidRound>(ExportBidRound.serializer())
        add<BidRoundResults>(BidRoundResults.serializer())
        add<BidResult>(BidResult.serializer())
        add<EvaluateBidRound>(EvaluateBidRound.serializer())
        add<BidRoundEvaluation>(BidRoundEvaluation.serializer())
        add<PreEvaluateBidRound>(PreEvaluateBidRound.serializer())
        add<BidRoundPreEvaluation>(BidRoundPreEvaluation.serializer())
        add<WeightedBid>(WeightedBid.serializer())
        add<AcceptRound>(AcceptRound.serializer())
        add<AcceptedRound>(AcceptedRound.serializer())

        // Search Bidders
        add<BidderMails>(BidderMails.serializer())
        add<BidderData>(BidderData.serializer())
        add<SearchBidderData>(SearchBidderData.serializer())
        add<AddBidders>(AddBidders.serializer())

        // UserManagement
        add<CreateUser>(CreateUser.serializer())
        add<User>(User.serializer())
        add<Users>(Users.serializer())
        add<GetUsers>(GetUsers.serializer())
        add<ChangePassword>(ChangePassword.serializer())

        // Permissions
        add<ReadRightRoleContextsOfUser>(ReadRightRoleContextsOfUser.serializer())
        add<ReadRightRoleContextsOfUsers>(ReadRightRoleContextsOfUsers.serializer())
        add<Context>(Context.serializer())
        add<Role>(Role.serializer())
        add<Right>(Right.serializer())
    }
} }
