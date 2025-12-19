package org.solyton.solawi.bid.module.bid.routing.util

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.headers.Header
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.solyton.solawi.bid.module.application.data.ApiApplications
import kotlin.test.assertIs

suspend fun HttpClient.getApplications(
    accessToken: String,
    applicationContextId: String
): Result.Success<ApiApplications> {
    val applicationsResponse = get("applications/all") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        header(HttpHeaders.Authorization, "Bearer $accessToken")
        header(Header.CONTEXT, applicationContextId)
    }
    val applicationsResult: Result<ApiApplications> = Json.decodeFromString(
        ResultSerializer(),
        applicationsResponse.bodyAsText()
    )
    assertIs<Result.Success<ApiApplications>>(applicationsResult, "HttpClient.getApplications failed")

    return applicationsResult
}



