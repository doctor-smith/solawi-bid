package org.solyton.solawi.bid.module.testFramework

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.solyton.solawi.bid.module.authentication.environment.JWT
import org.solyton.solawi.bid.module.authentication.service.generateAccessToken
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UsersTable

/**
 * Provides user token for registered users
 */
fun Routing.provideUserTokens(jwt: JWT, database: Database) {
    get("/test/get-token") {
        val username = call.request.queryParameters["user"] as String
        val token =  transaction(database) {
            val user = UserEntity.find { UsersTable.username eq username }.first()
            generateAccessToken(user.id.value.toString(), jwt)
        }
        call.respondText(token)
    }
}

suspend fun HttpClient.getTestToken(user: String): String {
    val response = get("/test/get-token?user=$user")
    return response.bodyAsText()
}
