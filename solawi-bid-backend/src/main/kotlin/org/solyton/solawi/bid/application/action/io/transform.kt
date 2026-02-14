package org.solyton.solawi.bid.application.action.io

import io.ktor.http.*
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.toMessage
import org.evoleq.math.x
import org.solyton.solawi.bid.module.application.exception.ApplicationException
import org.solyton.solawi.bid.module.authentication.exception.AuthenticationException
import org.solyton.solawi.bid.module.banking.exception.BankAccountsException
import org.solyton.solawi.bid.module.banking.exception.FiscalYearException
import org.solyton.solawi.bid.module.bid.data.api.RoundStateException
import org.solyton.solawi.bid.module.bid.exception.BidRoundException
import org.solyton.solawi.bid.module.distribution.exception.DistributionPointException
import org.solyton.solawi.bid.module.permission.PermissionException
import org.solyton.solawi.bid.module.permission.exception.ContextException
import org.solyton.solawi.bid.module.permission.exception.PermissionExceptionD
import org.solyton.solawi.bid.module.shares.exception.ShareException
import org.solyton.solawi.bid.module.shares.exception.ShareStatusException
import org.solyton.solawi.bid.module.user.exception.AddressException
import org.solyton.solawi.bid.module.user.exception.OrganizationException
import org.solyton.solawi.bid.module.user.exception.UserManagementException
import org.solyton.solawi.bid.application.exception.ApplicationException as AppException

@Suppress("CognitiveComplexMethod", "CyclomaticComplexMethod")
fun Result.Failure.Exception.transform(): Pair<HttpStatusCode, Result.Failure.Message> =
    when(this.value) {
        // Main Application
        is AppException.MissingContextHeader -> HttpStatusCode.BadRequest

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
        is DistributionPointException -> when(value as DistributionPointException) {
            is DistributionPointException.NoSuchDistributionPoint -> HttpStatusCode.NotFound
            is DistributionPointException.DuplicateNameInOrganization -> HttpStatusCode.Conflict
        }

        // Share
        is ShareException -> when(value as ShareException){
            is ShareException.NoSuchShareType -> HttpStatusCode.NotFound
            is ShareException.InvalidNumberOfShares -> HttpStatusCode.BadRequest
            is ShareException.NoSuchShareOffer -> HttpStatusCode.NotFound
            is ShareException.InvalidPricePerShare -> HttpStatusCode.BadRequest
            is ShareException.ProviderMismatch -> HttpStatusCode.BadRequest
            is ShareException.FiscalYearMismatch -> HttpStatusCode.BadRequest
            is ShareException.NoSuchShareSubscription -> HttpStatusCode.NotFound
            is ShareException.InvalidPricing -> HttpStatusCode.BadRequest
            is ShareException.DuplicateNameOfShareTypeAtProvider -> HttpStatusCode.Conflict
            is ShareException.CannotDeleteShareOffer -> HttpStatusCode.Conflict
            is ShareException.CannotDeleteShareSubscription -> HttpStatusCode.Conflict
            is ShareException.CannotDeleteShareType -> HttpStatusCode.Conflict
            is ShareException.CannotDeleteShareTypesOfProvider -> HttpStatusCode.Conflict
            is ShareException.MissingShareSubscriptionOfUser -> HttpStatusCode.NotFound
        }

        is ShareStatusException -> when(value as ShareStatusException) {
            is ShareStatusException.NoSuchStatus -> HttpStatusCode.NotFound
            is ShareStatusException.ForbiddenChangeReason -> HttpStatusCode.Forbidden
            is ShareStatusException.InvalidHistoryEntry -> HttpStatusCode.BadRequest
            is ShareStatusException.MissingTransitionPermission -> HttpStatusCode.Forbidden
            is ShareStatusException.NoInitialState -> HttpStatusCode.BadRequest
            is ShareStatusException.NoSuchStatusTransition -> HttpStatusCode.NotFound
            is ShareStatusException.TransitionNotAllowedForModifier -> HttpStatusCode.Forbidden
        }

        // Banking
        is BankAccountsException -> when(value as BankAccountsException) {
            is BankAccountsException.NoSuchBankAccount -> HttpStatusCode.NotFound
            is BankAccountsException.InvalidBic -> HttpStatusCode.BadRequest
            is BankAccountsException.InvalidIban -> HttpStatusCode.BadRequest
            is BankAccountsException.InvalidBicCountryCode -> HttpStatusCode.BadRequest
            is BankAccountsException.BicNotInEU -> HttpStatusCode.BadRequest
        }
        is FiscalYearException -> when(value as FiscalYearException) {
            is FiscalYearException.NoSuchFiscalYear -> HttpStatusCode.NotFound
            is FiscalYearException.DurationTooLong -> HttpStatusCode.BadRequest
            is FiscalYearException.FiscalYearMismatch -> HttpStatusCode.BadRequest
            is FiscalYearException.Overlaps -> HttpStatusCode.BadRequest
            is FiscalYearException.StartAfterEnd -> HttpStatusCode.BadRequest
            is FiscalYearException.TooManyPerYear -> HttpStatusCode.BadRequest
        }

        //User
        is UserManagementException -> when(value as UserManagementException) {
            is UserManagementException.UserDoesNotExist -> HttpStatusCode.Unauthorized
            is UserManagementException.WrongCredentials -> HttpStatusCode.Unauthorized
            is UserManagementException.NoSuchUserProfile -> HttpStatusCode.NotFound
            is UserManagementException.UsersDoNotExist -> HttpStatusCode.NotFound
        }
        // Organization
        is OrganizationException -> when(value as OrganizationException) {
            is OrganizationException.NoSuchOrganization -> HttpStatusCode.NotFound
            is OrganizationException.NoSuchChildOrganization -> HttpStatusCode.NotFound
            is OrganizationException.DuplicateMember -> HttpStatusCode.Conflict
            is OrganizationException.CannotDeleteOrganization -> HttpStatusCode.Conflict
            is OrganizationException.NoRoot -> HttpStatusCode.BadRequest
        }
        // Address
        is AddressException -> when(value as AddressException) {
            is AddressException.NoSuchAddress -> HttpStatusCode.NotFound
        }
        // Permission
        is PermissionException.AccessDenied -> HttpStatusCode.Forbidden

        // todo:dev how to handle these permission exceptions?
        is PermissionException.NoSuchContext -> HttpStatusCode.Forbidden
        is PermissionException.NoSuchRight -> HttpStatusCode.Forbidden
        is PermissionExceptionD.NoSuchRole -> HttpStatusCode.NotFound
        is PermissionExceptionD.DuplicateRoleName -> HttpStatusCode.Conflict
        is PermissionExceptionD.DuplicateRightName -> HttpStatusCode.Conflict

        is ContextException.NoContextProvided -> HttpStatusCode.BadRequest

        is ApplicationException -> when(value as ApplicationException) {
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

            // Sort!
            is ApplicationException.DuplicateLifecycleStage -> HttpStatusCode.Conflict
            is ApplicationException.DuplicateLifecycleTransition -> HttpStatusCode.Conflict
            is ApplicationException.DuplicateModuleName -> HttpStatusCode.Conflict
            is ApplicationException.ForbiddenLifecycleTransition -> HttpStatusCode.Forbidden
            is ApplicationException.NoSuchBundle -> HttpStatusCode.NotFound
            is ApplicationException.NoSuchLifecycleStage -> HttpStatusCode.NotFound
            is ApplicationException.NoSuchModule -> HttpStatusCode.NotFound
        }
        else -> HttpStatusCode.InternalServerError
    } x this.value.toMessage()
