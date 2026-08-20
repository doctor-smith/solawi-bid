package org.evoleq.iql.dsl


import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.evoleq.iql.data.*
import kotlin.test.*

@Suppress("LargeClass")
class IqlQueryEdgeCaseTest {

    private val json =
        Json {
            classDiscriminator = "type"
        }

    // -------------------------------------------------------------------------
    // Empty queries
    // -------------------------------------------------------------------------

    @Test
    fun `query without where is valid`() {

        val query =
            query {
                page(20)
            }

        assertEquals(
            20,
            query.pageInfo?.pageSize
        )
    }

    @Test
    fun `query without pagination is valid`() {

        val query =
            query {
                where {
                    p("User.active") eq true
                }
            }

        assertTrue(
            query.filter != null
        )
    }

    @Test
    fun `empty OR is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            query {
                where {
                    or {}
                }
            }
        }
    }

    @Test
    fun `empty AND is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            query {
                where {
                    and {}
                }
            }
        }
    }

    @Test
    fun `empty ANY is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            query {
                where {
                    any("orders") {}
                }
            }
        }
    }
    @Test
    fun `empty NONE is rejected`() {

        assertFailsWith<IllegalArgumentException> {
            query {
                where {
                    none("orders") {}
                }
            }
        }
    }

    @Test
    fun `empty ALL is rejected`() {

        assertFailsWith<IllegalArgumentException> {
            query {
                where {
                    all("orders") {}
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Single expressions
    // -------------------------------------------------------------------------

    @Test
    fun `single comparison remains comparison filter`() {

        val query =
            query {
                where {
                    p("User.name") eq "Alice"
                }
            }

        assertTrue(
            query.filter is ComparisonFilter
        )
    }

    @Test
    fun `single expression AND remains AndFilter`() {

        val query =
            query {
                where {
                    and {
                        p("User.active") eq true
                    }
                }
            }

        val filter =
            query.filter as AndFilter

        assertEquals(
            1,
            filter.filters.size
        )

        assertTrue(
            filter.filters.first() is ComparisonFilter
        )
    }

    @Test
    fun `single expression OR remains OrFilter`() {

        val query =
            query {
                where {
                    or {
                        p("User.active") eq true
                    }
                }
            }

        val filter =
            query.filter as OrFilter

        assertEquals(
            1,
            filter.filters.size
        )

        assertTrue(
            filter.filters.first() is ComparisonFilter
        )
    }

    @Test
    fun `multiple expressions become AND filter`() {

        val query =
            query {
                where {
                    p("User.active") eq true
                    p("User.name") eq "Alice"
                }
            }

        val filter =
            query.filter as AndFilter

        assertEquals(
            2,
            filter.filters.size
        )
    }

    // -------------------------------------------------------------------------
    // AND / OR / NOT
    // -------------------------------------------------------------------------

    @Test
    fun `AND combines multiple expressions`() {

        val query =
            query {
                where {
                    and {
                        p("User.active") eq true
                        p("User.deleted") eq false
                    }
                }
            }

        val filter =
            query.filter as AndFilter

        assertEquals(2, filter.filters.size)
    }

    @Test
    fun `OR combines multiple expressions`() {

        val query =
            query {
                where {
                    or {
                        p("User.name") eq "Alice"
                        p("User.name") eq "Bob"
                    }
                }
            }

        val filter =
            query.filter as OrFilter

        assertEquals(2, filter.filters.size)
    }

    @Test
    fun `NOT wraps filter`() {

        val query =
            query {
                where {
                    not {
                        p("User.active") eq true
                    }
                }
            }

        assertTrue(
            query.filter is NotFilter
        )
    }

    @Test
    fun `nested AND OR NOT structure is preserved`() {

        val query =
            query {
                where {
                    and {
                        p("User.active") eq true

                        or {
                            p("User.name") eq "Alice"
                            p("User.name") eq "Bob"
                        }

                        not {
                            p("User.deleted") eq true
                        }
                    }
                }
            }

        val root =
            query.filter as AndFilter

        assertEquals(3, root.filters.size)

        assertTrue(root.filters[0] is ComparisonFilter)
        assertTrue(root.filters[1] is OrFilter)
        assertTrue(root.filters[2] is NotFilter)
    }

    @Test
    fun `complex boolean tree preserves structure`() {

        val query =
            query {
                where {
                    or {

                        and {
                            p("User.active") eq true
                            p("User.age") gte 18
                        }

                        and {
                            p("User.role") eq "admin"

                            not {
                                p("User.deleted") eq true
                            }
                        }
                    }
                }
            }

        val root =
            query.filter as OrFilter

        assertEquals(
            2,
            root.filters.size
        )

        val first =
            root.filters[0] as AndFilter

        assertEquals(
            2,
            first.filters.size
        )

        val second =
            root.filters[1] as AndFilter

        assertEquals(
            2,
            second.filters.size
        )

        assertTrue(
            second.filters[1] is NotFilter
        )
    }

    // -------------------------------------------------------------------------
    // IN
    // -------------------------------------------------------------------------

    @Test
    fun `IN supports multiple values`() {

        val query =
            query {
                where {
                    p("User.id") `in` listOf(1, 2, 3)
                }
            }

        val filter =
            query.filter as InFilter

        assertEquals(
            3,
            filter.values.size
        )
    }

    @Test
    fun `IN with empty list is represented`() {

        val query =
            query {
                where {
                    p("User.id") `in` emptyList()
                }
            }

        val filter =
            query.filter as InFilter

        assertTrue(
            filter.values.isEmpty()
        )
    }

    // -------------------------------------------------------------------------
    // NULL
    // -------------------------------------------------------------------------

    @Test
    fun `isNull creates IsNullFilter`() {

        val query =
            query {
                where {
                    p("User.deletedAt").isNull()
                }
            }

        assertTrue(
            query.filter is IsNullFilter
        )
    }

    // -------------------------------------------------------------------------
    // Quantifiers
    // -------------------------------------------------------------------------

    @Test
    fun `ANY creates quantifier filter`() {

        val query =
            query {
                where {
                    any("orders") {
                        p("Order.total") gt 100
                    }
                }
            }

        val filter =
            query.filter as QuantifierFilter

        assertEquals(
            Quantifier.ANY,
            filter.quantifier
        )

        assertEquals(
            "orders",
            filter.relation.relation
        )
    }

    @Test
    fun `NONE creates quantifier filter`() {

        val query =
            query {
                where {
                    none("orders") {
                        p("Order.cancelled") eq true
                    }
                }
            }

        val filter =
            query.filter as QuantifierFilter

        assertEquals(
            Quantifier.NONE,
            filter.quantifier
        )
    }

    @Test
    fun `ALL creates quantifier filter`() {

        val query =
            query {
                where {
                    all("orders") {
                        p("Order.paid") eq true
                    }
                }
            }

        val filter =
            query.filter as QuantifierFilter

        assertEquals(
            Quantifier.ALL,
            filter.quantifier
        )
    }

    @Test
    fun `deep quantifiers preserve nesting`() {

        val query =
            query {
                where {
                    any("orders") {
                        any("product") {
                            p("Product.name") eq "Coffee"
                        }
                    }
                }
            }

        val outer =
            query.filter as QuantifierFilter

        val inner =
            outer.filter as QuantifierFilter

        assertEquals(
            Quantifier.ANY,
            outer.quantifier
        )

        assertEquals(
            "orders",
            outer.relation.relation
        )

        assertEquals(
            Quantifier.ANY,
            inner.quantifier
        )

        assertEquals(
            "product",
            inner.relation.relation
        )
    }

    @Test
    fun `quantifiers can be combined with boolean expressions`() {

        val query =
            query {
                where {
                    and {

                        p("User.active") eq true

                        or {

                            any("orders") {
                                p("Order.total") gt 100
                            }

                            none("orders") {
                                p("Order.cancelled") eq true
                            }
                        }
                    }
                }
            }

        val root =
            query.filter as AndFilter

        assertEquals(
            2,
            root.filters.size
        )

        val nested =
            root.filters[1] as OrFilter

        assertEquals(
            2,
            nested.filters.size
        )

        assertTrue(
            nested.filters[0] is QuantifierFilter
        )

        assertTrue(
            nested.filters[1] is QuantifierFilter
        )

        assertEquals(
            Quantifier.ANY,
            (nested.filters[0] as QuantifierFilter).quantifier
        )

        assertEquals(
            Quantifier.NONE,
            (nested.filters[1] as QuantifierFilter).quantifier
        )
    }

    @Test
    fun `deeply nested quantifiers preserve relation hierarchy`() {

        val query =
            query {
                where {
                    any("orders") {
                        any("product") {
                            any("category") {
                                p("Category.name") eq "Food"
                            }
                        }
                    }
                }
            }

        val level1 =
            query.filter as QuantifierFilter

        assertEquals(
            Quantifier.ANY,
            level1.quantifier
        )

        assertEquals(
            "orders",
            level1.relation.relation
        )

        val level2 =
            level1.filter as QuantifierFilter

        assertEquals(
            Quantifier.ANY,
            level2.quantifier
        )

        assertEquals(
            "product",
            level2.relation.relation
        )

        val level3 =
            level2.filter as QuantifierFilter

        assertEquals(
            Quantifier.ANY,
            level3.quantifier
        )

        assertEquals(
            "category",
            level3.relation.relation
        )

        val comparison =
            level3.filter as ComparisonFilter

        assertEquals(
            "Category.name",
            comparison.field.path
        )
    }

    // -------------------------------------------------------------------------
    // Ordering
    // -------------------------------------------------------------------------

    @Test
    fun `ascending order is serialized`() {

        val query =
            query {
                asc("User.name")
            }

        assertEquals(
            1,
            query.sort.size
        )

        assertEquals(
            "User.name",
            query.sort.first().field
        )
    }

    @Test
    fun `descending order is serialized`() {

        val query =
            query {
                desc("User.createdAt")
            }

        assertEquals(
            1,
            query.sort.size
        )

        assertEquals(
            "User.createdAt",
            query.sort.first().field
        )
    }

    @Test
    fun `ascending sort is preserved`() {

        val query =
            query {
                asc("User.name")
            }

        assertEquals(
            1,
            query.sort.size
        )

        assertEquals(
            "User.name",
            query.sort.first().field
        )

        assertEquals(
            SortDirection.ASC,
            query.sort.first().direction
        )
    }


    @Test
    fun `descending sort is preserved`() {

        val query =
            query {
                desc("User.createdAt")
            }

        assertEquals(
            1,
            query.sort.size
        )

        assertEquals(
            "User.createdAt",
            query.sort.first().field
        )

        assertEquals(
            SortDirection.DESC,
            query.sort.first().direction
        )
    }

    @Test
    fun `multiple sort expressions preserve order`() {

        val query =
            query {
                asc("User.name")
                desc("User.createdAt")
            }

        assertEquals(
            2,
            query.sort.size
        )

        assertEquals(
            "User.name",
            query.sort[0].field
        )

        assertEquals(
            SortDirection.ASC,
            query.sort[0].direction
        )

        assertEquals(
            "User.createdAt",
            query.sort[1].field
        )

        assertEquals(
            SortDirection.DESC,
            query.sort[1].direction
        )
    }

    @Test
    fun `same field can occur in multiple sort expressions`() {

        val query =
            query {
                asc("User.name")
                desc("User.name")
            }

        assertEquals(
            2,
            query.sort.size
        )
    }

    // -------------------------------------------------------------------------
    // Pagination
    // -------------------------------------------------------------------------

    @Test
    fun `pagination defaults offset to zero`() {

        val query =
            query {
                page(20)
            }

        assertEquals(
            20,
            query.pageInfo?.pageSize
        )

        assertEquals(
            0,
            query.pageInfo?.offset
        )
    }

    @Test
    fun `pagination preserves explicit offset`() {

        val query =
            query {
                page(
                    size = 20,
                    offset = 40
                )
            }

        assertEquals(
            20,
            query.pageInfo.pageSize
        )

        assertEquals(
            40,
            query.pageInfo.offset
        )
    }
    @Test
    fun `pagination preserves page size and offset`() {

        val query =
            query {
                page(
                    size = 20,
                    offset = 40
                )
            }

        assertEquals(
            20,
            query.pageInfo.pageSize
        )

        assertEquals(
            40,
            query.pageInfo.offset
        )
    }

    @Test
    fun `negative offset is rejected`() {

        assertFailsWith<IllegalArgumentException> {
            query {
                page(
                    size = 20,
                    offset = -1
                )
            }
        }
    }

    /*
    @Test
    fun `zero page size is rejected`() {

        assertFailsWith<IllegalArgumentException> {
            query {
                page(
                    page = 0
                )
            }
        }
    }

     */

    // -------------------------------------------------------------------------
    // Serialization
    // -------------------------------------------------------------------------

    @Test
    fun `comparison filter survives serialization`() {

        val query =
            query {
                where {
                    p("User.active") eq true
                }
            }

        val encoded =
            json.encodeToString(
                Query.serializer(),
                query
            )

        val decoded =
            json.decodeFromString(
                Query.serializer(),
                encoded
            )

        assertEquals(
            query,
            decoded
        )
    }

    @Test
    fun `nested filter survives serialization`() {

        val query =
            query {
                where {
                    any("orders") {
                        any("product") {
                            p("Product.name") eq "Coffee"
                        }
                    }
                }
            }

        val encoded =
            json.encodeToString(
                Query.serializer(),
                query
            )

        val decoded =
            json.decodeFromString(
                Query.serializer(),
                encoded
            )

        assertEquals(
            query,
            decoded
        )
    }

    @Test
    fun `sort and pagination survive serialization`() {

        val query =
            query {
                where {
                    p("User.active") eq true
                }

                asc("User.name")
                desc("User.createdAt")

                page(
                    size = 25,
                    offset = 50
                )
            }

        val encoded =
            json.encodeToString(
                Query.serializer(),
                query
            )

        val decoded =
            json.decodeFromString(
                Query.serializer(),
                encoded
            )

        assertEquals(
            query,
            decoded
        )
    }

    @Test
    fun `serialized filter contains discriminator`() {

        val query =
            query {
                where {
                    p("User.active") eq true
                }
            }

        val encoded =
            json.encodeToString(
                Query.serializer(),
                query
            )

        assertTrue(
            encoded.contains("\"type\"")
        )

        assertTrue(
            encoded.contains("comparison")
        )
    }

    private fun assertQueryRoundTrip(
        query: Query
    ) {

        val serialized =
            json.encodeToString(
                Query.serializer(),
                query
            )

        val deserialized =
            json.decodeFromString(
                Query.serializer(),
                serialized
            )

        assertEquals(
            query,
            deserialized
        )
    }

    @Test
    fun `comparison query survives serialization round trip`() {

        assertQueryRoundTrip(
            query {
                where {
                    p("User.active") eq true
                }
            }
        )
    }

    @Test
    fun `IN query survives serialization round trip`() {

        assertQueryRoundTrip(
            query {
                where {
                    p("User.role") `in` listOf(
                        "admin",
                        "user"
                    )
                }
            }
        )
    }

    @Test
    fun `IS NULL query survives serialization round trip`() {

        assertQueryRoundTrip(
            query {
                where {
                    p("User.deletedAt").isNull()
                }
            }
        )
    }

    @Test
    fun `complex boolean query survives serialization round trip`() {

        assertQueryRoundTrip(
            query {
                where {
                    or {
                        and {
                            p("User.active") eq true
                            p("User.age") gte 18
                        }

                        not {
                            p("User.deleted") eq true
                        }
                    }
                }
            }
        )
    }

    @Test
    fun `quantifier query survives serialization round trip`() {

        assertQueryRoundTrip(
            query {
                where {
                    any("orders") {
                        any("product") {
                            p("Product.name") eq "Coffee"
                        }
                    }
                }
            }
        )
    }

    // -------------------------------------------------------------------------
    // JSON wire format
    // -------------------------------------------------------------------------
    @Test
    fun `comparison filter uses type discriminator`() {

        val query =
            query {
                where {
                    p("User.active") eq true
                }
            }

        val json =
            json.encodeToString(
                Query.serializer(),
                query
            )

        assertTrue(
            json.contains("\"type\":\"comparison\"")
        )
    }

    @Test
    fun `OR filter uses type discriminator`() {

        val query =
            query {
                where {
                    or {
                        p("User.name") eq "Alice"
                        p("User.name") eq "Bob"
                    }
                }
            }

        val json =
            json.encodeToString(
                Query.serializer(),
                query
            )

        assertTrue(
            json.contains("\"type\":\"or\"")
        )

        assertTrue(
            json.contains("\"type\":\"comparison\"")
        )
    }

    // -------------------------------------------------------------------------
    // Field paths
    // -------------------------------------------------------------------------

    @Test
    fun `field paths are preserved exactly`() {

        val query =
            query {
                where {
                    p("User.createdAt") gt "2026-01-01"
                }
            }

        val filter =
            query.filter as ComparisonFilter

        assertEquals(
            "User.createdAt",
            filter.field.path
        )
    }

    @Test
    fun `deep direct field path is preserved`() {

        val query =
            query {
                where {
                    p("Order.product.name") eq "Coffee"
                }
            }

        val filter =
            query.filter as ComparisonFilter

        assertEquals(
            "Order.product.name",
            filter.field.path
        )
    }




    // -------------------------------------------------------------------------
    // Operator coverage
    // -------------------------------------------------------------------------

    @Test
    fun `all comparison operators are representable`() {

        val operators =
            listOf(
                Operator.EQ,
                Operator.NE,
                Operator.GT,
                Operator.GTE,
                Operator.LT,
                Operator.LTE
            )

        operators.forEach { operator ->

            val filter =
                ComparisonFilter(
                    field =
                        SimpleFieldRef(
                            "User.age"
                        ),
                    operator = operator,
                    value =
                        JsonPrimitive(18)
                )

            assertEquals(
                operator,
                filter.operator
            )
        }
    }

    // -------------------------------------------------------------------------
    // FE contract
    // -------------------------------------------------------------------------

    /**
     * Frontend/common
     *     User.createdAt
     *           │
     *           │ JSON
     *           ▼
     * Backend
     *     User.createdAt
     *           │
     *           │ Registry
     *           ▼
     *     created_at
     */
    @Test
    fun `camelCase field is preserved in serialized query`() {

        val query =
            query {
                where {
                    p("User.createdAt") eq "2026-08-20"
                }

                asc("User.createdAt")
            }

        val serialized =
            json.encodeToString(
                Query.serializer(),
                query
            )

        assertTrue(
            serialized.contains("User.createdAt")
        )

        assertFalse(
            serialized.contains("User.created_at")
        )
    }

    // -------------------------------------------------------------------------
    // Full executable documentation
    // -------------------------------------------------------------------------

    @Test
    fun `complex query is fully serializable`() {

        val query =
            query {

                where {
                    and {

                        p("User.active") eq true

                        or {

                            any("orders") {
                                any("product") {
                                    p("Product.name") eq "Coffee"
                                }
                            }

                            none("orders") {
                                p("Order.cancelled") eq true
                            }
                        }
                    }
                }

                asc("User.name")
                desc("User.createdAt")

                page(
                    size = 20,
                    offset = 40
                )
            }

        assertQueryRoundTrip(query)
    }
}
