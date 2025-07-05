package org.solyton.solawi.bid.application.action.io

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.util.KtorDsl
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.ApiAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.ktorx.toMessage
import org.evoleq.math.x
import org.solyton.solawi.bid.module.authentication.exception.AuthenticationException
import org.solyton.solawi.bid.module.bid.data.api.RoundStateException
import org.solyton.solawi.bid.module.bid.exception.BidRoundException
import org.solyton.solawi.bid.module.permission.PermissionException
import org.solyton.solawi.bid.module.user.exception.UserManagementException


@KtorDsl
@Suppress("FunctionName")
suspend inline fun <reified T : Any>  Respond(): KlAction<Result<T>, Unit> = { result ->
    ApiAction { call ->
        when (result) {
            is Result.Success<T> -> try {
                call.respond(
                    HttpStatusCode.OK,
                    Json.encodeToString(ResultSerializer(), result)
                )
            } catch (exception: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    exception.message ?: "Serialization Error"
                )
            }

            is Result.Failure.Message -> call.respond(
                HttpStatusCode.InternalServerError,
                result
            )

            is Result.Failure.Exception -> with(result.transform()) {
                call.respond(first, second)
            }
        } x call
    }
}

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
        //
        //User
        is UserManagementException.UserDoesNotExist -> HttpStatusCode.Unauthorized
        is UserManagementException.WrongCredentials -> HttpStatusCode.Unauthorized

        // Permission
        is PermissionException.AccessDenied -> HttpStatusCode.Forbidden

        // RoundState
        is RoundStateException.IllegalTransition -> HttpStatusCode.BadRequest
        is RoundStateException.IllegalRoundState -> HttpStatusCode.BadRequest

        else -> HttpStatusCode.InternalServerError
    } x this.value.toMessage()
