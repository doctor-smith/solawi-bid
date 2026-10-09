package org.solyton.solawi.bid.application.data.env

import io.ktor.util.*
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.evoleq.math.Reader

@Serializable
@Suppress("ConstructorParameterNaming")
data class Config(
    val ENVIRONMENT: String,
    val FRONTEND_URL: String,
    val FRONTEND_PORT: String,
    val BACKEND_URL: String,
    val BACKEND_PORT: String
)

/**
 * Retrieves the application environment configuration.
 *
 * The method attempts to fetch configuration data from a predefined endpoint.
 * If the fetch operation fails, a default environment configuration is returned.
 *
 * @return an instance of [Environment] containing the environment settings such as
 * the type of environment, frontend and backend URLs, and their respective ports.
 */
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

/**
 * Determines if the current environment is a non-production environment.
 *
 * The method evaluates the `type` property of the Environment instance,
 * comparing it (case-insensitively) to common identifiers for production environments
 * such as "p", "prod", and "production". If the type does not match any of these,
 * the method returns `true`, indicating that the current environment is non-production.
 * Otherwise, it returns `false`.
 *
 * @receiver The Environment instance whose environment type is being checked.
 * @return `true` if the environment type is not "p", "prod", or "production"; otherwise, `false`.
 */
fun Environment.isNonProdEnv() = type.toLowerCasePreservingASCIIRules() !in listOf( "p", "prod", "production" )

/**
 * Reader instance that determines if the current environment is a non-production environment.
 *
 * This value leverages the `Environment.isNonProdEnv()` function to evaluate the environment type
 * by mapping it through a functional context. The evaluation checks whether the `type` property
 * in the `Environment` instance indicates a non-production environment (e.g., not "p", "prod", or "production").
 *
 * @receiver A `Reader` functional construct that takes an `Environment` instance and returns `true`
 * if it is a non-production environment, or `false` otherwise.
 */
val IsNonProdEnv: Reader<Environment, Boolean> = Reader { env -> env.isNonProdEnv() }
