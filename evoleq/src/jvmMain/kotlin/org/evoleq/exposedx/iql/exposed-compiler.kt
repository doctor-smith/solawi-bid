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

            is ComparisonFilter -> {
                val currentEntity = registry.getEntityByTable(table)

                compileComparison(filter, currentEntity)
            }
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
        filter: ComparisonFilter,
        currentEntity: EntityType? = null
    ): Op<Boolean> {

        val resolved =
            resolveField(
                filter.field,
                currentEntity
            )

        if (resolved.relationPath.isEmpty()) {

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

        val targetColumn =
            registry.getColumn(
                resolved.entity,
                resolved.field
            )

        val predicate =
            registry
                .lookup(resolved.info.type)
                .compileComparison(
                    column = targetColumn,
                    operator = filter.operator,
                    value = filter.value
                )

        return compileRelationPath(
            steps = resolved.relationPath,
            predicate = predicate
        )
    }
    private fun compileRelationPath(
        steps: List<RelationPathStep>,
        predicate: Op<Boolean>
    ): Op<Boolean> {

        require(steps.isNotEmpty()) {
            "Relation path must not be empty"
        }

        val step =
            steps.first()

        val sourceTable =
            registry.getEntityTable(
                step.sourceEntity.name
            )

        val targetTable =
            registry.getEntityTable(
                step.targetEntity.name
            )

        val nestedPredicate =
            if (steps.size == 1) {
                predicate
            } else {
                compileRelationPath(
                    steps = steps.drop(1),
                    predicate = predicate
                )
            }

        return when {
            step.relation.mapping != null ->
                compileManyToManyRelationPath(
                    sourceTable = sourceTable,
                    targetTable = targetTable,
                    relation = step.relation,
                    predicate = nestedPredicate
                )

            else ->
                compileDirectRelationPath(
                    sourceTable = sourceTable,
                    targetTable = targetTable,
                    relation = step.relation,
                    predicate = nestedPredicate
                )
        }
    }
    private fun compileDirectRelationPath(
        sourceTable: Table,
        targetTable: Table,
        relation: RelationInfo,
        predicate: Op<Boolean>
    ): Op<Boolean> {

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

    private fun compileManyToManyRelationPath(
        sourceTable: Table,
        targetTable: Table,
        relation: RelationInfo,
        predicate: Op<Boolean>
    ): Op<Boolean> {

        val mapping =
            relation.mapping
                ?: error(
                    "Expected mapping relation"
                )

        val mappingTable =
            registry.getMappingTable(
                mapping.table
            )

        val sourceCondition =
            createMappingSourceCondition(
                sourceTable = sourceTable,
                mappingTable = mappingTable,
                mapping = mapping
            )

        val targetCondition =
            createMappingTargetCondition(
                mappingTable = mappingTable,
                targetTable = targetTable,
                mapping = mapping
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
                    sourceCondition and
                            targetCondition and
                            predicate
                }

        return exists(query)
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

        val sourceCondition =
            createMappingSourceCondition(
                sourceTable = sourceTable,
                mappingTable = mappingTable,
                mapping = mapping
            )

        val targetCondition =
            createMappingTargetCondition(
                mappingTable = mappingTable,
                targetTable = targetTable,
                mapping = mapping
            )

        val predicate =
            compile(
                filter = filter,
                table = targetTable
            )

        val query =
            targetTable
                .join(
                    otherTable = mappingTable,
                    joinType = JoinType.INNER,
                    additionalConstraint = {
                        targetCondition
                    }
                )
                .selectAll()
                .where {
                    sourceCondition and predicate
                }

        return exists(query)
    }

    // -------------------------------------------------------------------------
    // Field resolution
    // -------------------------------------------------------------------------

    data class ResolvedField(
        val entity: String,
        val field: String,
        val info: FieldInfo,
        val relationPath: List<RelationPathStep> = emptyList()
    )
    data class RelationPathStep(
        val sourceEntity: EntityType,
        val relation: RelationInfo,
        val targetEntity: EntityType
    )

    /**
     * Resolves a field reference against the registry.
     *
     * Unqualified field paths (e.g. `firstName`) require `currentEntity`
     * to determine the entity in which the field is resolved.
     * Qualified field paths (e.g. `UserProfile.firstName`) contain their
     * entity explicitly and therefore do not require `currentEntity`.
     */
    @JvmOverloads
    fun resolveField(
        field: FieldRef,
        currentEntity: EntityType? = null
    ): ResolvedField {

        val parts =
            field.path.split(".")

        require(parts.isNotEmpty()) {
            "Empty field path"
        }

        var entity: EntityType
        var index: Int
        val firstPart = parts.first()

        val isRelation =
            currentEntity != null &&
                    currentEntity.relations.containsKey(firstPart)

        val isExplicitEntity =
            registry.getEntity(firstPart) != null

        if (isRelation) {
            // Resolve relative to the current entity.
            entity = currentEntity
            index = 0
        } else if (isExplicitEntity) {
            // Explicit entity prefix:
            // User.name
            entity = currentEntity?:
                registry.getEntityOrThrow(firstPart)

            index = 1
        } else {
            // Resolve relative to the current entity.
            entity =
                requireNotNull(currentEntity) {
                    "Cannot resolve field '${field.path}' without a current entity"
                }

            index = 0
        }

        val relationPath =
            mutableListOf<RelationPathStep>()

        while (index < parts.lastIndex) {

            val sourceEntity =
                entity

            val relationName =
                parts[index]

            val relation =
                requireNotNull(
                    sourceEntity.relations[relationName]
                ) {
                    "Unknown relation '$relationName' " +
                            "on entity '${sourceEntity.name}'"
                }

            val targetEntity =
                registry.getEntityOrThrow(
                    relation.targetEntity
                )

            relationPath +=
                RelationPathStep(
                    sourceEntity = sourceEntity,
                    relation = relation,
                    targetEntity = targetEntity
                )

            // The next relation is resolved against
            // the target entity of the current relation.
            entity =
                targetEntity

            index++
        }

        val fieldName =
            parts.last()

        val fieldInfo =
            requireNotNull(entity.fields[fieldName]) {
                "Unknown field '$fieldName' " +
                        "on entity '${entity.name}'"
            }

        return ResolvedField(
            entity = entity.name,
            field = fieldName,
            info = fieldInfo,
            relationPath = relationPath
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
