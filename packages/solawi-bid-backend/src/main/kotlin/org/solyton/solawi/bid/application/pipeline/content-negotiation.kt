package org.solyton.solawi.bid.application.pipeline

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun Application.installContentNegotiation() =try {

        install(ContentNegotiation) {
            json()
        }

    } catch (_:Exception) {}
