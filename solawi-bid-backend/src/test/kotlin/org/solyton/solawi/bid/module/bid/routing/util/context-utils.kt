package org.solyton.solawi.bid.module.bid.routing.util

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

suspend fun HttpClient.getAuctionsApplicationContextId(): String =  get("/test/context-by-name?context-name=AUCTIONS").bodyAsText()
suspend fun HttpClient.getTestAuctionContextId(): String =  get("/test/context-by-name?context-name=TEST_AUCTION_CONTEXT").bodyAsText()
