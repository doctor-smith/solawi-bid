package org.evoleq.iql.dsl


import kotlinx.serialization.json.jsonPrimitive
import org.evoleq.iql.data.*
import kotlin.test.*

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

    @Test
    fun `empty where is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            query {
                where { }
            }
        }
    }

    @Test
    fun `contains is represented as LIKE`() {
        val query =
            query {
                where {
                    p("User.name") contains "lor"
                }
            }

        val filter =
            query.filter as ComparisonFilter

        assertEquals(
            Operator.LIKE,
            filter.operator
        )

        assertEquals(
            "%lor%",
            filter.value.jsonPrimitive.content
        )
    }

    @Test
    fun `startsWith is represented as LIKE`() {
        val query =
            query {
                where {
                    p("User.name") startsWith "Flo"
                }
            }

        val filter =
            query.filter as ComparisonFilter

        assertEquals(
            Operator.LIKE,
            filter.operator
        )

        assertEquals(
            "Flo%",
            filter.value.jsonPrimitive.content
        )
    }

    @Test
    fun `endsWith is represented as LIKE`() {
        val query =
            query {
                where {
                    p("User.name") endsWith "ian"
                }
            }

        val filter =
            query.filter as ComparisonFilter

        assertEquals(
            Operator.LIKE,
            filter.operator
        )

        assertEquals(
            "%ian",
            filter.value.jsonPrimitive.content
        )
    }

    @Test
    fun `contains can ignore case`() {
        val query =
            query {
                where {
                    p("User.name")
                        .contains("florian", ignoreCase = true)
                }
            }

        val filter =
            query.filter as ComparisonFilter

        assertEquals(
            Operator.LIKE,
            filter.operator
        )

        assertEquals(
            "%florian%",
            filter.value.jsonPrimitive.content
        )

        assertTrue(filter.ignoreCase)
    }

    @Test
    fun `contains is case sensitive by default`() {
        val query =
            query {
                where {
                    p("User.name") contains "florian"
                }
            }

        val filter =
            query.filter as ComparisonFilter

        assertFalse(filter.ignoreCase)
    }




    @Test
    fun `startsWith can ignore case`() {
        val query =
            query {
                where {
                    p("User.name")
                        .startsWith("florian", ignoreCase = true)
                }
            }

        val filter =
            query.filter as ComparisonFilter

        assertEquals(
            Operator.LIKE,
            filter.operator
        )

        assertEquals(
            "florian%",
            filter.value.jsonPrimitive.content
        )

        assertTrue(filter.ignoreCase)
    }

    @Test
    fun `startsWith is case sensitive by default`() {
        val query =
            query {
                where {
                    p("User.name") startsWith  "florian"
                }
            }

        val filter =
            query.filter as ComparisonFilter

        assertFalse(filter.ignoreCase)
    }


    @Test
    fun `endsWith can ignore case`() {
        val query =
            query {
                where {
                    p("User.name")
                        .endsWith("florian", ignoreCase = true)
                }
            }

        val filter =
            query.filter as ComparisonFilter

        assertEquals(
            Operator.LIKE,
            filter.operator
        )

        assertEquals(
            "%florian",
            filter.value.jsonPrimitive.content
        )

        assertTrue(filter.ignoreCase)
    }

    @Test
    fun `endsWith is case sensitive by default`() {
        val query =
            query {
                where {
                    p("User.name") endsWith "florian"
                }
            }

        val filter =
            query.filter as ComparisonFilter

        assertFalse(filter.ignoreCase)
    }


    @Test
    fun `contains escapes underscore`() {
        val query =
            query {
                where {
                    p("User.name") contains "100_kg"
                }
            }

        val filter =
            query.filter as ComparisonFilter

        assertEquals(
            "%100\\_kg%",
            filter.value.jsonPrimitive.content
        )
    }

    @Test
    fun `contains escapes percent`() {
        val query =
            query {
                where {
                    p("User.name") contains "100%"
                }
            }

        val filter =
            query.filter as ComparisonFilter

        assertEquals(
            "%100\\%%",
            filter.value.jsonPrimitive.content
        )
    }

    @Test
    fun `startsWith escapes underscore`() {
        val query =
            query {
                where {
                    p("User.name") startsWith "100_kg"
                }
            }

        val filter =
            query.filter as ComparisonFilter

        assertEquals(
            Operator.LIKE,
            filter.operator
        )

        assertEquals(
            "100\\_kg%",
            filter.value.jsonPrimitive.content
        )
    }
    @Test
    fun `startsWith escapes percent`() {
        val query =
            query {
                where {
                    p("User.name") startsWith "100%"
                }
            }

        val filter =
            query.filter as ComparisonFilter

        assertEquals(
            "100\\%%",
            filter.value.jsonPrimitive.content
        )
    }
    @Test
    fun `endsWith escapes underscore`() {
        val query =
            query {
                where {
                    p("User.name") endsWith "100_kg"
                }
            }

        val filter =
            query.filter as ComparisonFilter

        assertEquals(
            "%100\\_kg",
            filter.value.jsonPrimitive.content
        )
    }
    @Test
    fun `endsWith escapes percent`() {
        val query =
            query {
                where {
                    p("User.name") endsWith "100%"
                }
            }

        val filter =
            query.filter as ComparisonFilter

        assertEquals(
            "%100\\%",
            filter.value.jsonPrimitive.content
        )
    }

    @Test
    fun `like preserves explicit escape sequences`() {
        val query =
            query {
                where {
                    p("User.probability") like "99\\%"
                }
            }

        val filter =
            query.filter as ComparisonFilter

        assertEquals(
            Operator.LIKE,
            filter.operator
        )

        assertEquals(
            "99\\%",
            filter.value.jsonPrimitive.content
        )
    }
}
