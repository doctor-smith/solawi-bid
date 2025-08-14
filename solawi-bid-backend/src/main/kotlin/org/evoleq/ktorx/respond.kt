package org.evoleq.ktorx

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.math.x


@KtorDsl
@Suppress("FunctionName")
suspend inline fun <reified T : Any>  Respond(
    noinline transformException: Result.Failure.Exception.() -> Pair<HttpStatusCode, Result.Failure.Message>
): KlAction<Result<T>, Unit> = { result ->
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

            is Result.Failure.Exception -> with(result.transformException()) {
                call.respond(first, second)
            }
        } x call
    }
}
