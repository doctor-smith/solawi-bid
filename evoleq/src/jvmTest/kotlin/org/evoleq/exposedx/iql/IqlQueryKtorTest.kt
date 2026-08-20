package org.evoleq.exposedx.iql

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import junit.framework.TestCase.assertNotNull
import org.evoleq.iql.data.IqlJson
import org.evoleq.iql.data.Query
import org.evoleq.iql.data.configure
import org.evoleq.iql.dsl.query
import kotlin.test.Test
import kotlin.test.assertEquals


class IqlQueryKtorTest {

    private val iqlJson = IqlJson.configure {
        classDiscriminator = "type"
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    @Test
    fun `POST query executes IQL query`() =
        testApplication {

            application {
                testRouting()
            }

            val query =
                query {

                    where {
                        p("User.active") eq true
                    }

                    desc("User.createdAt")

                    page(20)
                }

            val response =
                client.post("/users/query") {

                    contentType(
                        ContentType.Application.Json
                    )

                    setBody(
                        iqlJson.encodeToString(
                            Query.serializer(),
                            query
                        )
                    )
                }

            assertEquals(
                HttpStatusCode.OK,
                response.status
            )
        }

    @Test
    fun `POST query receives serialized IQL query`() =
        testApplication {

            application {
                testRouting()
            }

            val query =
                query {

                    where {
                        p("User.active") eq true
                    }

                    desc("User.createdAt")

                    page(
                        size = 20,
                        offset = 40
                    )
                }

            val response =
                client.post("/users/query") {

                    contentType(
                        ContentType.Application.Json
                    )

                    setBody(
                        iqlJson.encodeToString(
                            Query.serializer(),
                            query
                        )
                    )
                }

            assertEquals(
                HttpStatusCode.OK,
                response.status
            )

            val body =
                response.bodyAsText()

            assertNotNull(body)

            println(body)
        }

    private fun Application.testRouting() {
        install(ContentNegotiation) {
            json(iqlJson)
        }

        routing {

            post("/users/query") {

                val query =
                    call.receive<Query>()

                // This is where the real application
                // would hand the query to the service.

                call.respond(
                    HttpStatusCode.OK,
                    query
                )
            }
        }
    }
}

