package org.solyton.solawi.bid.application.data.env

import kotlinx.coroutines.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.browser.window

@Serializable
@Suppress("ConstructorParameterNaming")
data class Config(
    val ENVIRONMENT: String,
    val FRONTEND_URL: String,
    val FRONTEND_PORT: String,
    val BACKEND_URL: String,
    val BACKEND_PORT: String
)

suspend fun getEnv(): Environment {
    return try {
        val response = window.fetch("/config.json").await()
        val text = response.text().await()
        val config = Json.decodeFromString<Config>(text)
        Environment(
            set = true,
            type = config.ENVIRONMENT,
            frontendUrl = config.FRONTEND_URL,
            frontendPort = config.FRONTEND_PORT.toInt(),
            backendUrl = config.BACKEND_URL,
            backendPort = config.BACKEND_PORT.toInt()
        )
    } catch (e: dynamic) {
        console.error(e)
        Environment(
            set = true,
            type = "prod",
            backendUrl = "https://bid.solyton.org",
            backendPort = 8080,
            frontendUrl = "https://solyton.org",
            frontendPort = 80
        )
    }
}

