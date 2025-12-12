package org.solyton.solawi.bid.application.api

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.evoleq.math.MathDsl
import org.solyton.solawi.bid.application.data.Application
import org.evoleq.ktorx.headers.Header

@MathDsl
fun Application.client(loggedIn: Boolean = true) = HttpClient(Js) {
    defaultRequest {
        header(HttpHeaders.ContentType,ContentType.Application.Json)
        header(Header.CONTEXT, context.current)
        if(loggedIn) {
            header(HttpHeaders.Authorization, "Bearer ${userData.accessToken}")
        }
    }
}

