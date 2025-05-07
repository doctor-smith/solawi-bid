package org.evoleq.ktorx.client

import io.ktor.client.*

sealed class Request(
    open val url: String,
    open val client: HttpClient
) {
    data class Get(
        override val url: String,
        override val client: HttpClient
    ): Request(
        url, client
    )

}