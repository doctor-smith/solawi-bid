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
import org.solyton.solawi.bid.module.bid.data.values.AuctionId
import org.solyton.solawi.bid.module.distribution.data.api.*
import org.solyton.solawi.bid.module.permission.data.ContextId
import org.solyton.solawi.bid.module.permission.data.ContextName
import org.solyton.solawi.bid.module.permission.data.RightId
import org.solyton.solawi.bid.module.permission.data.RightName
import org.solyton.solawi.bid.module.permission.data.RoleId
import org.solyton.solawi.bid.module.permission.data.RoleName
import org.solyton.solawi.bid.module.permission.data.api.*
import org.solyton.solawi.bid.module.shares.data.api.*
import org.solyton.solawi.bid.module.shares.data.values.ShareOfferId
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId
import org.solyton.solawi.bid.module.shares.data.values.ShareTypeId
import org.solyton.solawi.bid.module.user.data.api.*
import org.solyton.solawi.bid.module.user.data.api.organization.*
import org.solyton.solawi.bid.module.user.data.api.userprofile.*
import org.solyton.solawi.bid.module.values.*

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
        add<AccessorId>(AccessorId.serializer())
        add<LegalEntityId>(LegalEntityId.serializer())
        add<UserProfileId>(UserProfileId.serializer())
        add<ModifierId>(ModifierId.serializer())
        add<CreatorId>(CreatorId.serializer())
        add<Username>(Username.serializer())
        add<Firstname>(Firstname.serializer())
        add<Lastname>(Lastname.serializer())
        add<Title>(Title.serializer())
        add<PhoneNumber>(PhoneNumber.serializer())
        add<Description>(Description.serializer())
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
        add<ImportBiddersFromOrganization>(ImportBiddersFromOrganization.serializer())
        add<DeleteBidders>(DeleteBidders.serializer())
        add<BidderInfo>(BidderInfo.serializer())
        add<AuctionId>(AuctionId.serializer())
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
        add<UpdateShareStatus>(UpdateShareStatus.serializer())
        add<ChangedBy>(ChangedBy.serializer())
        add<ChangeReason>(ChangeReason.serializer())

        // Share Management Values
        add<ShareSubscriptionId>(ShareSubscriptionId.serializer())
        add<ShareTypeId>(ShareTypeId.serializer())
        add<ShareOfferId>(ShareOfferId.serializer())

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
        add<PutRoleRightContext>(PutRoleRightContext.serializer())
        add<ContextName>(ContextName.serializer())
        add<RoleName>(RoleName.serializer())
        add<RightName>(RightName.serializer())
        add<ContextId>(ContextId.serializer())
        add<RightId>(RightId.serializer())
        add<RoleId>(RoleId.serializer())
        add<CreateRole>(CreateRole.serializer())
        add<CreateRight>(CreateRight.serializer())
        add<UpdateRole>(UpdateRole.serializer())
        add<UpdateRight>(UpdateRight.serializer())
        add<DeleteRole>(DeleteRole.serializer())
        add<DeleteRight>(DeleteRight.serializer())
        add<CreateRights>(CreateRights.serializer())
        add<CreateRoles>(CreateRoles.serializer())

        // Application / Modules
        add<CreateApplication>(CreateApplication.serializer())
        add<UpdateApplication>(UpdateApplication.serializer())
        add<DeleteApplication>(DeleteApplication.serializer())
        add<AddModulesToApplication>(AddModulesToApplication.serializer())
        add<RemoveModulesFromApplication>(RemoveModulesFromApplication.serializer())
        add<CreateModule>(CreateModule.serializer())
        add<CreateModuleOnTheFly>(CreateModuleOnTheFly.serializer())
        add<UpdateModule>(UpdateModule.serializer())
        add<DeleteModule>(DeleteModule.serializer())
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

        add<ApplicationId>(ApplicationId.serializer())
        add<ModuleId>(ModuleId.serializer())
        add<ApplicationName>(ApplicationName.serializer())
        add<ModuleName>(ModuleName.serializer())
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
        add<AccountType>(AccountType.serializer())
        add<BankAccounts>(BankAccounts.serializer())
        add<ReadBankAccounts>(ReadBankAccounts.serializer())
        add<ReadBankAccount>(ReadBankAccount.serializer())
        add<CreateBankAccount>(CreateBankAccount.serializer())
        add<UpdateBankAccount>(UpdateBankAccount.serializer())
        add<DeleteBankAccount>(DeleteBankAccount.serializer())
        add<ImportBankAccount>(ImportBankAccount.serializer())
        add<ImportBankAccounts>(ImportBankAccounts.serializer())
        add<FiscalYear>(FiscalYear.serializer())
        add<FiscalYears>(FiscalYears.serializer())
        add<CreateFiscalYear>(CreateFiscalYear.serializer())
        add<ReadFiscalYears>(ReadFiscalYears.serializer())
        add<ReadFiscalYear>(ReadFiscalYear.serializer())
        add<UpdateFiscalYear>(UpdateFiscalYear.serializer())
        add<DeleteFiscalYear>(DeleteFiscalYear.serializer())

        // Legal Entities
        add<LegalEntity>(LegalEntity.serializer())
        add<LegalEntityType>(LegalEntityType.serializer())
        add<LegalEntities>(LegalEntities.serializer())
        add<CreateLegalEntity>(CreateLegalEntity.serializer())
        add<ReadLegalEntity>(ReadLegalEntity.serializer())
        add<ReadLegalEntitiesOfProvider>(ReadLegalEntitiesOfProvider.serializer())
        add<UpdateLegalEntity>(UpdateLegalEntity.serializer())
        add<DeleteLegalEntity>(DeleteLegalEntity.serializer())
    }
}
