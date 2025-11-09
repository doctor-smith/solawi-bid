package org.solyton.solawi.bid.application.serialization

import kotlinx.serialization.builtins.serializer
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.ktorx.result.add
import org.evoleq.ktorx.result.serializers
import org.solyton.solawi.bid.module.application.data.*
import org.solyton.solawi.bid.module.application.data.ApiApplication
import org.solyton.solawi.bid.module.application.data.ApiApplications
import org.solyton.solawi.bid.module.application.data.ApiModule
import org.solyton.solawi.bid.module.application.data.ApiUserApplications
import org.solyton.solawi.bid.module.authentication.data.api.*
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.permission.data.api.*
import org.solyton.solawi.bid.module.user.data.api.*
import org.solyton.solawi.bid.module.user.data.api.organization.AddMember
import org.solyton.solawi.bid.module.user.data.api.organization.CreateChildOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.CreateOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.DeleteOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.Member
import org.solyton.solawi.bid.module.user.data.api.organization.Organization
import org.solyton.solawi.bid.module.user.data.api.organization.Organizations
import org.solyton.solawi.bid.module.user.data.api.organization.ReadOrganizations
import org.solyton.solawi.bid.module.user.data.api.organization.RemoveMember
import org.solyton.solawi.bid.module.user.data.api.organization.UpdateMember
import org.solyton.solawi.bid.module.user.data.api.organization.UpdateOrganization


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
        add<RoundComments>(RoundComments.serializer())
        add<RoundComment>(RoundComment.serializer())
        add<CommentOnRound>(CommentOnRound.serializer())

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
        add<ReadRightRoleContexts>(ReadRightRoleContexts.serializer())
        add<ReadRightRoleContextsOfUser>(ReadRightRoleContextsOfUser.serializer())
        add<ReadRightRoleContextsOfUsers>(ReadRightRoleContextsOfUsers.serializer())
        add<ReadParentChildRelationsOfContexts>(ReadParentChildRelationsOfContexts.serializer())
        add<ParentChildRelationsOfContexts>(ParentChildRelationsOfContexts.serializer())
        add<ParentChildRelationsOfContext>(ParentChildRelationsOfContext.serializer())
        add<Context>(Context.serializer())
        add<Contexts>(Contexts.serializer())
        add<Role>(Role.serializer())
        add<Right>(Right.serializer())
        add<UserToContextsMap>(UserToContextsMap.serializer())

        // Applications and Modules
        add<ReadApplications>(ReadApplications.serializer())
        add<ReadUserApplications>(ReadUserApplications.serializer())
        add<ReadPersonalUserApplications>(ReadPersonalUserApplications.serializer())
        add<ReadPersonalApplicationContextRelations>(ReadPersonalApplicationContextRelations.serializer())
        add<ReadPersonalModuleContextRelations>(ReadPersonalModuleContextRelations.serializer())
        add<RegisterForApplications>(RegisterForApplications.serializer())
        add<StartTrialsOfApplications>(StartTrialsOfApplications.serializer())
        add<SubscribeApplications>(SubscribeApplications.serializer())
        add<RegisterForModules>(RegisterForModules.serializer())
        add<StartTrialsOfModules>(StartTrialsOfModules.serializer())
        add<SubscribeModules>(SubscribeModules.serializer())
        add<ConnectApplicationToOrganization>(ConnectApplicationToOrganization.serializer())
        add<UpdateOrganizationModuleRelations>(UpdateOrganizationModuleRelations.serializer())
        add<ApiApplications>(ApiApplications.serializer())
        add<ApiUserApplications>(ApiUserApplications.serializer())
        add<ApiApplication>(ApiApplication.serializer())
        add<ApiModule>(ApiModule.serializer())
        add<LifecycleStage>(LifecycleStage.serializer())
        add<LifecycleStage.Empty>(LifecycleStage.Empty.serializer())
        add<LifecycleStage.Registered>(LifecycleStage.Registered.serializer())
        add<LifecycleStage.Trialing>(LifecycleStage.Trialing.serializer())
        add<LifecycleStage.Active>(LifecycleStage.Active.serializer())
        add<LifecycleStage.Paused>(LifecycleStage.Paused.serializer())
        add<LifecycleStage.PaymentFailedGracePeriod>(LifecycleStage.PaymentFailedGracePeriod.serializer())
        add<LifecycleStage.Cancelled>(LifecycleStage.Cancelled.serializer())
        add<LifecycleStage.Churned>(LifecycleStage.Churned.serializer())
        add<ApplicationContextRelation>(ApplicationContextRelation.serializer())
        add<ApplicationContextRelations>(ApplicationContextRelations.serializer())
        add<ModuleContextRelation>(ModuleContextRelation.serializer())
        add<ModuleContextRelations>(ModuleContextRelations.serializer())
        add<ApplicationOrganizationRelation>(ApplicationOrganizationRelation.serializer())
        add<ApplicationOrganizationRelations>(ApplicationOrganizationRelations.serializer())

        // Organizations
        add<Organizations>(Organizations.serializer())
        add<Organization>(Organization.serializer())
        add<Member>(Member.serializer())

        add<CreateOrganization>(CreateOrganization.serializer())
        add<CreateChildOrganization>(CreateChildOrganization.serializer())
        add<ReadOrganizations>(ReadOrganizations.serializer())
        add<UpdateOrganization>(UpdateOrganization.serializer())
        add<DeleteOrganization>(DeleteOrganization.serializer())
        add<AddMember>(AddMember.serializer())
        add<RemoveMember>(RemoveMember.serializer())
        add<UpdateMember>(UpdateMember.serializer())
    }
} }
