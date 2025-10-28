package org.solyton.solawi.bid.module.testFramework

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import java.util.UUID



fun Routing.testContexts(database: Database) {
    get("/test/dummy-root-context") {
        val contextId =  transaction(database) {   ContextEntity.find { ContextsTable.name eq "DUMMY_ROOT_CONTEXT" }.first().id.value }
        call.respondText { contextId.toString() }
    }
    get("/test/auctions-application-context") {
        val contextId =  transaction(database) {   ContextEntity.find { ContextsTable.name eq "AUCTIONS" }.first().id.value }
        call.respondText { contextId.toString() }
    }
    get("/test/test-auction-context") {
        val contextId =  transaction(database) {   ContextEntity.find { ContextsTable.name eq "TEST_AUCTION_CONTEXT" }.first().id.value }
        call.respondText { contextId.toString() }
    }
    get("/test/context-by-name") {
        val contextName = call.request.queryParameters["context-name"] as String
        val contextId =  transaction(database) {
            val context = ContextEntity.find { ContextsTable.name eq contextName }.first()
            context.id.value.toString()
        }
        call.respondText(contextId)
    }
    get("/test/context-by-id") {
        val contextName = call.request.queryParameters["context-id"] as String
        val context =  transaction(database) {
            val context = ContextEntity.find { ContextsTable.id eq UUID.fromString(contextName) }.first()
            context
        }
        call.respondText(context.name)
    }
}

suspend fun HttpClient.getDummyRootContextId(): String =  get("/test/context-by-name?context-name=DUMMY_ROOT_CONTEXT").bodyAsText()

suspend fun HttpClient.getTestContextIdByName(name: String): String = get("/test/context-by-name?context-name=$name").bodyAsText()

suspend fun HttpClient.contextExists(id: UUID): Boolean =
    try {
        get("/test/context-by-id?context-id=$id")
        true
    } catch(_: Exception) {
        false
    }
