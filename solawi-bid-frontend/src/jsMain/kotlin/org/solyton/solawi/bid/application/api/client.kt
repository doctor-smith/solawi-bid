package org.solyton.solawi.bid.application.api

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.result.Result
import org.evoleq.math.MathDsl
import org.solyton.solawi.bid.application.data.Application

@MathDsl
fun Application.client(loggedIn: Boolean = true) = HttpClient(Js) {
    defaultRequest {
        header(HttpHeaders.ContentType,ContentType.Application.Json)
        if(loggedIn) {
            header(HttpHeaders.Authorization, "Bearer ${userData.accessToken}")
        }
    }
}

fun <S: Any,T: Any> HttpClient.post(url: String, port: Int, serializer: KSerializer<S>, deserializer: KSerializer<Result<T>>): suspend (S)-> Result<T> = { s: S ->
    with(post(url) {
        this.port = port
        if (s::class != Unit::class) {
            val serialized = Json.encodeToString(serializer, s)
            setBody(serialized)
        }
    }) {
        decode(deserializer)
    } }

fun <S: Any,T: Any> HttpClient.get(url: String, port: Int, serializer: KSerializer<S>, deserializer: KSerializer<Result<T>>): suspend (S)-> Result<T> = { s: S ->
    with(get(url) {
        this.port = port

    }) {
        decode(deserializer)
    } }


suspend fun <T : Any> HttpResponse.decode(deserializer : KSerializer<Result<T>>): Result<T> = try{
        Json.decodeFromString(deserializer, this.bodyAsText())
    } catch(ex: Exception){
        Json.decodeFromString(Result.Failure.Message.serializer(), this.bodyAsText())
    }
