package org.evoleq.ktorx.client

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.context.data.Contextual
import org.evoleq.ktorx.result.map
import org.evoleq.ktorx.headers.Header
import org.evoleq.permission.EmptyContext


fun <S: Any,T: Any> HttpClient.post(url: String, port: Int, serializer: KSerializer<S>, deserializer: KSerializer<Result<T>>): suspend (S)-> Result<Contextual<T>> = { s: S ->
    with(post(url) {
        this.port = port
        if (s::class != Unit::class) {
            val serialized = Json.encodeToString(serializer, s)
            setBody(serialized)
        }
    }) {
        decode(deserializer)
    } }

fun <S: Any,T: Any> HttpClient.get(url: String, port: Int, /* serializer: KSerializer<S>,*/ deserializer: KSerializer<Result<T>>): suspend (S)-> Result<Contextual<T>> = { _: S ->
    with(get(url) {
        this.port = port

    }) {
        decode(deserializer)
    } }

fun <S: Any,T: Any> HttpClient.put(url: String, port: Int, serializer: KSerializer<S>, deserializer: KSerializer<Result<T>>): suspend (S)-> Result<Contextual<T>> = { s: S ->
    with(put(url) {
        this.port = port
        if (s::class != Unit::class) {
            val serialized = Json.encodeToString(serializer, s)
            setBody(serialized)
        }
    }) {
        decode(deserializer)
    } }

fun <S: Any,T: Any> HttpClient.patch(url: String, port: Int, serializer: KSerializer<S>, deserializer: KSerializer<Result<T>>): suspend (S)-> Result<Contextual<T>> = { s: S ->
    with(patch(url) {
        this.port = port
        if (s::class != Unit::class) {
            val serialized = Json.encodeToString(serializer, s)
            setBody(serialized)
        }
    }) {
        decode(deserializer)
    } }

fun <S: Any,T: Any> HttpClient.delete(url: String, port: Int, serializer: KSerializer<S>, deserializer: KSerializer<Result<T>>): suspend (S)-> Result<Contextual<T>> = { s: S ->
    with(delete(url) {
        this.port = port
        if (s::class != Unit::class) {
            val serialized = Json.encodeToString(serializer, s)
            setBody(serialized)
        }
    }) {
        decode(deserializer)
    } }


suspend fun <T : Any> HttpResponse.decode(deserializer : KSerializer<Result<T>>): Result<Contextual<T>> = try{
    Json.decodeFromString(deserializer, this.bodyAsText())
} catch(ex: Exception){
    Json.decodeFromString(Result.Failure.Message.serializer(), this.bodyAsText())
} map { value -> Contextual(
    context = headers[Header.CONTEXT]?: EmptyContext.value ,
    data =  value
) }
