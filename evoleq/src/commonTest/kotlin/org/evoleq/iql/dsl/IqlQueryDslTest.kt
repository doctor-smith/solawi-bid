package org.evoleq.iql.dsl


import org.evoleq.iql.data.IqlJson
import org.evoleq.iql.data.Query
import org.evoleq.iql.data.configure
import kotlin.test.Test
import kotlin.test.assertEquals

class QuerySerializationTest {

    private val json = IqlJson.configure {
        encodeDefaults = true
    }

    @Test
    fun `query DSL is serializable`() {

        val query =
            query {

                where {
                    (p("User.active") eq true)
                }

                asc("User.name")
                desc("User.createdAt")

                page(
                    size = 50,
                    offset = 100
                )
            }

        val serialized =
            json.encodeToString(
                Query.serializer(),
                query
            )

        val restored =
            json.decodeFromString(
                Query.serializer(),
                serialized
            )

        assertEquals(
            query,
            restored
        )
    }
}
