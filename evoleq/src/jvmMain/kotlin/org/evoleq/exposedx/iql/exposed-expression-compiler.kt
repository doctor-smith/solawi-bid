
package org.evoleq.exposedx.iql

import org.evoleq.exposedx.sql.ScalarSubqueryExpression
import org.evoleq.iql.data.*
import org.evoleq.iql.data.Expression
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.Expression as SqlExpression

/**
 * Result of compiling an IQL expression.
 *
 * The SQL expression alone is not sufficient to determine the semantic
 * value type. Therefore the compiler carries the FieldType alongside
 * the generated Exposed expression.
 */
data class CompiledExpression(
    val expression: ExpressionWithColumnType<*>,
    val fieldType: FieldType
)

/**
 * Compiles the backend-independent IQL Expression AST into Exposed SQL
 * expressions.
 *
 * Collection expressions are not directly represented by an Exposed
 * Expression. They represent query scopes which are subsequently transformed
 * into scalar expressions by aggregation.
 */
@Suppress("LargeClass", "TooManyFunctions", "UnsafeCallOnNullableType")
class ExposedExpressionCompiler(
    private val registry: Registry
) {

    fun compile(
        expression: Expression,
        sourceTable: Table,
        currentEntity: String
    ): CompiledExpression =
        when (expression) {

            is FieldExpression ->
                compileField(
                    expression = expression,
                    currentEntity = currentEntity
                )

            is RelationExpression ->
                error(
                    "Relation expression '${expression.relation.relation}' " +
                            "must be consumed by a collection operation or aggregation"
                )

            is MapExpression ->
                error(
                    "Map expression must be consumed by an aggregation"
                )

            is FlatMapExpression ->
                error(
                    "FlatMap expression must be consumed by an aggregation"
                )

            is FilterExpression ->
                error(
                    "Filter expression must be consumed by an aggregation"
                )

            is AggregateExpression ->
                compileAggregate(
                    expression = expression,
                    sourceTable = sourceTable,
                    currentEntity = currentEntity
                )
        }

    private fun compileField(
        expression: FieldExpression,
        currentEntity: String
    ): CompiledExpression {

        val resolved =
            resolveField(
                field = expression.field,
                currentEntity = currentEntity
            )

        require(resolved.entity == currentEntity) {
            "Field '${expression.field.path}' resolves to entity " +
                    "'${resolved.entity}', but current entity is '$currentEntity'"
        }

        return CompiledExpression(
            expression =
                registry.getColumn(
                    resolved.entity,
                    resolved.field
                ),
            fieldType = resolved.info.type
        )
    }

    private fun compileAggregate(
        expression: AggregateExpression,
        sourceTable: Table,
        currentEntity: String
    ): CompiledExpression {

        return when (val source = expression.source) {

            is RelationExpression ->
                compileRelationAggregate(
                    source = source,
                    aggregation = expression.aggregation,
                    sourceTable = sourceTable,
                    currentEntity = currentEntity
                )

            is MapExpression ->
                compileMappedRelationAggregate(
                    source = source,
                    aggregation = expression.aggregation,
                    sourceTable = sourceTable,
                    currentEntity = currentEntity
                )

            is FilterExpression ->
                compileFilteredRelationAggregate(
                    source = source,
                    aggregation = expression.aggregation,
                    sourceTable = sourceTable,
                    currentEntity = currentEntity
                )

            else ->
                error(
                    "Aggregation '${expression.aggregation}' is not supported " +
                            "for expression '${source::class.simpleName}'"
                )
        }
    }

    private fun compileRelationAggregate(
        source: RelationExpression,
        aggregation: Aggregation,
        sourceTable: Table,
        currentEntity: String
    ): CompiledExpression {

        val relation =
            registry.getRelation(
                entityName = currentEntity,
                relationName = source.relation.relation
            )

        val targetTable =
            registry.getEntityTable(
                relation.targetEntity
            )

        return when (relation.type) {

            RelationType.MANY_TO_MANY ->
                compileManyToManyAggregate(
                    aggregation = aggregation,
                    sourceTable = sourceTable,
                    targetTable = targetTable,
                    relation = relation
                )

            RelationType.ONE_TO_MANY,
            RelationType.MANY_TO_ONE ->
                compileDirectRelationAggregate(
                    aggregation = aggregation,
                    sourceTable = sourceTable,
                    targetTable = targetTable,
                    relation = relation
                )
        }
    }
    private fun compileDirectRelationAggregate(
        aggregation: Aggregation,
        sourceTable: Table,
        targetTable: Table,
        relation: RelationInfo
    ): CompiledExpression {

        val joinCondition =
            createJoinCondition(
                sourceTable = sourceTable,
                targetTable = targetTable,
                relation = relation
            )

        return aggregate(
            aggregation = aggregation,
            table = targetTable,
            where = joinCondition
        )
    }
    private fun compileManyToManyAggregate(
        aggregation: Aggregation,
        sourceTable: Table,
        targetTable: Table,
        relation: RelationInfo
    ): CompiledExpression {

        require(aggregation == Aggregation.COUNT) {
            "Aggregation '$aggregation' requires a mapped field " +
                    "for many-to-many relation '${relation.name}'"
        }

        val mapping =
            relation.mapping
                ?: error(
                    "Many-to-many relation '${relation.name}' has no mapping"
                )

        val mappingTable =
            registry.getMappingTable(mapping.table)

        return aggregateManyToMany(
            aggregation = aggregation,
            sourceTable = sourceTable,
            targetTable = targetTable,
            mappingTable = mappingTable,
            mapping = mapping,
            column = null,
            fieldType = FieldType.LONG
        )
    }
    private fun aggregateManyToMany(
        aggregation: Aggregation,
        sourceTable: Table,
        targetTable: Table,
        mappingTable: Table,
        mapping: MappingInfo,
        column: Column<*>?,
        fieldType: FieldType
    ): CompiledExpression {

        val expression =
            ScalarSubqueryExpression<Any?>(
                when (aggregation) {
                    Aggregation.COUNT ->
                        LongColumnType()

                    else ->
                        requireNotNull(column) {
                            "$aggregation requires a mapped column"
                        }.columnType
                }
            ) {

                append("SELECT ")

                when (aggregation) {

                    Aggregation.COUNT ->
                        append("COUNT(*)")

                    Aggregation.MIN -> {
                        append("MIN(")
                        append(requireNotNull(column))
                        append(")")
                    }

                    Aggregation.MAX -> {
                        append("MAX(")
                        append(requireNotNull(column))
                        append(")")
                    }

                    Aggregation.SUM -> {
                        append("SUM(")
                        append(requireNotNull(column))
                        append(")")
                    }

                    Aggregation.AVG -> {
                        append("AVG(")
                        append(requireNotNull(column))
                        append(")")
                    }
                }

                append(" FROM ")
                append(targetTable.tableName)

                append(" JOIN ")
                append(mappingTable.tableName)

                append(" ON ")

                mapping.mappingTargetColumns
                    .forEachIndexed { index, mappingColumnName ->

                        if (index > 0) {
                            append(" AND ")
                        }

                        val mappingColumn =
                            mappingTable.columns.first {
                                it.name == mappingColumnName
                            }

                        val targetColumn =
                            targetTable.columns.first {
                                it.name == mapping.targetColumns[index]
                            }

                        append(mappingColumn)
                        append(" = ")
                        append(targetColumn)
                    }

                append(" WHERE ")

                mapping.sourceColumns
                    .forEachIndexed { index, sourceColumnName ->

                        if (index > 0) {
                            append(" AND ")
                        }

                        val sourceColumn =
                            sourceTable.columns.first {
                                it.name == sourceColumnName
                            }

                        val mappingColumn =
                            mappingTable.columns.first {
                                it.name == mapping.mappingSourceColumns[index]
                            }

                        append(mappingColumn)
                        append(" = ")
                        append(sourceColumn)
                    }
            }

        return CompiledExpression(
            expression = expression,
            fieldType =
                when (aggregation) {
                    Aggregation.COUNT ->
                        FieldType.LONG

                    else ->
                        fieldType
                }
        )
    }

    private fun aggregateFilteredManyToMany(
        aggregation: Aggregation,
        sourceTable: Table,
        targetTable: Table,
        mappingTable: Table,
        mapping: MappingInfo,
        column: Column<*>?,
        fieldType: FieldType,
        filter: Op<Boolean>
    ): CompiledExpression {

        val targetAggregationColumn = column
            /*
            column?.let {
                targetTable.columns.firstOrNull { candidate ->
                    candidate.name == it.name
                }
            }

             */

        require(
            aggregation == Aggregation.COUNT ||
                    targetAggregationColumn != null
        ) {
            "${aggregation.name} requires a mapped column"
        }

        val expression =
            ScalarSubqueryExpression<Any>(
                columnType =
                    when (fieldType) {
                        FieldType.LONG ->
                            LongColumnType()

                        FieldType.INTEGER ->
                            IntegerColumnType()

                        FieldType.DOUBLE ->
                            DoubleColumnType()

                        FieldType.STRING ->
                            VarCharColumnType(255)

                        FieldType.BOOLEAN ->
                            BooleanColumnType()

                        else ->
                            error(
                                "Unsupported aggregate field type: $fieldType"
                            )
                    }
            ) {
                append("SELECT ")

                when (aggregation) {

                    Aggregation.COUNT ->
                        append("COUNT(*)")

                    Aggregation.MIN -> {
                        append("MIN(")
                        append(targetAggregationColumn!!)
                        append(")")
                    }

                    Aggregation.MAX -> {
                        append("MAX(")
                        append(targetAggregationColumn!!)
                        append(")")
                    }

                    Aggregation.SUM -> {
                        append("SUM(")
                        append(targetAggregationColumn!!)
                        append(")")
                    }

                    Aggregation.AVG -> {
                        append("AVG(")
                        append(targetAggregationColumn!!)
                        append(")")
                    }
                }

                append(" FROM ")
                append(targetTable.tableName)

                append(" INNER JOIN ")
                append(mappingTable.tableName)

                append(" ON ")

                mapping.targetColumns
                    .zip(mapping.mappingTargetColumns)
                    .forEachIndexed { index, (targetColumnName, mappingColumnName) ->

                        if (index > 0) {
                            append(" AND ")
                        }

                        val targetColumn =
                            targetTable.columns.firstOrNull {
                                it.name == targetColumnName
                            }
                                ?: error(
                                    "No target column '$targetColumnName' " +
                                            "registered on table '${targetTable.tableName}'"
                                )

                        val mappingColumn =
                            mappingTable.columns.firstOrNull {
                                it.name == mappingColumnName
                            }
                                ?: error(
                                    "No mapping column '$mappingColumnName' " +
                                            "registered on table '${mappingTable.tableName}'"
                                )

                        append(targetColumn)
                        append(" = ")
                        append(mappingColumn)
                    }

                append(" WHERE ")

                mapping.sourceColumns
                    .zip(mapping.mappingSourceColumns)
                    .forEachIndexed { index, (sourceColumnName, mappingColumnName) ->

                        if (index > 0) {
                            append(" AND ")
                        }

                        val sourceColumn =
                            sourceTable.columns.firstOrNull {
                                it.name == sourceColumnName
                            }
                                ?: error(
                                    "No source column '$sourceColumnName' " +
                                            "registered on table '${sourceTable.tableName}'"
                                )

                        val mappingColumn =
                            mappingTable.columns.firstOrNull {
                                it.name == mappingColumnName
                            }
                                ?: error(
                                    "No mapping column '$mappingColumnName' " +
                                            "registered on table '${mappingTable.tableName}'"
                                )

                        append(mappingColumn)
                        append(" = ")
                        append(sourceColumn)
                    }

                append(" AND ")

                filter.toQueryBuilder(this)
            }

        return CompiledExpression(
            expression = expression,
            fieldType = fieldType
        )
    }

    private fun compileMappedRelationAggregate(
        source: MapExpression,
        aggregation: Aggregation,
        sourceTable: Table,
        currentEntity: String
    ): CompiledExpression {

        val relationExpression =
            source.source as? RelationExpression
                ?: error(
                    "Map currently requires a RelationExpression as its source"
                )

        val relation =
            registry.getRelation(
                entityName = currentEntity,
                relationName = relationExpression.relation.relation
            )

        val targetTable =
            registry.getEntityTable(
                relation.targetEntity
            )

        val resolved =
            resolveField(
                source.field,
                relation.targetEntity
            )

        require(resolved.entity == relation.targetEntity) {
            "Mapped field '${source.field.path}' must belong to " +
                    "target entity '${relation.targetEntity}'"
        }

        val column =
            registry.getColumn(
                relation.targetEntity,
                resolved.field
            )

        return when (relation.type) {

            RelationType.MANY_TO_MANY -> {

                val mapping =
                    relation.mapping
                        ?: error(
                            "Many-to-many relation '${relation.name}' has no mapping"
                        )

                val mappingTable =
                    registry.getMappingTable(mapping.table)

                aggregateManyToMany(
                    aggregation = aggregation,
                    sourceTable = sourceTable,
                    targetTable = targetTable,
                    mappingTable = mappingTable,
                    mapping = mapping,
                    column = column,
                    fieldType = resolved.info.type
                )
            }

            RelationType.ONE_TO_MANY,
            RelationType.MANY_TO_ONE -> {

                val joinCondition =
                    createJoinCondition(
                        sourceTable = sourceTable,
                        targetTable = targetTable,
                        relation = relation
                    )

                aggregate(
                    aggregation = aggregation,
                    table = targetTable,
                    column = column,
                    where = joinCondition
                )
            }
        }
    }


    private fun compileFilteredRelationAggregate(
        source: FilterExpression,
        aggregation: Aggregation,
        sourceTable: Table,
        currentEntity: String
    ): CompiledExpression {

        val (relationExpression, mappedColumn, mappedFieldType) =
            when (val inner = source.source) {

                is RelationExpression -> {

                    val relation =
                        registry.getRelation(
                            entityName = currentEntity,
                            relationName = inner.relation.relation
                        )

                    val comparisonFilter =
                        source.filter as? ComparisonFilter
                            ?: error(
                                "Filtered relation aggregate requires a ComparisonFilter"
                            )

                    val resolved =
                        resolveField(
                            field = comparisonFilter.field,
                            currentEntity = relation.targetEntity
                        )


                    val column =
                        registry.getColumn(
                            resolved.entity,
                            resolved.field
                        )

                    Triple(
                        inner,
                        column,
                        resolved.info.type
                    )
                }

                is MapExpression -> {

                    val relation =
                        inner.source as? RelationExpression
                            ?: error(
                                "Map currently requires a RelationExpression as its source"
                            )

                    val resolved =
                        resolveField(
                            field = inner.field,
                            currentEntity =
                                registry
                                    .getRelation(
                                        entityName = currentEntity,
                                        relationName =
                                            relation.relation.relation
                                    )
                                    .targetEntity
                        )

                    val column =
                        registry.getColumn(
                            resolved.entity,
                            resolved.field
                        )

                    Triple(
                        relation,
                        column,
                        resolved.info.type
                    )
                }

                else ->
                    error(
                        "Filter currently requires a RelationExpression " +
                                "or MapExpression as its source"
                    )
            }

        val relation =
            registry.getRelation(
                entityName = currentEntity,
                relationName = relationExpression.relation.relation
            )

        val targetTable =
            registry.getEntityTable(
                relation.targetEntity
            )

        val filterCondition =
            compileFilter(
                filter = source.filter,
                table = targetTable,
                currentEntity = relation.targetEntity
            )

        return when (relation.type) {

            RelationType.MANY_TO_MANY -> {

                val mapping =
                    relation.mapping
                        ?: error(
                            "Many-to-many relation '${relation.name}' has no mapping"
                        )

                val mappingTable =
                    registry.getMappingTable(mapping.table)

                aggregateFilteredManyToMany(
                    aggregation = aggregation,
                    sourceTable = sourceTable,
                    targetTable = targetTable,
                    mappingTable = mappingTable,
                    mapping = mapping,
                    column = mappedColumn,
                    fieldType = mappedFieldType,
                    filter = filterCondition
                )
            }

            RelationType.ONE_TO_MANY,
            RelationType.MANY_TO_ONE -> {

                val relationCondition =
                    createJoinCondition(
                        sourceTable = sourceTable,
                        targetTable = targetTable,
                        relation = relation
                    )

                aggregate(
                    aggregation = aggregation,
                    table = targetTable,
                    column = mappedColumn,
                    where = relationCondition and filterCondition
                )
            }
        }
    }

    private fun aggregate(
        aggregation: Aggregation,
        table: Table,
        column: Column<*>? = null,
        where: Op<Boolean>
    ): CompiledExpression {

        val columnType =
            when (aggregation) {

                Aggregation.COUNT ->
                    LongColumnType()

                Aggregation.MIN,
                Aggregation.MAX -> {
                    requireNotNull(column) {
                        "$aggregation requires a mapped column"
                    }

                    column.columnType
                }

                Aggregation.SUM -> {
                    requireNotNull(column) {
                        "SUM requires a mapped column"
                    }

                    column.columnType
                }

                Aggregation.AVG ->
                    DoubleColumnType()
            }

        val fieldType =
            when (aggregation) {

                Aggregation.COUNT ->
                    FieldType.LONG

                Aggregation.MIN,
                Aggregation.MAX,
                Aggregation.SUM -> {

                    requireNotNull(column) {
                        "$aggregation requires a mapped column"
                    }

                    // We need the registry here rather than guessing from
                    // the Exposed column type.
                    registry
                        .getFieldType(
                            table = table,
                            column = column
                        )
                }

                Aggregation.AVG ->
                    FieldType.DOUBLE
            }

        val expression =
            ScalarSubqueryExpression<Any?>(
                columnType = columnType
            ) {

                append("SELECT ")

                when (aggregation) {

                    Aggregation.COUNT ->
                        append("COUNT(*)")

                    Aggregation.MIN -> {
                        requireNotNull(column)

                        append("MIN(")
                        append(column)
                        append(")")
                    }

                    Aggregation.MAX -> {
                        requireNotNull(column)

                        append("MAX(")
                        append(column)
                        append(")")
                    }

                    Aggregation.SUM -> {
                        requireNotNull(column)

                        append("SUM(")
                        append(column)
                        append(")")
                    }

                    Aggregation.AVG -> {
                        requireNotNull(column)

                        append("AVG(")
                        append(column)
                        append(")")
                    }
                }

                append(" FROM ")
                append(table)

                append(" WHERE ")
                where.toQueryBuilder(this)
            }

        return CompiledExpression(
            expression = expression,
            fieldType = fieldType
        )
    }

    private fun compileFilter(
        filter: Filter,
        table: Table,
        currentEntity: String
    ): Op<Boolean> =
        when (filter) {

            is AndFilter ->
                filter.filters
                    .map {
                        compileFilter(
                            filter = it,
                            table = table,
                            currentEntity = currentEntity
                        )
                    }
                    .reduce(Op<Boolean>::and)

            is OrFilter ->
                filter.filters
                    .map {
                        compileFilter(
                            filter = it,
                            table = table,
                            currentEntity = currentEntity
                        )
                    }
                    .reduce(Op<Boolean>::or)

            is NotFilter ->
                not(
                    compileFilter(
                        filter = filter.filter,
                        table = table,
                        currentEntity = currentEntity
                    )
                )

            is ComparisonFilter -> {

                val resolved =
                    resolveField(
                        field = filter.field,
                        currentEntity = currentEntity
                    )

                val column =
                    registry.getColumn(
                        resolved.entity,
                        resolved.field
                    )

                registry
                    .lookup(resolved.info.type)
                    .compileComparison(
                        expression = column,
                        operator = filter.operator,
                        value = filter.value
                    )
            }

            is IsNullFilter -> {

                val resolved =
                    resolveField(
                        field = filter.field,
                        currentEntity = currentEntity
                    )

                registry
                    .getColumn(
                        resolved.entity,
                        resolved.field
                    )
                    .isNull()
            }

            is InFilter -> {

                val resolved =
                    resolveField(
                        field = filter.field,
                        currentEntity = currentEntity
                    )

                registry
                    .lookup(resolved.info.type)
                    .compileIn(
                        column =
                            registry.getColumn(
                                resolved.entity,
                                resolved.field
                            ),
                        values = filter.values
                    )
            }

            is QuantifierFilter ->
                error(
                    "Nested quantifier filters are not yet supported " +
                            "inside expression aggregation"
                )

            is ExpressionComparisonFilter -> {

                val left =
                    compile(
                        expression = filter.expression,
                        sourceTable = table,
                        currentEntity = currentEntity
                    )

                registry
                    .lookup(left.fieldType)
                    .compileComparison(
                        expression = left.expression,
                        operator = filter.operator,
                        value = filter.value
                    )
            }
        }

    private data class ResolvedField(
        val entity: String,
        val field: String,
        val info: FieldInfo
    )

    private fun resolveField(
        field: FieldRef,
        currentEntity: String
    ): ResolvedField {

        val parts =
            field.path.split(".")

        return when (parts.size) {

            1 -> {

                val entity =
                    registry.getEntityOrThrow(
                        currentEntity
                    )

                val fieldInfo =
                    entity.fields[parts[0]]
                        ?: error(
                            "Unknown field '${parts[0]}' " +
                                    "on entity '$currentEntity'"
                        )

                ResolvedField(
                    entity = currentEntity,
                    field = parts[0],
                    info = fieldInfo
                )
            }

            2 -> {

                val entityName =
                    parts[0]

                val fieldName =
                    parts[1]

                val entity =
                    registry.getEntityOrThrow(
                        entityName
                    )

                val fieldInfo =
                    entity.fields[fieldName]
                        ?: error(
                            "Unknown field '$fieldName' " +
                                    "on entity '$entityName'"
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

    private fun createJoinCondition(
        sourceTable: Table,
        targetTable: Table,
        relation: RelationInfo
    ): Op<Boolean> {

        val targetColumns =
            when {

                relation.inverseJoinColumns.isNotEmpty() ->
                    relation.inverseJoinColumns

                relation.inverseJoinColumn != null -> {

                    require(
                        relation.joinColumns.size == 1
                    ) {
                        "Relation '${relation.name}' has multiple source " +
                                "columns but only one inverseJoinColumn"
                    }

                    listOf(
                        relation.inverseJoinColumn
                    )
                }

                else ->
                    error(
                        "Relation '${relation.name}' has no target join columns"
                    )
            }

        require(
            targetColumns.size ==
                    relation.joinColumns.size
        ) {
            "Relation '${relation.name}' has mismatching join columns"
        }

        return relation.joinColumns
            .mapIndexed { index, sourceName ->

                val targetName =
                    targetColumns[index]

                val sourceColumn =
                    sourceTable.columns.first {
                        it.name == sourceName
                    }

                val targetColumn =
                    targetTable.columns.first {
                        it.name == targetName
                    }

                (sourceColumn as Column<Any>) eq
                        (targetColumn as SqlExpression<Any>)
            }
            .reduce(Op<Boolean>::and)
    }
}
