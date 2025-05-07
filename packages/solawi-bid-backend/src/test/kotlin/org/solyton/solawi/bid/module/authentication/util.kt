package org.solyton.solawi.bid.module.authentication

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.solyton.solawi.bid.application.permission.Header
import org.solyton.solawi.bid.module.authentication.data.api.Login
import org.solyton.solawi.bid.module.authentication.data.api.RefreshToken

const val username = "developer@alpha-structure.com"
const val password = "pass1234"

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

suspend fun ApplicationTestBuilder.refreshToken(username: String, accessToken: String) = client.post("refresh") {
    header(HttpHeaders.ContentType, ContentType.Application.Json)
    setBody(
        Json.encodeToString(
            RefreshToken.serializer(),
            RefreshToken(
                username = username,
                refreshToken = accessToken
            )
        )
    )
}