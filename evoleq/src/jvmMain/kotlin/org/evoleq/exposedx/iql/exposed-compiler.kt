package org.evoleq.exposedx.iql

import org.evoleq.iql.data.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.Expression as SqlExpression

@Suppress("TooManyFunctions")
class ExposedCompiler(
    private val registry: Registry,
    private val expressionCompiler: ExposedExpressionCompiler = ExposedExpressionCompiler(registry)
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

            is ExpressionComparisonFilter -> {
                val currentEntity = registry.getEntityByTable(table)
                val left =
                    expressionCompiler.compile(
                        expression = filter.expression,
                        sourceTable = table,
                        currentEntity = currentEntity.name
                    )

                registry
                    .lookup(left.fieldType)
                    .compileComparison(
                        expression = left.expression,
                        operator = filter.operator,
                        value = filter.value
                    )
            }


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
        sourceTable: Table,
        targetTable: Table,
        relation: RelationInfo
    ): Op<Boolean> {

        return if (relation.mapping != null) {
            compileManyToManyExists(
                filter,
                sourceTable,
                targetTable,
                relation
            )
        } else {
            compileDirectExists(
                filter,
                sourceTable,
                targetTable,
                relation
            )
        }
    }


    private fun compileDirectExists(
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

    private fun compileManyToManyExists(
        filter: Filter,
        sourceTable: Table,
        targetTable: Table,
        relation: RelationInfo
    ): Op<Boolean> {

        val mapping =
            relation.mapping
                ?: error("Expected mapping relation")

        val mappingTable =
            registry.getMappingTable(mapping.table)

        val sourceJoin =
            createMappingSourceCondition(
                sourceTable,
                mappingTable,
                mapping
            )

        val targetJoin =
            createMappingTargetCondition(
                mappingTable,
                targetTable,
                mapping
            )

        val predicate =
            compile(
                filter,
                targetTable
            )

        val query =
            targetTable
                .join(
                    mappingTable,
                    JoinType.INNER,
                    onColumn =
                        mappingTable.columns.first {
                            it.name ==
                                    mapping.mappingTargetColumns.first()
                        },
                    otherColumn =
                        targetTable.columns.first {
                            it.name ==
                                    mapping.targetColumns.first()
                        }
                )
                .selectAll()
                .where {
                    sourceJoin and
                            targetJoin and
                            predicate
                }

        return exists(query)
    }

    //--------------------------------------------------------------------------
    // Compile expression comparison
    //--------------------------------------------------------------------------
    /*
    private fun compileExpressionComparison(
        expression: SqlExpression<*>,
        operator: Operator,
        value: JsonElement
    ): Op<Boolean> =
        when (operator) {

            Operator.EQ ->
                expression eq value

            Operator.NE ->
                expression neq value

            Operator.GT ->
                expression greater value

            Operator.GTE ->
                expression greaterEq value

            Operator.LT ->
                expression less value

            Operator.LTE ->
                expression lessEq value

            Operator.LIKE ->
                expression like value
        }

     */
    // -------------------------------------------------------------------------
    // Field resolution
    // -------------------------------------------------------------------------

    private data class ResolvedField(
        val entity: String,
        val field: String,
        val info: FieldInfo
    )

    /**
     * Resolves a field reference against the registry.
     *
     * Unqualified field paths (e.g. `firstName`) require `currentEntity`
     * to determine the entity in which the field is resolved.
     * Qualified field paths (e.g. `UserProfile.firstName`) contain their
     * entity explicitly and therefore do not require `currentEntity`.
     */
    private fun resolveField(
        field: FieldRef,
        currentEntity: String? = null
    ): ResolvedField {

        val parts = field.path.split(".")

        return when (parts.size) {

            // p("firstName")
            1 -> {
                val entityName = requireNotNull(currentEntity) {
                    "Cannot resolve relative field '${field.path}' without a current entity"
                }
                val fieldName = parts[0]

                val entity =
                    registry.getEntityOrThrow(entityName)

                val fieldInfo =
                    entity.fields[fieldName]
                        ?: error(
                            "Unknown field '$fieldName' on entity '$entityName'"
                        )

                ResolvedField(
                    entity = currentEntity,
                    field = fieldName,
                    info = fieldInfo
                )
            }

            // p("UserProfile.firstName")
            2 -> {
                val entityName = parts[0]
                val fieldName = parts[1]

                val entity =
                    registry.getEntityOrThrow(entityName)

                val fieldInfo =
                    entity.fields[fieldName]
                        ?: error(
                            "Unknown field '$fieldName' on entity '$entityName'"
                        )

                ResolvedField(
                    entity = entityName,
                    field = fieldName,
                    info = fieldInfo
                )
            }

            else ->
                error(
                    "Invalid field path '${field.path}'"
                )
        }
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

        val targetColumns =
            when {
                relation.inverseJoinColumns.isNotEmpty() -> {
                    require(
                        relation.inverseJoinColumns.size ==
                                relation.joinColumns.size
                    ) {
                        "Relation '${relation.name}' has ${relation.joinColumns.size} " +
                                "source columns but ${relation.inverseJoinColumns.size} " +
                                "target columns"
                    }

                    relation.inverseJoinColumns
                }

                relation.inverseJoinColumn != null -> {
                    require(relation.joinColumns.size == 1) {
                        "Relation '${relation.name}' has multiple source columns. " +
                                "Use inverseJoinColumns for composite relations."
                    }

                    listOf(relation.inverseJoinColumn)
                }

                else -> {
                    error(
                        "Relation '${relation.name}' has no target join columns"
                    )
                }
            }

        val conditions =
            relation.joinColumns.mapIndexed { index, sourceColumnName ->

                val targetColumnName =
                    targetColumns[index]

                val sourceColumn =
                    sourceTable.columns.firstOrNull {
                        it.name == sourceColumnName
                    } ?: error(
                        "Source column '$sourceColumnName' not found"
                    )

                val targetColumn =
                    targetTable.columns.firstOrNull {
                        it.name == targetColumnName
                    } ?: error(
                        "Target column '$targetColumnName' not found"
                    )

                columnEquals(
                    sourceColumn,
                    targetColumn
                )
            }

        return conditions.reduce(Op<Boolean>::and)
    }
    private fun createMappingSourceCondition(
        sourceTable: Table,
        mappingTable: Table,
        mapping: MappingInfo
    ): Op<Boolean> {

        require(
            mapping.sourceColumns.size ==
                    mapping.mappingSourceColumns.size
        )

        val conditions =
            mapping.sourceColumns.mapIndexed { index, sourceName ->

                val mappingName =
                    mapping.mappingSourceColumns[index]

                val sourceColumn =
                    sourceTable.columns.first {
                        it.name == sourceName
                    }

                val mappingColumn =
                    mappingTable.columns.first {
                        it.name == mappingName
                    }

                columnEquals(
                    sourceColumn,
                    mappingColumn
                )
            }

        return conditions.reduce(Op<Boolean>::and)
    }

    private fun createMappingTargetCondition(
        mappingTable: Table,
        targetTable: Table,
        mapping: MappingInfo
    ): Op<Boolean> {

        require(
            mapping.mappingTargetColumns.size ==
                    mapping.targetColumns.size
        )

        val conditions =
            mapping.mappingTargetColumns.mapIndexed { index, mappingName ->

                val targetName =
                    mapping.targetColumns[index]

                val mappingColumn =
                    mappingTable.columns.first {
                        it.name == mappingName
                    }

                val targetColumn =
                    targetTable.columns.first {
                        it.name == targetName
                    }

                columnEquals(
                    mappingColumn,
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
                (right as SqlExpression<Any>)
    }
}
