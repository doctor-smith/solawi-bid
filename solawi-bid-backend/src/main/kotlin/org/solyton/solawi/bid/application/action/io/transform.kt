package org.solyton.solawi.bid.application.action.io

import io.ktor.http.*
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.toMessage
import org.evoleq.math.x
import org.solyton.solawi.bid.module.application.exception.ApplicationException
import org.solyton.solawi.bid.module.authentication.exception.AuthenticationException
import org.solyton.solawi.bid.module.bid.data.api.RoundStateException
import org.solyton.solawi.bid.module.bid.exception.BidRoundException
import org.solyton.solawi.bid.module.bid.exception.DistributionPointException
import org.solyton.solawi.bid.module.permission.PermissionException
import org.solyton.solawi.bid.module.permission.exception.ContextException
import org.solyton.solawi.bid.module.permission.exception.PermissionExceptionD
import org.solyton.solawi.bid.module.user.exception.AddressException
import org.solyton.solawi.bid.module.user.exception.OrganizationException
import org.solyton.solawi.bid.module.user.exception.UserManagementException


fun Result.Failure.Exception.transform(): Pair<HttpStatusCode, Result.Failure.Message> =
    when(this.value) {
        // Authentication
        is AuthenticationException.InvalidOrExpiredToken -> HttpStatusCode.Unauthorized

        // BidRound
        is BidRoundException.RoundNotStarted -> HttpStatusCode.Conflict
        is BidRoundException.NoSuchRound,
        is BidRoundException.NoSuchRoundState,
        is BidRoundException.NoSuchAuction -> HttpStatusCode.NotFound
        is BidRoundException.UnregisteredBidder,
        is BidRoundException.RegisteredBidderNotPartOfTheAuction,
        is BidRoundException.AuctionAccepted,
        is BidRoundException.LinkNotPresent, -> HttpStatusCode.Forbidden
        is BidRoundException.IllegalNumberOfParts -> HttpStatusCode.BadRequest
        is BidRoundException.MissingBidderDetails -> HttpStatusCode.NotFound
        // RoundState
        is RoundStateException.IllegalTransition -> HttpStatusCode.BadRequest
        is RoundStateException.IllegalRoundState -> HttpStatusCode.BadRequest

        // DistributionPoint
        is DistributionPointException.NoSuchDistributionPoint -> HttpStatusCode.NotFound
        is DistributionPointException.DuplicateNameInOrganization -> HttpStatusCode.Conflict

        //User
        is UserManagementException.UserDoesNotExist -> HttpStatusCode.Unauthorized
        is UserManagementException.WrongCredentials -> HttpStatusCode.Unauthorized

        // Organization
        is OrganizationException.NoSuchOrganization -> HttpStatusCode.NotFound
        is OrganizationException.NoSuchChildOrganization -> HttpStatusCode.NotFound
        is OrganizationException.DuplicateMember -> HttpStatusCode.Conflict
        // Address
        is AddressException.NoSuchAddress -> HttpStatusCode.NotFound

        // Permission
        is PermissionException.AccessDenied -> HttpStatusCode.Forbidden

        // todo:dev how to handle these permission exceptions?
        is PermissionException.NoSuchContext -> HttpStatusCode.Forbidden
        is PermissionException.NoSuchRight -> HttpStatusCode.Forbidden
        is PermissionExceptionD.NoSuchRole -> HttpStatusCode.NotFound
        is PermissionExceptionD.DuplicateRoleName -> HttpStatusCode.Conflict
        is PermissionExceptionD.DuplicateRightName -> HttpStatusCode.Conflict

        is ContextException.NoContextProvided -> HttpStatusCode.BadRequest

        // Application (Module!)
        is ApplicationException.NoSuchApplication -> HttpStatusCode.NotFound
        is ApplicationException.DuplicateApplicationName -> HttpStatusCode.Conflict
        // App lifecycle
        is ApplicationException.ApplicationRegistrationImpossible -> HttpStatusCode.Conflict
        is ApplicationException.ApplicationTrialImpossible -> HttpStatusCode.Conflict
        is ApplicationException.ApplicationSubscriptionImpossible -> HttpStatusCode.Conflict
        // Module lifecycle
        is ApplicationException.ModuleRegistrationImpossible -> HttpStatusCode.Conflict
        is ApplicationException.ModuleTrialImpossible -> HttpStatusCode.Conflict
        is ApplicationException.ModuleSubscriptionImpossible -> HttpStatusCode.Conflict
        // Applications and Organizations
        is ApplicationException.AlreadyConnectedToOrganization -> HttpStatusCode.Conflict
        // Application / Module Access
        is ApplicationException.UserNotRegisteredForApplication -> HttpStatusCode.Forbidden
        is ApplicationException.UserNotRegisteredForModule -> HttpStatusCode.Forbidden
        is ApplicationException.UserNotRegisteredForModules -> HttpStatusCode.Forbidden
        // Bundles
        is ApplicationException.DuplicateBundleName -> HttpStatusCode.Conflict
        is ApplicationException.NoSuchAppsOrModules -> HttpStatusCode.NotFound
        is ApplicationException.CannotDeleteBundle -> HttpStatusCode.Conflict

        else -> HttpStatusCode.InternalServerError
    } x this.value.toMessage()
