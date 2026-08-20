package org.evoleq.exposedx.iql

import kotlinx.serialization.json.*
import org.evoleq.iql.data.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq

@Suppress("TooManyFunctions")
class ExposedCompiler(
    private val registry: Registry
) {

    fun compile(
        filter: Filter,
        table: Table
    ): Op<Boolean> =
        when (filter) {

            is AndFilter ->
                compileAnd(filter, table)

            is OrFilter ->
                compileOr(filter, table)

            is NotFilter ->
                not(
                    compile(filter.filter, table)
                )

            is ComparisonFilter ->
                compileComparison(filter)

            is InFilter ->
                compileIn(filter)

            is IsNullFilter ->
                compileIsNull(filter)

            is QuantifierFilter ->
                compileQuantifier(filter, table)
        }

    // -------------------------------------------------------------------------
    // AND / OR
    // -------------------------------------------------------------------------

    private fun compileAnd(
        filter: AndFilter,
        table: Table
    ): Op<Boolean> {

        require(filter.filters.isNotEmpty()) {
            "AND requires at least one filter"
        }

        return filter.filters
            .map { compile(it, table) }
            .reduce(Op<Boolean>::and)
    }

    private fun compileOr(
        filter: OrFilter,
        table: Table
    ): Op<Boolean> {

        require(filter.filters.isNotEmpty()) {
            "OR requires at least one filter"
        }

        return filter.filters
            .map { compile(it, table) }
            .reduce(Op<Boolean>::or)
    }

    // -------------------------------------------------------------------------
    // Comparison
    // -------------------------------------------------------------------------

    private fun compileComparison(
        filter: ComparisonFilter
    ): Op<Boolean> {

        val resolved = resolveField(filter.field)

        val column =
            registry.getColumn(
                resolved.entity,
                resolved.field
            )

        return when (resolved.info.type) {

            FieldType.STRING ->
                compileStringComparison(
                    column,
                    filter.operator,
                    filter.value
                )

            FieldType.INTEGER ->
                compileIntComparison(
                    column,
                    filter.operator,
                    filter.value
                )

            FieldType.LONG ->
                compileLongComparison(
                    column,
                    filter.operator,
                    filter.value
                )

            FieldType.DOUBLE ->
                compileDoubleComparison(
                    column,
                    filter.operator,
                    filter.value
                )

            FieldType.BOOLEAN ->
                compileBooleanComparison(
                    column,
                    filter.operator,
                    filter.value
                )

            FieldType.UUID,
            FieldType.DATE,
            FieldType.DATETIME ->
                throw IllegalArgumentException(
                    "Comparison for ${resolved.info.type} is not supported"
                )
        }
    }

    private fun compileStringComparison(
        column: Column<*>,
        operator: Operator,
        value: JsonElement
    ): Op<Boolean> {

        val typedValue =
            value.jsonPrimitive.content

        return when (operator) {

            Operator.EQ,
            Operator.NE ->
                typedComparison(
                    column,
                    operator,
                    typedValue
                )

            Operator.GT,
            Operator.GTE,
            Operator.LT,
            Operator.LTE ->
                typedOrderingComparison(
                    column,
                    operator,
                    typedValue
                )
        }
    }

    private fun compileIntComparison(
        column: Column<*>,
        operator: Operator,
        value: JsonElement
    ): Op<Boolean> {

        val typedValue =
            value.jsonPrimitive.int

        return when (operator) {

            Operator.EQ,
            Operator.NE ->
                typedComparison(
                    column,
                    operator,
                    typedValue
                )

            Operator.GT,
            Operator.GTE,
            Operator.LT,
            Operator.LTE ->
                typedOrderingComparison(
                    column,
                    operator,
                    typedValue
                )
        }
    }

    private fun compileLongComparison(
        column: Column<*>,
        operator: Operator,
        value: JsonElement
    ): Op<Boolean> {

        val typedValue =
            value.jsonPrimitive.long

        return when (operator) {

            Operator.EQ,
            Operator.NE ->
                typedComparison(
                    column,
                    operator,
                    typedValue
                )

            Operator.GT,
            Operator.GTE,
            Operator.LT,
            Operator.LTE ->
                typedOrderingComparison(
                    column,
                    operator,
                    typedValue
                )
        }
    }

    private fun compileDoubleComparison(
        column: Column<*>,
        operator: Operator,
        value: JsonElement
    ): Op<Boolean> {

        val typedValue =
            value.jsonPrimitive.double

        return when (operator) {

            Operator.EQ,
            Operator.NE ->
                typedComparison(
                    column,
                    operator,
                    typedValue
                )

            Operator.GT,
            Operator.GTE,
            Operator.LT,
            Operator.LTE ->
                typedOrderingComparison(
                    column,
                    operator,
                    typedValue
                )
        }
    }

    private fun compileBooleanComparison(
        column: Column<*>,
        operator: Operator,
        value: JsonElement
    ): Op<Boolean> {

        val typedValue =
            value.jsonPrimitive.boolean

        return when (operator) {

            Operator.EQ,
            Operator.NE ->
                typedComparison(
                    column,
                    operator,
                    typedValue
                )

            Operator.GT,
            Operator.GTE,
            Operator.LT,
            Operator.LTE ->
                throw IllegalArgumentException(
                    "Operator $operator is not supported for BOOLEAN"
                )
        }
    }

    /**
     * Equality works for arbitrary column types.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> typedComparison(
        column: Column<*>,
        operator: Operator,
        value: T
    ): Op<Boolean> {

        val typedColumn =
            column as Column<T>

        return when (operator) {

            Operator.EQ ->
                typedColumn eq value

            Operator.NE ->
                typedColumn neq value

            else ->
                throw IllegalArgumentException(
                    "Operator $operator is not an equality operator"
                )
        }
    }

    /**
     * Ordering requires T to be Comparable<T>.
     *
     * This is deliberately separate from typedComparison(), because
     * equality does not require Comparable.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T : Comparable<T>> typedOrderingComparison(
        column: Column<*>,
        operator: Operator,
        value: T
    ): Op<Boolean> {

        val typedColumn =
            column as Column<T>

        return when (operator) {

            Operator.GT ->
                typedColumn greater value

            Operator.GTE ->
                typedColumn greaterEq value

            Operator.LT ->
                typedColumn less value

            Operator.LTE ->
                typedColumn lessEq value

            else ->
                throw IllegalArgumentException(
                    "Operator $operator is not an ordering operator"
                )
        }
    }

    // -------------------------------------------------------------------------
    // IN
    // -------------------------------------------------------------------------

    private fun compileIn(
        filter: InFilter
    ): Op<Boolean> {

        val resolved =
            resolveField(filter.field)

        val column =
            registry.getColumn(
                resolved.entity,
                resolved.field
            )

        return when (resolved.info.type) {

            FieldType.STRING ->
                typedIn(
                    column,
                    filter.values.map {
                        it.jsonPrimitive.content
                    }
                )

            FieldType.INTEGER ->
                typedIn(
                    column,
                    filter.values.map {
                        it.jsonPrimitive.int
                    }
                )

            FieldType.LONG ->
                typedIn(
                    column,
                    filter.values.map {
                        it.jsonPrimitive.long
                    }
                )

            FieldType.DOUBLE ->
                typedIn(
                    column,
                    filter.values.map {
                        it.jsonPrimitive.double
                    }
                )

            FieldType.BOOLEAN ->
                typedIn(
                    column,
                    filter.values.map {
                        it.jsonPrimitive.boolean
                    }
                )

            FieldType.UUID,
            FieldType.DATE,
            FieldType.DATETIME ->
                throw IllegalArgumentException(
                    "IN for ${resolved.info.type} is not supported"
                )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> typedIn(
        column: Column<*>,
        values: List<T>
    ): Op<Boolean> {

        val typedColumn =
            column as Column<T>

        return typedColumn inList values
    }

    // -------------------------------------------------------------------------
    // IS NULL
    // -------------------------------------------------------------------------

    private fun compileIsNull(
        filter: IsNullFilter
    ): Op<Boolean> {

        val resolved =
            resolveField(filter.field)

        return registry
            .getColumn(
                resolved.entity,
                resolved.field
            )
            .isNull()
    }

    // -------------------------------------------------------------------------
    // Quantifiers
    // -------------------------------------------------------------------------

    private fun compileQuantifier(
        filter: QuantifierFilter,
        sourceTable: Table
    ): Op<Boolean> {

        val relation =
            registry.getRelation(
                entityName = filter.relation.entity,
                relationName = filter.relation.relation
            )

        val targetTable =
            registry.getEntityTable(
                relation.targetEntity
            )

        return when (filter.quantifier) {

            Quantifier.ANY ->
                compileExists(
                    filter.filter,
                    sourceTable,
                    targetTable,
                    relation
                )

            Quantifier.NONE ->
                not(
                    compileExists(
                        filter.filter,
                        sourceTable,
                        targetTable,
                        relation
                    )
                )

            Quantifier.ALL ->
                not(
                    compileExists(
                        NotFilter(filter.filter),
                        sourceTable,
                        targetTable,
                        relation
                    )
                )
        }
    }

    private fun compileExists(
        filter: Filter,
        sourceTable: Table,
        targetTable: Table,
        relation: RelationInfo
    ): Op<Boolean> {

        val predicate =
            compile(
                filter = filter,
                table = targetTable
            )

        val joinCondition =
            createJoinCondition(
                sourceTable = sourceTable,
                targetTable = targetTable,
                relation = relation
            )

        val query =
            targetTable
                .selectAll()
                .where {
                    joinCondition and predicate
                }

        return exists(query)
    }

    // -------------------------------------------------------------------------
    // Field resolution
    // -------------------------------------------------------------------------

    private data class ResolvedField(
        val entity: String,
        val field: String,
        val info: FieldInfo
    )

    private fun resolveField(
        field: FieldRef
    ): ResolvedField {

        val parts =
            field.path.split(".")

        require(parts.size == 2) {
            "Invalid field path '${field.path}'. " +
                    "Expected '<entity>.<field>'."
        }

        val entity =
            parts[0]

        val fieldName =
            parts[1]

        val info =
            registry.getField(
                entityName = entity,
                fieldName = fieldName
            )

        return ResolvedField(
            entity = entity,
            field = fieldName,
            info = info
        )
    }

    // -------------------------------------------------------------------------
    // Relations
    // -------------------------------------------------------------------------

    private fun createJoinCondition(
        sourceTable: Table,
        targetTable: Table,
        relation: RelationInfo
    ): Op<Boolean> {

        require(relation.joinColumns.isNotEmpty()) {
            "Relation '${relation.name}' has no join columns"
        }

        val inverse =
            relation.inverseJoinColumn
                ?: error(
                    "Relation '${relation.name}' has no inverseJoinColumn"
                )

        val conditions =
            relation.joinColumns.map { sourceColumnName ->

                val sourceColumn =
                    sourceTable.columns.firstOrNull {
                        it.name == sourceColumnName
                    } ?: error(
                        "Source column '$sourceColumnName' not found"
                    )

                val targetColumn =
                    targetTable.columns.firstOrNull {
                        it.name == inverse
                    } ?: error(
                        "Target column '$inverse' not found"
                    )

                columnEquals(
                    sourceColumn,
                    targetColumn
                )
            }

        return conditions.reduce(Op<Boolean>::and)
    }

    @Suppress("UNCHECKED_CAST")
    private fun columnEquals(
        left: Column<*>,
        right: Column<*>
    ): Op<Boolean> {

        return (left as Column<Any>) eq
                (right as Expression<Any>)
    }
}
