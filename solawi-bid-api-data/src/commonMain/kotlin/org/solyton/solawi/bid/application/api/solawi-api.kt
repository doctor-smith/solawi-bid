package org.solyton.solawi.bid.application.api


import org.evoleq.ktorx.api.Api
import org.solyton.solawi.bid.module.authentication.data.api.*
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.user.data.api.CreateUser
import org.solyton.solawi.bid.module.user.data.api.GetUsers
import org.solyton.solawi.bid.module.user.data.api.Users

val solawiApi by lazy {
    // Authentication
    Api().post<Login, LoggedIn>(
        key = Login::class,
        url = "login"
    ).post<RefreshToken,LoggedIn>(
        key = RefreshToken::class,
        url = "refresh"
    ).patch<Logout, Unit>(
        key = Logout::class,
        url = "logout"
    ).patch<IsLoggedIn, LoggedInAs>(
        key = IsLoggedIn::class,
        url = "is-logged-in"
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
    .patch<ConfigureAuction, Auction>(
        key = ConfigureAuction::class,
        url = "auction/configure"
    )
    .post<ImportBidders, Auction>(
        key = ImportBidders::class,
        url = "auction/bidder/import"
    )
    .delete<DeleteBidders, Auction>(
        key = DeleteBidders::class,
        url = "auction/bidder/delete"
    )
    // Round
    .get<GetRound, Round>(
        key = GetRound::class,
        url = "round/create---nonsense"
    )
    .post<CreateRound, Round>(
        key = CreateRound::class,
        url = "round/create"
    )
    .patch<ChangeRoundState, Round>(
        key = ChangeRoundState::class,
        url = "round/change-state"
    )
    .patch<ExportBidRound, BidRoundResults>(
        key = ExportBidRound::class,
        url = "round/export-results"
    )
    .patch<EvaluateBidRound, BidRoundEvaluation>(
        key = EvaluateBidRound::class,
        url = "round/evaluate"
    )
    .patch<PreEvaluateBidRound, BidRoundPreEvaluation>(
        key = PreEvaluateBidRound::class,
        url = "round/pre-evaluate"
    )
    .patch<AcceptRound, AcceptedRound>(
        key  = AcceptRound::class,
        url = "auction/accept-round"
    )

    // Auction bid
    .post<Bid,BidRound> (
        key = Bid::class,
        url = "bid/send"
    )
    // Search Bidders
    .patch<SearchBidderData, BidderMails>(
        key = SearchBidderData::class,
        url = "bidders/search"
    )
    .post<AddBidders, Unit>(
        key = AddBidders::class, "bidders/add"
    )

    // User Management
    .post<CreateUser, Unit>(
        key = CreateUser::class,
        url = "users/create"
    )
    .get<GetUsers, Users>(
        key = GetUsers::class,
        url = "users/all"
    )
}
