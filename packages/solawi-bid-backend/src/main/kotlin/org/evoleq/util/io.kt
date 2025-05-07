package org.evoleq.util

// import org.solyton.solawi.bid.application.plugin.AuthenticationHolder.Companion.jwtPrincipal
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import org.evoleq.exposedx.NoMessageProvided
import org.evoleq.ktorx.result.*
import org.evoleq.math.state.times
import org.evoleq.math.x
import org.solyton.solawi.bid.application.permission.Header
import org.solyton.solawi.bid.module.authentication.exception.AuthenticationException
import org.solyton.solawi.bid.module.bid.data.api.RoundStateException
import org.solyton.solawi.bid.module.db.BidRoundException
import org.solyton.solawi.bid.module.permission.PermissionException
import org.solyton.solawi.bid.module.user.exception.UserManagementException
import java.util.*

@KtorDsl
@Suppress("FunctionName")
fun  Principle(): Action<Result<JWTPrincipal>> = ApiAction {
    call -> with(call.authentication.principal<JWTPrincipal>()) {
        when(this){
            null -> Result.Failure.Exception(AuthenticationException.InvalidOrExpiredToken)
            else -> Result.Success(this)
        }
    } x call
}




@KtorDsl
@Suppress("FunctionName")
fun <T : Any> Success(): KlAction<Result<T>, Result<Boolean>> = KlAction { _ -> Action { r -> Result.Success(true) to  r } }

@KtorDsl
@Suppress("FunctionName")
suspend inline fun <reified T : Any>  ReceiveContextual(): Action<Result<Contextual<T>>> = Principle() * {
    principle -> ApiAction { call -> principle mapSuspend  { jwtp ->
        val data = call.receive<T>()
        val userId = jwtp.payload.subject
        val context = call.request.headers[Header.CONTEXT]!!
        Contextual(UUID.fromString(userId), context, data)
    } x call }
}
@KtorDsl
@Suppress("FunctionName")
suspend inline fun   Context(): Action<Result<Contextual<Unit>>> = Principle() * {
    principle -> ApiAction { call -> principle mapSuspend  { jwtp ->

        val userId = jwtp.payload.subject
        val context = call.request.headers[Header.CONTEXT]!!
        Contextual(UUID.fromString(userId),context, Unit)
    } x call }
}

@KtorDsl
@Suppress("FunctionName")
suspend inline fun <reified T : Any>  Receive(): Action<Result<T>> = ApiAction {
    call -> try{
        Result.Success(Json.decodeFromString(Serializer<T>(), call.receive<String>()))
    } catch (e: Exception) {
        // println(e.message?:"No message provided")
        Result.Failure.Exception(e)
    } x call
}


@KtorDsl
@Suppress("FunctionName")
suspend inline fun <reified T : Any>  Receive(d: T): Action<Result<T>> = ApiAction {
        call -> try{
            Result.Return(d)
    //Result.Success(Json.decodeFromString(Serializer<T>(), call.receive<String>()))
} catch (e: Exception) {
    Result.Failure.Exception(e)
} x call
}

@KtorDsl
@Suppress("FunctionName")
suspend inline fun <reified T : Any>  Respond(): KlAction<Result<T>, Unit> = {result -> ApiAction {
        call-> when(result){
            is Result.Success<T> ->  call.respond(
                HttpStatusCode.OK,
                Json.encodeToString( ResultSerializer(), result)
            )
            is Result.Failure.Message ->  call.respond(HttpStatusCode.InternalServerError, result)
            is Result.Failure.Exception -> with(result.transform()){
                call.respond(first, second)
            }
        } x call
} }

@KtorDsl
@Suppress("FunctionName")
fun <S : Any, T: Any> Transform(f: (S)-> T): KlAction<Result<S>, Result<T>> =
    {result: Result<S> -> Action { base -> result map f x base } }

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

fun Throwable.toMessage(): Result.Failure.Message = Result.Failure.Message(message?: NoMessageProvided)

@KtorDsl
@Suppress("FunctionName")
fun <T:  Any> Fail(message: String): KlAction<Result<T>, Result<T>> = {_ -> ApiAction {
    call -> Result.Failure.Message(message) x call
}}