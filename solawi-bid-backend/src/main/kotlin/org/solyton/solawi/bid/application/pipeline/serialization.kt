package org.solyton.solawi.bid.application.pipeline

import io.ktor.server.application.*
import kotlinx.serialization.builtins.serializer
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.ktorx.result.serializers
import org.solyton.solawi.bid.module.application.data.ApiApplication
import org.solyton.solawi.bid.module.application.data.ApiApplications
import org.solyton.solawi.bid.module.application.data.ApiLifecycleStage
import org.solyton.solawi.bid.module.application.data.ApiModule
import org.solyton.solawi.bid.module.application.data.ApiUserApplications
import org.solyton.solawi.bid.module.application.data.ApplicationContextRelation
import org.solyton.solawi.bid.module.application.data.ApplicationContextRelations
import org.solyton.solawi.bid.module.application.data.LifecycleStage
import org.solyton.solawi.bid.module.application.data.ModuleContextRelation
import org.solyton.solawi.bid.module.application.data.ModuleContextRelations
import org.solyton.solawi.bid.module.application.data.ReadApplications
import org.solyton.solawi.bid.module.application.data.ReadPersonalUserApplications
import org.solyton.solawi.bid.module.application.data.ReadPersonalApplicationContextRelations
import org.solyton.solawi.bid.module.application.data.ReadPersonalModuleContextRelations
import org.solyton.solawi.bid.module.application.data.ReadUserApplications
import org.solyton.solawi.bid.module.application.data.RegisterForApplications
import org.solyton.solawi.bid.module.application.data.RegisterForModules
import org.solyton.solawi.bid.module.application.data.StartTrialsOfModules
import org.solyton.solawi.bid.module.application.data.StartTrialsOfApplications
import org.solyton.solawi.bid.module.application.data.SubscribeApplications
import org.solyton.solawi.bid.module.application.data.SubscribeModules
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
    serializers[RoundComments::class] = RoundComments.serializer()
    serializers[RoundComment::class] = RoundComment.serializer()
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
    serializers[ReadRightRoleContexts::class] = ReadRightRoleContexts.serializer()
    serializers[ReadRightRoleContextsOfUser::class] = ReadRightRoleContextsOfUser.serializer()
    serializers[ReadRightRoleContextsOfUsers::class] = ReadRightRoleContextsOfUsers.serializer()
    serializers[ReadParentChildRelationsOfContexts::class] = ReadParentChildRelationsOfContexts.serializer()
    serializers[ParentChildRelationsOfContext::class] = ParentChildRelationsOfContext.serializer()
    serializers[ParentChildRelationsOfContexts::class] = ParentChildRelationsOfContexts.serializer()
    serializers[Contexts::class] = Contexts.serializer()
    serializers[Context::class] = Context.serializer()
    serializers[Role::class] = Role.serializer()
    serializers[Right::class] = Right.serializer()
    serializers[UserToContextsMap::class] = UserToContextsMap.serializer()

    // Application / Modules
    serializers[ReadApplications::class] = ReadApplications.serializer()
    serializers[ReadUserApplications::class] = ReadUserApplications.serializer()
    serializers[ReadPersonalUserApplications::class] = ReadPersonalUserApplications.serializer()
    serializers[ReadPersonalApplicationContextRelations::class] = ReadPersonalApplicationContextRelations.serializer()
    serializers[ReadPersonalModuleContextRelations::class] = ReadPersonalModuleContextRelations.serializer()
    serializers[RegisterForApplications::class] = RegisterForApplications.serializer()
    serializers[StartTrialsOfApplications::class] = StartTrialsOfApplications.serializer()
    serializers[SubscribeApplications::class] = SubscribeApplications.serializer()
    serializers[RegisterForModules::class] = RegisterForModules.serializer()
    serializers[StartTrialsOfModules::class] = StartTrialsOfModules.serializer()
    serializers[SubscribeModules::class] = SubscribeModules.serializer()
    serializers[ApiApplications::class] = ApiApplications.serializer()
    serializers[ApiApplication::class] = ApiApplication.serializer()
    serializers[ApiUserApplications::class] = ApiUserApplications.serializer()
    serializers[ApiModule::class] = ApiModule.serializer()
    serializers[ApiLifecycleStage::class] = ApiLifecycleStage.serializer()
    serializers[LifecycleStage.Empty::class] = LifecycleStage.Empty.serializer()
    serializers[LifecycleStage.Registered::class] = LifecycleStage.Registered.serializer()
    serializers[LifecycleStage.Trialing::class] = LifecycleStage.Trialing.serializer()
    serializers[LifecycleStage.Active::class] = LifecycleStage.Active.serializer()
    serializers[LifecycleStage.Paused::class] = LifecycleStage.Paused.serializer()
    serializers[LifecycleStage.PaymentFailedGracePeriod::class] = LifecycleStage.PaymentFailedGracePeriod.serializer()
    serializers[LifecycleStage.Cancelled::class] = LifecycleStage.Cancelled.serializer()
    serializers[LifecycleStage.Churned::class] = LifecycleStage.Churned.serializer()
    serializers[ApplicationContextRelations::class] = ApplicationContextRelations.serializer()
    serializers[ApplicationContextRelation::class] = ApplicationContextRelation.serializer()
    serializers[ModuleContextRelations::class] = ModuleContextRelations.serializer()
    serializers[ModuleContextRelation::class] = ModuleContextRelation.serializer()
}
