package org.solyton.solawi.bid.application.pipeline

import io.ktor.server.application.*
import kotlinx.serialization.builtins.serializer
import org.evoleq.ktorx.client.Parameters
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.ktorx.result.add
import org.evoleq.ktorx.result.serializers
import org.solyton.solawi.bid.module.application.data.*
import org.solyton.solawi.bid.module.application.data.ApiApplication
import org.solyton.solawi.bid.module.application.data.ApiApplications
import org.solyton.solawi.bid.module.application.data.ApiLifecycleStage
import org.solyton.solawi.bid.module.application.data.ApiModule
import org.solyton.solawi.bid.module.application.data.ApiUserApplications
import org.solyton.solawi.bid.module.authentication.data.api.*
import org.solyton.solawi.bid.module.banking.data.api.*
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.distribution.data.api.*
import org.solyton.solawi.bid.module.permission.data.api.*
import org.solyton.solawi.bid.module.shares.data.api.*
import org.solyton.solawi.bid.module.user.data.api.*
import org.solyton.solawi.bid.module.user.data.api.organization.*
import org.solyton.solawi.bid.module.user.data.api.userprofile.*
import org.solyton.solawi.bid.module.values.ProviderId
import org.solyton.solawi.bid.module.values.UserId
import org.solyton.solawi.bid.module.values.Username
import org.solyton.solawi.bid.module.values.Uuid

fun installSerializers() {
    serializers {

        // primitive serializers
        add<Int>(Int.serializer())
        add<Boolean>(Boolean.serializer())
        add<String>(String.serializer())
        add<Double>(Double.serializer())
        add<Unit>(Unit.serializer())
        // standard values
        add<Uuid>(Uuid.serializer())
        add<UserId>(UserId.serializer())
        add<ProviderId>(ProviderId.serializer())
        add<Username>(Username.serializer())
        // API
        add<Parameters>(Parameters.serializer())
        // Result serializers
        // Result
        add<Result<*>>(ResultSerializer)
        add<Result.Failure>(Result.Failure.serializer())
        add<Result.Failure.Message>(Result.Failure.Message.serializer())

        // General
        add<Identifier>(Identifier.serializer())

        // Login serializers
        add<Login>(Login.serializer())
        add<LoggedIn>(LoggedIn.serializer())
        add<RefreshToken>(RefreshToken.serializer())
        add<AccessToken>(AccessToken.serializer())
        add<IsLoggedIn>(IsLoggedIn.serializer())
        add<LoggedInAs>(LoggedInAs.serializer())
        add<Logout>(Logout.serializer())

        // Bid serializers
        add<Bid>(Bid.serializer())
        add<BidRound>(BidRound.serializer())
        add<GetRound>(GetRound.serializer())

        // Auction
        add<Auction>(Auction.serializer())
        add<CreateAuction>(CreateAuction.serializer())
        add<GetAuctions>(GetAuctions.serializer())
        add<Auctions>(Auctions.serializer())
        add<DeleteAuctions>(DeleteAuctions.serializer())
        add<UpdateAuctions>(UpdateAuctions.serializer())
        add<ConfigureAuction>(ConfigureAuction.serializer())
        add<AuctionDetails>(AuctionDetails.serializer())
        add<AuctionDetails.SolawiTuebingen>(AuctionDetails.SolawiTuebingen.serializer())

        // Auction/Bidders
        add<NewBidder>(NewBidder.serializer())
        add<Bidder>(Bidder.serializer())
        add<ImportBidders>(ImportBidders.serializer())
        add<DeleteBidders>(DeleteBidders.serializer())
        add<BidderInfo>(BidderInfo.serializer())

        // Round
        add<Round>(Round.serializer())
        add<BidRound>(BidRound.serializer())
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

        // Shares
        add<Share>(Share.serializer())
        add<Shares>(Shares.serializer())
        add<ShareType>(ShareType.serializer())
        add<ShareTypes>(ShareTypes.serializer())
        add<CreateShareType>(CreateShareType.serializer())
        add<ReadShareTypes>(ReadShareTypes.serializer())
        add<UpdateShareType>(UpdateShareType.serializer())

        // ShareOffer
        add<ShareOffer>(ShareOffer.serializer())
        add<ShareOffers>(ShareOffers.serializer())
        add<CreateShareOffer>(CreateShareOffer.serializer())
        add<ReadShareOffers>(ReadShareOffers.serializer())
        add<UpdateShareOffer>(UpdateShareOffer.serializer())

        // ShareSubscription
        add<ShareSubscription>(ShareSubscription.serializer())
        add<ShareSubscriptions>(ShareSubscriptions.serializer())
        add<CreateShareSubscription>(CreateShareSubscription.serializer())
        add<ReadShareSubscriptions>(ReadShareSubscriptions.serializer())
        add<UpdateShareSubscription>(UpdateShareSubscription.serializer())
        add<ImportShareSubscription>(ImportShareSubscription.serializer())
        add<ImportShareSubscriptions>(ImportShareSubscriptions.serializer())
        // ShareStatus
        add<ShareStatus>(ShareStatus.serializer())

        // PricingType
        add<PricingType>(PricingType.serializer())

        // Distribution Points
        add<DistributionPoint>(DistributionPoint.serializer())
        add<DistributionPoints>(DistributionPoints.serializer())
        add<CreateDistributionPoint>(CreateDistributionPoint.serializer())
        add<UpdateDistributionPoint>(UpdateDistributionPoint.serializer())
        add<DeleteDistributionPoint>(DeleteDistributionPoint.serializer())
        add<ReadDistributionPoints>(ReadDistributionPoints.serializer())
        add<ReadDistributionPoint>(ReadDistributionPoint.serializer())
        add<CreateOrUseAddress>(CreateOrUseAddress.serializer())
        add<CreateOrUseAddress.Create>(CreateOrUseAddress.Create.serializer())
        add<CreateOrUseAddress.Use>(CreateOrUseAddress.Use.serializer())

        // UserManagement
        add<CreateUser>(CreateUser.serializer())
        add<User>(User.serializer())
        add<Users>(Users.serializer())
        add<GetUsers>(GetUsers.serializer())
        add<ChangePassword>(ChangePassword.serializer())
        add<UserProfile>(UserProfile.serializer())
        add<UserProfiles>(UserProfiles.serializer())
        add<ReadUserProfiles>(ReadUserProfiles.serializer())
        add<ReadUserProfile>(ReadUserProfile.serializer())
        add<UpdateUserProfile>(UpdateUserProfile.serializer())
        add<CreateUserProfile>(CreateUserProfile.serializer())
        add<DeleteUserProfile>(DeleteUserProfile.serializer())
        add<UserProfileToImport>(UserProfileToImport.serializer())
        add<ImportUserProfiles>(ImportUserProfiles.serializer())
        add<Address>(Address.serializer())
        add<CreateAddress>(CreateAddress.serializer())
        add<UpdateAddress>(UpdateAddress.serializer())
        add<DeleteAddress>(DeleteAddress.serializer())
        add<ReadAddresses>(ReadAddresses.serializer())
        add<ReadAddress>(ReadAddress.serializer())

        // Permissions
        add<ReadRightRoleContexts>(ReadRightRoleContexts.serializer())
        add<ReadRightRoleContextsOfUser>(ReadRightRoleContextsOfUser.serializer())
        add<ReadRightRoleContextsOfUsers>(ReadRightRoleContextsOfUsers.serializer())
        add<ReadParentChildRelationsOfContexts>(ReadParentChildRelationsOfContexts.serializer())
        add<PutUserRoleContext>(PutUserRoleContext.serializer())
        add<ParentChildRelationsOfContext>(ParentChildRelationsOfContext.serializer())
        add<ParentChildRelationsOfContexts>(ParentChildRelationsOfContexts.serializer())
        add<Contexts>(Contexts.serializer())
        add<Context>(Context.serializer())
        add<Role>(Role.serializer())
        add<Right>(Right.serializer())
        add<UserContext>(UserContext.serializer())
        add<UserToContextsMap>(UserToContextsMap.serializer())

        // Application / Modules
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
        add<ReadApplicationOrganizationContextRelations>(ReadApplicationOrganizationContextRelations.serializer())
        add<ConnectApplicationToOrganization>(ConnectApplicationToOrganization.serializer())
        add<UpdateOrganizationModuleRelations>(UpdateOrganizationModuleRelations.serializer())

        add<ApiApplications>(ApiApplications.serializer())
        add<ApiApplication>(ApiApplication.serializer())
        add<ApiUserApplications>(ApiUserApplications.serializer())
        add<ApiModule>(ApiModule.serializer())
        add<ApiLifecycleStage>(ApiLifecycleStage.serializer())
        add<LifecycleStage.Empty>(LifecycleStage.Empty.serializer())
        add<LifecycleStage.Registered>(LifecycleStage.Registered.serializer())
        add<LifecycleStage.Trialing>(LifecycleStage.Trialing.serializer())
        add<LifecycleStage.Active>(LifecycleStage.Active.serializer())
        add<LifecycleStage.Paused>(LifecycleStage.Paused.serializer())
        add<LifecycleStage.PaymentFailedGracePeriod>(LifecycleStage.PaymentFailedGracePeriod.serializer())
        add<LifecycleStage.Cancelled>(LifecycleStage.Cancelled.serializer())
        add<LifecycleStage.Churned>(LifecycleStage.Churned.serializer())
        add<ApplicationContextRelations>(ApplicationContextRelations.serializer())
        add<ApplicationContextRelation>(ApplicationContextRelation.serializer())
        add<ModuleContextRelations>(ModuleContextRelations.serializer())
        add<ModuleContextRelation>(ModuleContextRelation.serializer())
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
        add<ImportMembers>(ImportMembers.serializer())

        // Banking
        add<BankAccount>(BankAccount.serializer())
        add<ReadBankAccounts>(ReadBankAccounts.serializer())
        add<ReadBankAccount>(ReadBankAccount.serializer())
        add<CreateBankAccount>(CreateBankAccount.serializer())
        add<UpdateBankAccount>(UpdateBankAccount.serializer())
        add<DeleteBankAccount>(DeleteBankAccount.serializer())

        add<FiscalYear>(FiscalYear.serializer())
        add<FiscalYears>(FiscalYears.serializer())
        add<CreateFiscalYear>(CreateFiscalYear.serializer())
        add<ReadFiscalYears>(ReadFiscalYears.serializer())
        add<ReadFiscalYear>(ReadFiscalYear.serializer())
        add<UpdateFiscalYear>(UpdateFiscalYear.serializer())
        add<DeleteFiscalYear>(DeleteFiscalYear.serializer())
    }

    /*
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
    serializers[CommentOnRound::class] = CommentOnRound.serializer()
    // Search Bidders
    serializers[BidderMails::class] = BidderMails.serializer()
    serializers[BidderData::class] = BidderData.serializer()
    serializers[SearchBidderData::class] = SearchBidderData.serializer()
    serializers[AddBidders::class] = AddBidders.serializer()
    // Shares
    serializers[Share::class] = Share.serializer()
    serializers[Shares::class] = Shares.serializer()
    serializers[ShareType::class] = ShareType.serializer()
    serializers[ShareTypes::class] = ShareTypes.serializer()
    serializers[CreateShareType::class] = CreateShareType.serializer()
    serializers[UpdateShareType::class] = UpdateShareType.serializer()
    // ShareOffer
    serializers[ShareOffer::class] = ShareOffer.serializer()
    serializers[ShareOffers::class] = ShareOffers.serializer()
    serializers[CreateShareOffer::class] = CreateShareOffer.serializer()
    serializers[UpdateShareOffer::class] = UpdateShareOffer.serializer()
    // ShareSubscription
    serializers[ShareSubscription::class] = ShareSubscription.serializer()
    serializers[ShareSubscriptions::class] = ShareSubscriptions.serializer()
    serializers[CreateShareSubscription::class] = CreateShareSubscription.serializer()
    serializers[UpdateShareSubscription::class] = UpdateShareSubscription.serializer()
    // ShareStatus
    serializers[ShareStatus::class] = ShareStatus.serializer()
    // PricingType
    serializers[PricingType::class] = PricingType.serializer()


    // Distribution Points
    serializers[DistributionPoint::class] = DistributionPoint.serializer()
    serializers[DistributionPoints::class] = DistributionPoints.serializer()
    serializers[CreateDistributionPoint::class] = CreateDistributionPoint.serializer()
    serializers[UpdateDistributionPoint::class] = UpdateDistributionPoint.serializer()
    serializers[DeleteDistributionPoint::class] = DeleteDistributionPoint.serializer()
    serializers[ReadDistributionPoints::class] = ReadDistributionPoints.serializer()
    serializers[ReadDistributionPoint::class] = ReadDistributionPoint.serializer()
    serializers[CreateOrUseAddress::class] = CreateOrUseAddress.serializer()
    serializers[CreateOrUseAddress.Create::class] = CreateOrUseAddress.Create.serializer()
    serializers[CreateOrUseAddress.Use::class] = CreateOrUseAddress.Use.serializer()

    // UserManagement
    serializers[CreateUser::class] = CreateUser.serializer()
    serializers[User::class] = User.serializer()
    serializers[Users::class] = Users.serializer()
    serializers[GetUsers::class] = GetUsers.serializer()
    serializers[ChangePassword::class] = ChangePassword.serializer()
    serializers[UserProfile::class] = UserProfile.serializer()
    serializers[UserProfiles::class] = UserProfiles.serializer()
    serializers[ReadUserProfiles::class] = ReadUserProfiles.serializer()
    serializers[ReadUserProfile::class] = ReadUserProfile.serializer()
    serializers[UpdateUserProfile::class] = UpdateUserProfile.serializer()
    serializers[CreateUserProfile::class] = CreateUserProfile.serializer()
    serializers[DeleteUserProfile::class] = DeleteUserProfile.serializer()
    serializers[UserProfileToImport::class] = UserProfileToImport.serializer()
    serializers[ImportUserProfiles::class] = ImportUserProfiles.serializer()
    serializers[Address::class] = Address.serializer()
    serializers[CreateAddress::class] = CreateAddress.serializer()
    serializers[UpdateAddress::class] = UpdateAddress.serializer()
    serializers[DeleteAddress::class] = DeleteAddress.serializer()
    serializers[ReadAddresses::class] = ReadAddresses.serializer()
    serializers[ReadAddress::class] = ReadAddress.serializer()
    // Permissions
    serializers[ReadRightRoleContexts::class] = ReadRightRoleContexts.serializer()
    serializers[ReadRightRoleContextsOfUser::class] = ReadRightRoleContextsOfUser.serializer()
    serializers[ReadRightRoleContextsOfUsers::class] = ReadRightRoleContextsOfUsers.serializer()
    serializers[ReadParentChildRelationsOfContexts::class] = ReadParentChildRelationsOfContexts.serializer()
    serializers[PutUserRoleContext::class] = PutUserRoleContext.serializer()
    serializers[ParentChildRelationsOfContext::class] = ParentChildRelationsOfContext.serializer()
    serializers[ParentChildRelationsOfContexts::class] = ParentChildRelationsOfContexts.serializer()
    serializers[Contexts::class] = Contexts.serializer()
    serializers[Context::class] = Context.serializer()
    serializers[Role::class] = Role.serializer()
    serializers[Right::class] = Right.serializer()
    serializers[UserContext::class] = UserContext.serializer()
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
    serializers[ReadApplicationOrganizationContextRelations::class] = ReadApplicationOrganizationContextRelations.serializer()
    serializers[ConnectApplicationToOrganization::class] = ConnectApplicationToOrganization.serializer()
    serializers[UpdateOrganizationModuleRelations::class] = UpdateOrganizationModuleRelations.serializer()

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
    serializers[ApplicationOrganizationRelation::class] = ApplicationOrganizationRelation.serializer()
    serializers[ApplicationOrganizationRelations::class] = ApplicationOrganizationRelations.serializer()

    // Organizations
    serializers[Organizations::class] = Organizations.serializer()
    serializers[Organization::class] = Organization.serializer()
    serializers[Member::class] = Member.serializer()

    serializers[CreateOrganization::class] = CreateOrganization.serializer()
    serializers[CreateChildOrganization::class] = CreateChildOrganization.serializer()
    serializers[ReadOrganizations::class] = ReadOrganizations.serializer()
    serializers[UpdateOrganization::class] = UpdateOrganization.serializer()
    serializers[DeleteOrganization::class] = DeleteOrganization.serializer()
    serializers[AddMember::class] = AddMember.serializer()
    serializers[RemoveMember::class] = RemoveMember.serializer()
    serializers[UpdateMember::class] = UpdateMember.serializer()
    serializers[ImportMembers::class] = ImportMembers.serializer()

    // Banking
    serializers[BankAccount::class] = BankAccount.serializer()
    serializers[ReadBankAccounts::class] = ReadBankAccounts.serializer()
    serializers[ReadBankAccount::class] = ReadBankAccount.serializer()
    serializers[CreateBankAccount::class] = CreateBankAccount.serializer()
    serializers[UpdateBankAccount::class] = UpdateBankAccount.serializer()
    serializers[DeleteBankAccount::class] = DeleteBankAccount.serializer()

    serializers[FiscalYear::class] = FiscalYear.serializer()
    serializers[FiscalYears::class] = FiscalYears.serializer()
    serializers[CreateFiscalYear::class] = CreateFiscalYear.serializer()
    serializers[ReadFiscalYears::class] = ReadFiscalYears.serializer()
    serializers[ReadFiscalYear::class] = ReadFiscalYear.serializer()
    serializers[UpdateFiscalYear::class] = UpdateFiscalYear.serializer()
    serializers[DeleteFiscalYear::class] = FiscalYear.serializer()


     */
}
