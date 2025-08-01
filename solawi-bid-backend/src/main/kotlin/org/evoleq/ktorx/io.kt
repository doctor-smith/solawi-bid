package org.evoleq.ktorx

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import org.evoleq.exposedx.NO_MESSAGE_PROVIDED
import org.evoleq.ktorx.result.*
import org.evoleq.math.state.times
import org.evoleq.math.x
import org.solyton.solawi.bid.application.permission.Header
import org.solyton.solawi.bid.module.authentication.exception.AuthenticationException
import org.solyton.solawi.bid.module.bid.data.api.RoundStateException
import org.solyton.solawi.bid.module.bid.exception.BidRoundException
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
@Suppress("FunctionName", "UnsafeCallOnNullableType")
suspend inline fun   Context(): Action<Result<Contextual<Unit>>> = Principle() * {
    principle -> ApiAction { call -> principle mapSuspend  { jwtp ->

        val userId = jwtp.payload.subject
        val context = call.request.headers[Header.CONTEXT]!!
        Contextual(UUID.fromString(userId),context, Unit)
    } x call }
}

@KtorDsl
@Suppress("FunctionName", "RedundantSuspendModifier")
// suspend
inline fun <reified T : Any>  Receive(): Action<Result<T>> = ApiAction {
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
    } catch (e: Exception) {
        Result.Failure.Exception(e)
    } x call
}

@KtorDsl
@Suppress("FunctionName")
fun <S : Any, T: Any> Transform(f: (S)-> T): KlAction<Result<S>, Result<T>> =
    {result: Result<S> -> Action { base -> result map f x base } }

fun Throwable.toMessage(): Result.Failure.Message = Result.Failure.Message(message?: NO_MESSAGE_PROVIDED)

@KtorDsl
@Suppress("FunctionName")
fun <T:  Any> Fail(message: String): KlAction<Result<T>, Result<T>> = {_ -> ApiAction {
    call -> Result.Failure.Message(message) x call
}}
