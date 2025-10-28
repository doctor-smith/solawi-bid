package org.solyton.solawi.bid.module.usermanagement.routing.util

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.solyton.solawi.bid.application.api.solawiApi
import org.solyton.solawi.bid.application.permission.Header
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganizations
import org.solyton.solawi.bid.module.user.data.api.organization.CreateChildOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.CreateOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.ReadOrganizations
import org.solyton.solawi.bid.module.user.data.api.organization.UpdateOrganization

suspend fun HttpClient.createOrganization(data: CreateOrganization,token: String, contextId: String, assert: HttpResponse.()->Unit = {}): Result<ApiOrganization> {
    val response = post(solawiApi[CreateOrganization::class]!!.url) {
        header(Header.CONTEXT, contextId)
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        header(HttpHeaders.Authorization, "Bearer $token")
        setBody(Json.encodeToString(CreateOrganization.serializer(),data))
    }
    assert(response)
    return Json.decodeFromString(ResultSerializer<ApiOrganization>(), response.bodyAsText())
}

suspend fun HttpClient.createChildOrganization(data: CreateChildOrganization, token: String, contextId: String, assert: HttpResponse.()->Unit = {}): Result<ApiOrganization> {
    val response = post(solawiApi[CreateChildOrganization::class]!!.url) {
        header(Header.CONTEXT, contextId)
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        header(HttpHeaders.Authorization, "Bearer $token")
        setBody(Json.encodeToString(CreateChildOrganization.serializer(),data))
    }
    assert(response)
    return Json.decodeFromString(ResultSerializer<ApiOrganization>(), response.bodyAsText())
}

suspend fun HttpClient.readOrganizations(token: String, contextId: String, assert: HttpResponse.()->Unit = {}): Result<ApiOrganizations> {
    val response = get(solawiApi[ReadOrganizations::class]!!.url) {
        header(Header.CONTEXT, contextId)
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        header(HttpHeaders.Authorization, "Bearer $token")
    }
    assert(response)
    return Json.decodeFromString(ResultSerializer<ApiOrganizations>(), response.bodyAsText())
}

suspend fun HttpClient.updateOrganization(data: UpdateOrganization, token: String, contextId: String, assert: HttpResponse.()->Unit = {}): Result<ApiOrganization> {
    val response = patch(solawiApi[UpdateOrganization::class]!!.url) {
        header(Header.CONTEXT, contextId)
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        header(HttpHeaders.Authorization, "Bearer $token")
        setBody(Json.encodeToString(UpdateOrganization.serializer(),data))
    }
    assert(response)
    return Json.decodeFromString(ResultSerializer<ApiOrganization>(), response.bodyAsText())
}
