package org.solyton.solawi.bid.application.api


import org.evoleq.ktorx.api.Api
import org.solyton.solawi.bid.module.application.data.*
import org.solyton.solawi.bid.module.authentication.data.api.*
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.permission.data.api.*
import org.solyton.solawi.bid.module.user.data.api.*
import org.solyton.solawi.bid.module.user.data.api.organization.AddMember
import org.solyton.solawi.bid.module.user.data.api.organization.CreateChildOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.CreateOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.DeleteOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.Organization
import org.solyton.solawi.bid.module.user.data.api.organization.Organizations
import org.solyton.solawi.bid.module.user.data.api.organization.ReadOrganizations
import org.solyton.solawi.bid.module.user.data.api.organization.RemoveMember
import org.solyton.solawi.bid.module.user.data.api.organization.UpdateMember
import org.solyton.solawi.bid.module.user.data.api.organization.UpdateOrganization

val solawiApi by lazy {
    // Authentication
    Api{
        post<Login, LoggedIn>(
            key = Login::class,
            url = "login"
        )
        post<RefreshToken,LoggedIn>(
            key = RefreshToken::class,
            url = "refresh"
        )
        patch<Logout, Unit>(
            key = Logout::class,
            url = "logout"
        )
        patch<IsLoggedIn, LoggedInAs>(
            key = IsLoggedIn::class,
            url = "is-logged-in"
        )

        // Permissions
        patch<ReadRightRoleContextsOfUser, Contexts>(
            key = ReadRightRoleContextsOfUser::class,
            url = "permissions/user/role-right-contexts"
        )
        patch<ReadRightRoleContextsOfUsers, UserToContextsMap>(
            key = ReadRightRoleContextsOfUsers::class,
            url = "permissions/users/role-right-contexts"
        )
        patch<ReadParentChildRelationsOfContexts, ParentChildRelationsOfContext>(
            key = ReadParentChildRelationsOfContexts::class,
            url = "permissions/contexts/parent-child-relations"
        )
        patch<ReadRightRoleContexts, Contexts>(
            key = ReadRightRoleContexts::class,
            url = "permissions/contexts/roles-and-rights"
        )
        // Auction
        post<CreateAuction, Auction>(
            key = CreateAuction::class,
            url = "auction/create"
        )
        get<GetAuctions, List<Auction>>(
            key = GetAuctions::class,
            url = "auction/all"
        )
        delete<DeleteAuctions, List<Auction>>(
            key = DeleteAuctions::class,
            url ="auction/delete"
        )
        patch<UpdateAuctions, List<Auction>>(
            key = UpdateAuctions::class,
            url = "auction/update"
        )
        patch<ConfigureAuction, Auction>(
            key = ConfigureAuction::class,
            url = "auction/configure"
        )
        post<ImportBidders, Auction>(
            key = ImportBidders::class,
            url = "auction/bidder/import"
        )
        delete<DeleteBidders, Auction>(
            key = DeleteBidders::class,
            url = "auction/bidder/delete"
        )
        // Round
        get<GetRound, Round>(
            key = GetRound::class,
            url = "round/create---nonsense"
        )
        post<CreateRound, Round>(
            key = CreateRound::class,
            url = "round/create"
        )
        patch<ChangeRoundState, Round>(
            key = ChangeRoundState::class,
            url = "round/change-state"
        )
        patch<ExportBidRound, BidRoundResults>(
            key = ExportBidRound::class,
            url = "round/export-results"
        )
        patch<EvaluateBidRound, BidRoundEvaluation>(
            key = EvaluateBidRound::class,
            url = "round/evaluate"
        )
        patch<PreEvaluateBidRound, BidRoundPreEvaluation>(
            key = PreEvaluateBidRound::class,
            url = "round/pre-evaluate"
        )
        patch<AcceptRound, AcceptedRound>(
            key  = AcceptRound::class,
            url = "auction/accept-round"
        )
        post<CommentOnRound, RoundComments>(
            key = CommentOnRound::class,
            url = "round/add-comment"
        )

        // Auction bid
        post<Bid,BidRound> (
            key = Bid::class,
            url = "bid/send"
        )
        // Search Bidders
        patch<SearchBidderData, BidderMails>(
            key = SearchBidderData::class,
            url = "bidders/search"
        )
        post<AddBidders, Unit>(
            key = AddBidders::class, "bidders/add"
        )

        // User Management
        post<CreateUser, User>(
            key = CreateUser::class,
            url = "users/create"
        )
        get<GetUsers, Users>(
            key = GetUsers::class,
            url = "users/all"
        )
        patch<ChangePassword, User>(
            key = ChangePassword::class,
            url = "users/change-password"
        )

        post<RegisterUser, UserRegistered> (
            key = RegisterUser::class,
            url = "user/register"
        )

        post<SendMailForRegistrationConfirmation, MailForRegistrationConfirmationSent>(
            key = SendMailForRegistrationConfirmation::class,
            url = "user/send-registration-mail"
        )

        // Applications and Modules
        get<ReadApplications, ApiApplications>(
            key = ReadApplications::class,
            url = "applications/all"
        )
        get<ReadPersonalUserApplications, Applications>(
            key = ReadPersonalUserApplications::class,
            url = "applications/personal/all"
        )
        get<ReadPersonalApplicationContextRelations, ApplicationContextRelations>(
            key = ReadPersonalApplicationContextRelations::class,
            url = "applications/personal/application-context-relations"
        )
        get<ReadPersonalModuleContextRelations, ModuleContextRelations>(
            key = ReadPersonalModuleContextRelations::class,
            url = "applications/modules/personal/module-context-relations"
        )
        patch<ReadUserApplications, UserApplications>(
            key = ReadUserApplications::class,
            url = "applications/management/users"
        )
        patch<RegisterForApplications, ApiApplications>(
            key = RegisterForApplications::class,
            url = "applications/personal/register"
        )
        patch<StartTrialsOfApplications, ApiApplications>(
            key = StartTrialsOfApplications::class,
            url = "applications/personal/trial"
        )
        patch<SubscribeApplications, ApiApplications>(
            key = SubscribeApplications::class,
            url = "applications/personal/subscribe"
        )
        patch<RegisterForModules, ApiApplications>(
            key = RegisterForModules::class,
            url = "applications/modules/personal/register"
        )
        patch<StartTrialsOfModules, ApiApplications>(
            key = StartTrialsOfModules::class,
            url = "applications/modules/personal/trial"
        )
        patch<SubscribeModules, ApiApplications>(
            key = SubscribeModules::class,
            url = "applications/modules/personal/subscribe"
        )
        post<ConnectApplicationToOrganization, ApplicationOrganizationRelations>(
            key = ConnectApplicationToOrganization::class,
            url = "applications/personal/connect-organization"
        )
        patch<UpdateOrganizationModuleRelations, ApplicationOrganizationRelations>(
            key = UpdateOrganizationModuleRelations::class,
            url = "applications/personal/update-organization-module-relations"
        )
        get<ReadApplicationOrganizationContextRelations,  ApplicationOrganizationRelations>(
            key = ReadApplicationOrganizationContextRelations::class,
            url = "applications/personal/organization-context-relations"
        )
        // Organizations
        post<CreateOrganization, Organization>(
            key = CreateOrganization::class,
            url = "organizations/create"
        )
        post<CreateChildOrganization, Organization>(
            key = CreateChildOrganization::class,
            url = "organizations/create-child"
        )
        get<ReadOrganizations, Organizations>(
            key = ReadOrganizations::class,
            url = "organizations/all"
        )
        patch<UpdateOrganization, Organization>(
            key = UpdateOrganization::class,
            url = "organizations/update"
        )
        delete<DeleteOrganization, Organizations>(
            key = DeleteOrganization::class,
            url = "organizations/delete"
        )
        post<AddMember, Organization>(
            key = AddMember::class,
            url = "organizations/members/add"
        )
        patch<UpdateMember, Organization>(
            key = UpdateMember::class,
            url = "organizations/members/update"
        )
        delete<RemoveMember, Organization>(
            key = RemoveMember::class,
            url = "organizations/members/remove"
        )
    }
}
