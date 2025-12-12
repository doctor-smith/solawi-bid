package org.solyton.solawi.bid.module.application

import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.headers.Header
import org.solyton.solawi.bid.module.authentication.data.api.Login
import org.solyton.solawi.bid.module.permission.data.api.ApiContext

const val USERNAME = "developer@solyton.org"
const val PASSWORD = "pass1234"

suspend fun ApplicationTestBuilder.testCall(accessToken: String? = null, url: String = "test") = client.get(url) {
    header(HttpHeaders.ContentType, ContentType.Application.Json)
    header(Header.CONTEXT, "EMPTY")
    if (accessToken != null) {
        header(HttpHeaders.Authorization, "Bearer $accessToken")
    }
}

suspend fun ApplicationTestBuilder.login(username: String, password: String) = client.post("/login") {
    header(HttpHeaders.ContentType, ContentType.Application.Json)
    header(Header.CONTEXT, "LOGIN")
    setBody(
        Json.encodeToString(
            Login.serializer(),
            Login(
                username = username,
                password = password
            )
        )
    )
}

suspend fun ApplicationTestBuilder.getRootContextByName(
    contextName: String,
    accessToken: String
): ApiContext {
    val contextResponse = client.get("setup/root-context-by-name") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        header(HttpHeaders.Authorization, "Bearer $accessToken")
        header(Header.CONTEXT, contextName)
    }.bodyAsText()

    val context = Json.decodeFromString<ApiContext>(
        contextResponse
    )
    return context
}
