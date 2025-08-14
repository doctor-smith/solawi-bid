package org.evoleq.ktorx.data

import io.ktor.http.*
import org.evoleq.ktorx.result.Result

interface KTorEnv {
    // val call: ApplicationCall
    val transformException: Result.Failure.Exception.() -> Pair<HttpStatusCode, Result.Failure.Message>
}
