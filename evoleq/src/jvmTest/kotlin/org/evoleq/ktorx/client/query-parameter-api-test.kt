package org.evoleq.ktorx.client

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import io.ktor.server.testing.testApplication
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

class QueryParameterApiTest {

    @Test fun queryParameterTest() = runBlocking {
        testApplication{
            routing {
                get("test") {
                    val params = call.request.queryParameters
                    call.respondText {
                        val queryParams: QueryParams = params.entries().map { (key, value) ->
                            value.map { key to it }
                        }.flatten()
                        Json.encodeToString<QueryParams>(queryParams)
                    }
                }


            }
            val queryParams = queryParams {
                "p"+="p"
                "q"+="q"
                "r"+=setOf(
                    "1","2","3"
                )
            }.toQueryString()
            val paramsText = client.get("test$queryParams").bodyAsText()
            val params = Json.decodeFromString<QueryParams>(paramsText)

            assertEquals(
                listOf(
                    "p" to "p",
                    "q" to "q",
                    "r" to "1",
                    "r" to "2",
                    "r" to "3",
                ),
                params
            )
        }
    }
}
