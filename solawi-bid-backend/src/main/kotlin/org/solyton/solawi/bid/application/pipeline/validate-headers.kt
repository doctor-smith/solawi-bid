package org.solyton.solawi.bid.application.pipeline

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.solyton.solawi.bid.application.exception.ApplicationException
import org.solyton.solawi.bid.application.permission.Context
import org.solyton.solawi.bid.application.permission.Header
import org.solyton.solawi.bid.module.user.service.credentialsAreOK

fun Application.interceptAndValidateHeaders() {
    intercept(ApplicationCallPipeline.Plugins) {
        if(call.request.httpMethod == HttpMethod.Options) {
            return@intercept
        }
        // validate that the CONTEXT Header is present
        val contextHeader = call.request.headers[Header.CONTEXT]
        if (contextHeader.isNullOrEmpty()) {

            call.response.headers.append(Header.CONTEXT, Context.Empty.value)
            call.respondText(
                Json.encodeToString(
                    ResultSerializer(),
                    Result.Failure.Message(ApplicationException.MissingContextHeader.message) as Result<String>
                ),
                status = HttpStatusCode.BadRequest
            )
            finish()
        } else {
            call.response.headers.append(Header.CONTEXT, contextHeader)
        }
    }

}
