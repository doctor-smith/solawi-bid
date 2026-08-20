package org.evoleq.exposedx.iql

import org.evoleq.iql.data.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull

@Suppress("TooManyFunctions")
class ExposedCompiler(
    private val registry: Registry
) {

    fun compile(
        filter: Filter,
        table: org.jetbrains.exposed.sql.Table
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
        table: org.jetbrains.exposed.sql.Table
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
        table: org.jetbrains.exposed.sql.Table
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

        val resolved =
            resolveField(filter.field)

        val column =
            registry.getColumn(
                resolved.entity,
                resolved.field
            )

        return registry
            .lookup(resolved.info.type)
            .compileComparison(
                column = column,
                operator = filter.operator,
                value = filter.value
            )
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

        return registry
            .lookup(resolved.info.type)
            .compileIn(
                column = column,
                values = filter.values
            )
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
        sourceTable: org.jetbrains.exposed.sql.Table
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
        sourceTable: org.jetbrains.exposed.sql.Table,
        targetTable: org.jetbrains.exposed.sql.Table,
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
        sourceTable: org.jetbrains.exposed.sql.Table,
        targetTable: org.jetbrains.exposed.sql.Table,
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
