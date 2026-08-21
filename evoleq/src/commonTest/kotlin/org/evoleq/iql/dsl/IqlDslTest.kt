package org.evoleq.iql.dsl


import kotlinx.serialization.json.JsonPrimitive
import org.evoleq.iql.data.*
import kotlin.test.Test
import kotlin.test.assertEquals

class IqlDslTest {

    @Test
    fun `simple equality`() {

        val filter = where {
            p("name") eq "Alice"
        }

        assertEquals(
            ComparisonFilter(
                field = SimpleFieldRef("name"),
                operator = Operator.EQ,
                value = JsonPrimitive("Alice")
            ),
            filter
        )
    }

    @Test
    fun `simple numeric comparison`() {

        val filter = where {
            p("age") gte 18
        }

        assertEquals(
            ComparisonFilter(
                field = SimpleFieldRef("age"),
                operator = Operator.GTE,
                value = JsonPrimitive(18)
            ),
            filter
        )
    }

    @Test
    fun `multiple conditions become AND`() {

        val filter = where {

            p("age") gte 18
            p("active") eq true
            p("name") neq "Bob"
        }

        assertEquals(
            AndFilter(
                listOf(
                    ComparisonFilter(
                        SimpleFieldRef("age"),
                        Operator.GTE,
                        JsonPrimitive(18)
                    ),
                    ComparisonFilter(
                        SimpleFieldRef("active"),
                        Operator.EQ,
                        JsonPrimitive(true)
                    ),
                    ComparisonFilter(
                        SimpleFieldRef("name"),
                        Operator.NE,
                        JsonPrimitive("Bob")
                    )
                )
            ),
            filter
        )
    }

    @Test
    fun `IN expression`() {

        val filter = where {

            p("status") `in` listOf(
                "ACTIVE",
                "PENDING"
            )
        }

        assertEquals(
            InFilter(
                field = SimpleFieldRef("status"),
                values = listOf(
                    JsonPrimitive("ACTIVE"),
                    JsonPrimitive("PENDING")
                )
            ),
            filter
        )
    }

    @Test
    fun `is null`() {

        val filter = where {
            p("deletedAt").isNull()
        }

        assertEquals(
            IsNullFilter(
                field = SimpleFieldRef("deletedAt")
            ),
            filter
        )
    }

    @Test
    fun `is not null`() {

        val filter = where {
            p("deletedAt").isNotNull()
        }

        assertEquals(
            NotFilter(
                IsNullFilter(
                    SimpleFieldRef("deletedAt")
                )
            ),
            filter
        )
    }

    @Test
    fun `nested path`() {

        val filter = where {
            p("customer.age") gte 18
        }

        assertEquals(
            ComparisonFilter(
                field = SimpleFieldRef("customer.age"),
                operator = Operator.GTE,
                value = JsonPrimitive(18)
            ),
            filter
        )
    }

    @Test
    fun `ANY relation`() {

        val filter = where {

            any("orders") {
                p("amount") gt 100
            }
        }

        assertEquals(
            QuantifierFilter(
                quantifier = Quantifier.ANY,
                relation = RelationRef(
                    entity = "",
                    relation = "orders"
                ),
                filter = ComparisonFilter(
                    field = SimpleFieldRef("amount"),
                    operator = Operator.GT,
                    value = JsonPrimitive(100)
                )
            ),
            filter
        )
    }

    @Test
    fun `NONE relation`() {

        val filter = where {

            none("orders") {
                p("status") eq "CANCELLED"
            }
        }

        assertEquals(
            QuantifierFilter(
                quantifier = Quantifier.NONE,
                relation = RelationRef(
                    entity = "",
                    relation = "orders"
                ),
                filter = ComparisonFilter(
                    field = SimpleFieldRef("status"),
                    operator = Operator.EQ,
                    value = JsonPrimitive("CANCELLED")
                )
            ),
            filter
        )
    }

    @Test
    fun `ALL relation`() {

        val filter = where {

            all("orders") {
                p("amount") gt 100
            }
        }

        assertEquals(
            QuantifierFilter(
                quantifier = Quantifier.ALL,
                relation = RelationRef(
                    entity = "",
                    relation = "orders"
                ),
                filter = ComparisonFilter(
                    field = SimpleFieldRef("amount"),
                    operator = Operator.GT,
                    value = JsonPrimitive(100)
                )
            ),
            filter
        )
    }

    @Test
    fun `nested quantifiers`() {

        val filter = where {

            any("orders") {

                p("amount") gt 100

                any("items") {
                    p("price") gt 50
                }
            }
        }

        assertEquals(
            QuantifierFilter(
                quantifier = Quantifier.ANY,
                relation = RelationRef(
                    entity = "",
                    relation = "orders"
                ),
                filter = AndFilter(
                    listOf(
                        ComparisonFilter(
                            SimpleFieldRef("amount"),
                            Operator.GT,
                            JsonPrimitive(100)
                        ),
                        QuantifierFilter(
                            quantifier = Quantifier.ANY,
                            relation = RelationRef(
                                entity = "",
                                relation = "items"
                            ),
                            filter = ComparisonFilter(
                                SimpleFieldRef("price"),
                                Operator.GT,
                                JsonPrimitive(50)
                            )
                        )
                    )
                )
            ),
            filter
        )
    }

    @Test
    fun `complex filter`() {

        val filter = where {

            p("age") gte 18
            p("active") eq true

            any("orders") {
                p("amount") gt 100
            }

            none("orders") {
                p("status") eq "CANCELLED"
            }
        }

        assertEquals(
            AndFilter(
                listOf(

                    ComparisonFilter(
                        SimpleFieldRef("age"),
                        Operator.GTE,
                        JsonPrimitive(18)
                    ),

                    ComparisonFilter(
                        SimpleFieldRef("active"),
                        Operator.EQ,
                        JsonPrimitive(true)
                    ),

                    QuantifierFilter(
                        Quantifier.ANY,
                        RelationRef("", "orders"),
                        ComparisonFilter(
                            SimpleFieldRef("amount"),
                            Operator.GT,
                            JsonPrimitive(100)
                        )
                    ),

                    QuantifierFilter(
                        Quantifier.NONE,
                        RelationRef("", "orders"),
                        ComparisonFilter(
                            SimpleFieldRef("status"),
                            Operator.EQ,
                            JsonPrimitive("CANCELLED")
                        )
                    )
                )
            ),
            filter
        )
    }

}
