package org.solyton.solawi.bid.application.pipeline

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*


fun Application.installCors() = try {

        install(CORS) {
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
            allowMethod(HttpMethod.Patch)
            allowHeader(HttpHeaders.Authorization)
            allowHeader(HttpHeaders.AccessControlAllowHeaders)
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.AccessControlAllowOrigin)
            allowHeader(HttpHeaders.AccessControlAllowMethods)

            // Context Header
            allowHeader("CONTEXT")

            allowCredentials = true
            anyHost()
            // Restrict to specific hosts (use anyHost() only in development)
            // allowHost("example.com", schemes = listOf("https"))
            // allowHost("api.example.com")

            maxAgeInSeconds = 24 * 3600 // 1 day
        }
    } catch (_:Exception) {}
