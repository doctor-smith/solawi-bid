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
}

suspend fun HttpClient.getDummyRootContextId(): String =  get("/test/dummy-root-context").bodyAsText()
suspend fun HttpClient.getAuctionsApplicationContextId(): String =  get("/test/auctions-application-context").bodyAsText()
suspend fun HttpClient.getTestAuctionContextId(): String =  get("/test/test-auction-context").bodyAsText()

