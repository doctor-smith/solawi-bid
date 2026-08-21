package org.evoleq.iql.dsl



import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.evoleq.iql.data.*

/**
 * Entry point for building an IQL filter tree.
 *
 * Example:
 *
 * where {
 *     p("age") >= 18
 *     p("active") == true
 *
 *     any("orders") {
 *         p("amount") > 100
 *     }
 * }
 */
fun where(block: FilterBuilder.() -> Unit): Filter {
    return FilterBuilder()
        .apply(block)
        .build()
}

fun where(entity: String,block: FilterBuilder.()-> Unit): Filter {
    return FilterBuilder(entity)
        .apply(block)
        .build()
}


@Suppress("TooManyFunctions")
class FilterBuilder(
    private val entity: String? = null
) {

    private val filters =
        mutableListOf<Filter>()

    fun p(path: String): FieldPath =
        FieldPath(
            path = path,
            builder = this
        )

    fun field(path: String): FieldPath =
        p(path)

    internal fun add(filter: Filter) {
        filters += filter
    }

    // -------------------------------------------------------------------------
    // Logical operators
    // -------------------------------------------------------------------------

    /**
     * Returns the expressions currently collected by this builder.
     *
     * Used by explicit logical combinators such as AND and OR.
     */
    internal fun buildFilters(): List<Filter> =
        filters.toList()

    fun and(
        block: FilterBuilder.() -> Unit
    ) {
        val nested =
            FilterBuilder()
                .apply(block)

        require(nested.filters.isNotEmpty()) {
            "AND requires at least one expression"
        }

        add(
            AndFilter(
                nested.buildFilters()
            )
        )
    }

    fun or(
        block: FilterBuilder.() -> Unit
    ) {
        val nested =
            FilterBuilder()
                .apply(block)

        require(nested.filters.isNotEmpty()) {
            "OR requires at least one expression"
        }

        add(
            OrFilter(
                nested.buildFilters()
            )
        )
    }

    fun not(
        block: FilterBuilder.() -> Unit
    ) {
        val nested =
            FilterBuilder()
                .apply(block)


        add(
            NotFilter(nested.build())
        )
    }

    // -------------------------------------------------------------------------
    // Quantifiers
    // -------------------------------------------------------------------------

    fun any(
        relation: String,
        block: FilterBuilder.() -> Unit
    ) {
        addQuantifier(
            quantifier = Quantifier.ANY,
            relation = relation,
            block = block
        )
    }

    fun none(
        relation: String,
        block: FilterBuilder.() -> Unit
    ) {
        addQuantifier(
            quantifier = Quantifier.NONE,
            relation = relation,
            block = block
        )
    }

    fun all(
        relation: String,
        block: FilterBuilder.() -> Unit
    ) {
        addQuantifier(
            quantifier = Quantifier.ALL,
            relation = relation,
            block = block
        )
    }

    private fun addQuantifier(
        quantifier: Quantifier,
        relation: String,
        block: FilterBuilder.() -> Unit
    ) {
        val nested =
            FilterBuilder()
                .apply(block)
                .build()

        add(
            QuantifierFilter(
                quantifier = quantifier,
                relation = RelationRef(
                    entity = entity?:"",
                    relation = relation
                ),
                filter = nested
            )
        )
    }

    // -------------------------------------------------------------------------
    // Build
    // -------------------------------------------------------------------------

    internal fun build(): Filter {

        require(filters.isNotEmpty()) {
            "IQL filter must contain at least one expression"
        }

        return when (filters.size) {
            1 -> filters.first()
            else -> AndFilter( filters.toList()  )
        }
    }

}

/**
 * A path to a field.
 *
 * Examples:
 *
 *     p("age")
 *     p("customer.age")
 *     p("customer.address.country")
 */
@Suppress("FunctionNaming", "TooManyFunctions")
class FieldPath(
    val path: String,
    private val builder: FilterBuilder
) {

    private fun add(filter: Filter): Filter {
        builder.add(filter)
        return filter
    }

    infix fun eq(value: Any?) =
        add(
            ComparisonFilter(
                SimpleFieldRef(path),
                Operator.EQ,
                json(value)
            )
        )

    infix fun neq(value: Any?) =
        add(
            ComparisonFilter(
                SimpleFieldRef(path),
                Operator.NE,
                json(value)
            )
        )

    infix fun gt(value: Any) =
        add(
            ComparisonFilter(
                SimpleFieldRef(path),
                Operator.GT,
                json(value)
            )
        )

    infix fun gte(value: Any) =
        add(
            ComparisonFilter(
                SimpleFieldRef(path),
                Operator.GTE,
                json(value)
            )
        )

    infix fun lt(value: Any) =
        add(
            ComparisonFilter(
                SimpleFieldRef(path),
                Operator.LT,
                json(value)
            )
        )

    infix fun lte(value: Any) =
        add(
            ComparisonFilter(
                SimpleFieldRef(path),
                Operator.LTE,
                json(value)
            )
        )

    infix fun `in`(values: List<Any?>) =
        add(
            InFilter(
                SimpleFieldRef(path),
                values.map(::json)
            )
        )

    fun isNull() =
        add(
            IsNullFilter(
                SimpleFieldRef(path)
            )
        )

    fun isNotNull() =
        add(
            NotFilter(
                IsNullFilter(
                    SimpleFieldRef(path)
                )
            )
        )



    infix fun contains(value: String) = add(
        comparison(
            operator = Operator.LIKE,
            value = JsonPrimitive("%${escapeLike(value)}%")
        )
    )

    infix fun startsWith(value: String) = add(
        comparison(
            operator = Operator.LIKE,
            value = JsonPrimitive("${escapeLike(value)}%")
        )
    )

    infix fun endsWith(value: String) = add(
        comparison(
            operator = Operator.LIKE,
            value = JsonPrimitive("%${escapeLike(value)}")
        )
    )

    fun contains(value: String, ignoreCase: Boolean) = add(
        comparison(
            operator = Operator.LIKE,
            value = JsonPrimitive("%${escapeLike(value)}%"),
            ignoreCase = ignoreCase
        )
    )
    fun startsWith(value: String, ignoreCase: Boolean) = add(
        comparison(
            operator = Operator.LIKE,
            value = JsonPrimitive("${escapeLike(value)}%"),
            ignoreCase = ignoreCase
        )
    )

    fun endsWith(value: String, ignoreCase: Boolean) = add(
        comparison(
            operator = Operator.LIKE,
            value = JsonPrimitive("%${escapeLike(value)}"),
            ignoreCase = ignoreCase
        )
    )

    infix fun containsIgnoreCase(value: String) = contains(value, ignoreCase = true)

    infix fun startsWithIgnoreCase(value: String) = contains(value, ignoreCase = true)

    infix fun endsWithIgnoreCase(value: String) = contains(value, ignoreCase = true)

    infix fun like(pattern: String) = add(
        comparison(
            operator = Operator.LIKE,
            value = JsonPrimitive(pattern),
            ignoreCase = false
        )
    )
    infix fun likeIgnoreCase(pattern: String) = add(
        comparison(
            operator = Operator.LIKE,
            value = JsonPrimitive(pattern),
            ignoreCase = true
        )
    )
    fun like(pattern: String, ignoreCase: Boolean) = (
        comparison(
            operator = Operator.LIKE,
            value = JsonPrimitive(pattern),
            ignoreCase = ignoreCase
        )
    )
}

const val LIKE_ESCAPE = '\\'

private fun escapeLike(value: String): String =
    value
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")

/**
 * Comparison operators.
 *
 * Kotlin's operator overloading cannot be used directly for
 * == because equals() must return Boolean. Therefore EQ is
 * available as:
 *
 *     p("age") eq 18
 *
 * Ordering can be written naturally:
 *
 *     p("age") gt 18
 *     p("age") gte 18
 */
infix fun FieldPath.eq(
    value: String
): Filter =
    _root_ide_package_.org.evoleq.iql.data.ComparisonFilter(
        _root_ide_package_.org.evoleq.iql.data.SimpleFieldRef(path),
        Operator.EQ,
        JsonPrimitive(value)
    )

infix fun FieldPath.eq(
    value: Int
): Filter =
    _root_ide_package_.org.evoleq.iql.data.ComparisonFilter(
        _root_ide_package_.org.evoleq.iql.data.SimpleFieldRef(path),
        Operator.EQ,
        JsonPrimitive(value)
    )

infix fun FieldPath.eq(
    value: Long
): Filter =
    _root_ide_package_.org.evoleq.iql.data.ComparisonFilter(
        _root_ide_package_.org.evoleq.iql.data.SimpleFieldRef(path),
        Operator.EQ,
        JsonPrimitive(value)
    )

infix fun FieldPath.eq(
    value: Double
): Filter =
    _root_ide_package_.org.evoleq.iql.data.ComparisonFilter(
        _root_ide_package_.org.evoleq.iql.data.SimpleFieldRef(path),
        Operator.EQ,
        JsonPrimitive(value)
    )

infix fun FieldPath.eq(
    value: Boolean
): Filter =
    _root_ide_package_.org.evoleq.iql.data.ComparisonFilter(
        _root_ide_package_.org.evoleq.iql.data.SimpleFieldRef(path),
        Operator.EQ,
        JsonPrimitive(value)
    )

infix fun FieldPath.neq(
    value: String
): Filter =
    _root_ide_package_.org.evoleq.iql.data.ComparisonFilter(
        _root_ide_package_.org.evoleq.iql.data.SimpleFieldRef(path),
        Operator.NE,
        JsonPrimitive(value)
    )

infix fun FieldPath.neq(
    value: Int
): Filter =
    _root_ide_package_.org.evoleq.iql.data.ComparisonFilter(
        _root_ide_package_.org.evoleq.iql.data.SimpleFieldRef(path),
        Operator.NE,
        JsonPrimitive(value)
    )

infix fun FieldPath.gt(
    value: Int
): Filter =
    comparison(
        Operator.GT,
        JsonPrimitive(value)
    )

infix fun FieldPath.gt(
    value: Long
): Filter =
    comparison(
        Operator.GT,
        JsonPrimitive(value)
    )

infix fun FieldPath.gt(
    value: Double
): Filter =
    comparison(
        Operator.GT,
        JsonPrimitive(value)
    )

infix fun FieldPath.gt(
    value: String
): Filter =
    comparison(
        Operator.GT,
        JsonPrimitive(value)
    )

infix fun FieldPath.gte(
    value: Int
): Filter =
    comparison(
        Operator.GTE,
        JsonPrimitive(value)
    )

infix fun FieldPath.gte(
    value: Long
): Filter =
    comparison(
        Operator.GTE,
        JsonPrimitive(value)
    )

infix fun FieldPath.gte(
    value: Double
): Filter =
    comparison(
        Operator.GTE,
        JsonPrimitive(value)
    )

infix fun FieldPath.gte(
    value: String
): Filter =
    comparison(
        Operator.GTE,
        JsonPrimitive(value)
    )

infix fun FieldPath.lt(
    value: Int
): Filter =
    comparison(
        Operator.LT,
        JsonPrimitive(value)
    )

infix fun FieldPath.lt(
    value: Long
): Filter =
    comparison(
        Operator.LT,
        JsonPrimitive(value)
    )

infix fun FieldPath.lt(
    value: Double
): Filter =
    comparison(
        Operator.LT,
        JsonPrimitive(value)
    )

infix fun FieldPath.lt(
    value: String
): Filter =
    comparison(
        Operator.LT,
        JsonPrimitive(value)
    )

infix fun FieldPath.lte(
    value: Int
): Filter =
    comparison(
        Operator.LTE,
        JsonPrimitive(value)
    )

infix fun FieldPath.lte(
    value: Long
): Filter =
    comparison(
        Operator.LTE,
        JsonPrimitive(value)
    )

infix fun FieldPath.lte(
    value: Double
): Filter =
    comparison(
        Operator.LTE,
        JsonPrimitive(value)
    )

infix fun FieldPath.lte(
    value: String
): Filter =
    comparison(
        Operator.LTE,
        JsonPrimitive(value)
    )

infix fun FieldPath.contains(value: String) =
    comparison(
        operator = Operator.LIKE,
        value = JsonPrimitive("%${escapeLike(value)}%")
    )

infix fun FieldPath.startsWith(value: String) =
    comparison(
        operator = Operator.LIKE,
        value = JsonPrimitive("${escapeLike(value)}%")
    )

infix fun FieldPath.endsWith(value: String) =
    comparison(
        operator = Operator.LIKE,
        value = JsonPrimitive("%${escapeLike(value)}")
    )

fun FieldPath.contains(value: String, ignoreCase: Boolean) =
    comparison(
        operator = Operator.LIKE,
        value = JsonPrimitive("%${escapeLike(value)}%"),
        ignoreCase = ignoreCase
    )

fun FieldPath.startsWith(value: String, ignoreCase: Boolean) =
    comparison(
        operator = Operator.LIKE,
        value = JsonPrimitive("${escapeLike(value)}%"),
        ignoreCase = ignoreCase
    )

fun FieldPath.endsWith(value: String, ignoreCase: Boolean) =
    comparison(
        operator = Operator.LIKE,
        value = JsonPrimitive("%${escapeLike(value)}"),
        ignoreCase = ignoreCase
    )

infix fun FieldPath.containsIgnoreCase(value: String) = contains(value, ignoreCase = true)

infix fun FieldPath.startsWithIgnoreCase(value: String) = contains(value, ignoreCase = true)

infix fun FieldPath.endsWithIgnoreCase(value: String) = contains(value, ignoreCase = true)

infix fun FieldPath.like(pattern: String) =
    comparison(
        operator = Operator.LIKE,
        value = JsonPrimitive(pattern),
        ignoreCase = false
    )

infix fun FieldPath.likeIgnoreCase(pattern: String) =
    comparison(
        operator = Operator.LIKE,
        value = JsonPrimitive(pattern),
        ignoreCase = true
    )

fun FieldPath.like(pattern: String, ignoreCase: Boolean) =
    comparison(
        operator = Operator.LIKE,
        value = JsonPrimitive(pattern),
        ignoreCase = ignoreCase
    )

private fun FieldPath.comparison(
    operator: Operator,
    value: JsonElement,
    ignoreCase: Boolean = false
): Filter =
    _root_ide_package_.org.evoleq.iql.data.ComparisonFilter(
        field = _root_ide_package_.org.evoleq.iql.data.SimpleFieldRef(path),
        operator = operator,
        value = value,
        ignoreCase = ignoreCase
    )


private fun json(value: Any?): JsonElement =
    when (value) {

        null ->
            JsonPrimitive("null")

        is JsonElement ->
            value

        is String ->
            JsonPrimitive(value)

        is Int ->
            JsonPrimitive(value)

        is Long ->
            JsonPrimitive(value)

        is Double ->
            JsonPrimitive(value)

        is Float ->
            JsonPrimitive(value)

        is Boolean ->
            JsonPrimitive(value)

        else ->
            error(
                "Unsupported IQL value type: ${value::class.simpleName}"
            )
    }
