package org.evoleq.exposedx.iql

import org.evoleq.iql.data.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.Expression as SqlExpression


@Suppress("TooManyFunctions", "LargeClass")
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


    private fun createRelationJoinCondition(
        mappingTable: Table,
        targetTable: Table,
        join: RelationJoin
    ): Op<Boolean> {

        require(
            join.mappingColumns.size == join.targetColumns.size
        ) {
            "Relation join for '${join.entity}' has " +
                    "${join.mappingColumns.size} mapping columns but " +
                    "${join.targetColumns.size} target columns"
        }

        val conditions =
            join.mappingColumns.mapIndexed { index, mappingName ->

                val targetName =
                    join.targetColumns[index]

                val mappingColumn =
                    mappingTable.columns.firstOrNull {
                        it.name == mappingName
                    } ?: error(
                        "Mapping column '$mappingName' not found in '${mappingTable.tableName}'"
                    )

                val targetColumn =
                    targetTable.columns.firstOrNull {
                        it.name == targetName
                    } ?: error(
                        "Target column '$targetName' not found in '${targetTable.tableName}'"
                    )

                columnEquals(
                    mappingColumn,
                    targetColumn
                )
            }

        return conditions.reduce(Op<Boolean>::and)
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
            if (filter is ComparisonFilter) {

                val parts =
                    filter.field.path.split(".")

                val firstPart =
                    parts.first()

                val normalRelationExists =
                    targetTable.let {
                        registry
                            .getEntityByTable(it)
                            .relations
                            .containsKey(firstPart)
                    }

                if (!normalRelationExists) {

                    val resolvedJoinField =
                        resolveRelationJoinField(
                            fieldName = filter.field.path,
                            relation = relation
                        )

                    if (resolvedJoinField != null) {

                        val column =
                            registry.getColumn(
                                resolvedJoinField.entity.name,
                                resolvedJoinField.field
                            )

                        registry
                            .lookup(resolvedJoinField.info.type)
                            .compileComparison(
                                column = column,
                                operator = filter.operator,
                                value = filter.value
                            )

                    } else {
                        compile(
                            filter = filter,
                            table = targetTable
                        )
                    }

                } else {

                    // A normal relation on the M:N target entity
                    // always has priority over a relationJoin
                    // with the same name.
                    compile(
                        filter = filter,
                        table = targetTable
                    )
                }

            } else {
                compile(
                    filter = filter,
                    table = targetTable
                )
            }


        /*
         * First join:
         *
         * targetTable <-> mappingTable
         *
         * The mapping target condition may contain multiple columns.
         */
        var query =
            targetTable
                .join(
                    otherTable = mappingTable,
                    joinType = JoinType.INNER,
                    additionalConstraint = {
                        targetCondition
                    }
                )

        /*
         * Additional relation joins.
         *
         * Example:
         *
         * RoleRightContext.contextId -> Contexts.id
         *
         * These joins are defined by RelationInfo.relationJoins.
         */
        relation.relationJoins.forEach { relationJoin ->

            val additionalTargetTable = registry.getTable(relationJoin.entity)

            val joinCondition =
                createRelationJoinCondition(
                    mappingTable = mappingTable,
                    targetTable = additionalTargetTable,
                    join = relationJoin
                )

            query =
                query.join(
                    otherTable = additionalTargetTable,
                    joinType = JoinType.INNER,
                    additionalConstraint = {
                        joinCondition
                    }
                )
        }

        return exists(
            query
                .selectAll()
                .where {
                    sourceCondition and
                            predicate
                }
        )
    }

    // -------------------------------------------------------------------------
    // Field resolution
    // -------------------------------------------------------------------------

    data class ResolvedField(
        val entity: String,
        val field: String,
        val info: FieldInfo,
        val relationPath: List<RelationPathStep> = emptyList(),
        val relationJoinPath: List<RelationJoin> = emptyList()
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

            /*
             * Priority:
             *
             * 1. Normal relation of the current entity
             * 2. Additional relationJoin of the previously resolved M:N relation
             *
             * This is important because relationJoins are not actual
             * relations registered on the current entity.
             */
            val relation =
                sourceEntity.relations[relationName]
                    ?: relationPath.lastOrNull()
                        ?.relation
                        ?.relationJoins
                        ?.firstOrNull { join ->

                            registry.entities().values.any { candidate ->

                                candidate.name == relationName &&
                                        registry
                                            .getEntityTable(candidate.name)
                                            .tableName == join.entity
                            }

                        }
                        ?.let { join ->

                            val targetEntity =
                                registry.entities().values.first { candidate ->

                                    candidate.name == relationName &&
                                            registry
                                                .getEntityTable(candidate.name)
                                                .tableName == join.entity
                                }

                            RelationInfo(
                                name = relationName,
                                type = RelationType.MANY_TO_ONE,
                                targetEntity = targetEntity.name,
                                joinColumns = join.mappingColumns,
                                inverseJoinColumn = join.targetColumns.first()
                            )
                        }

            requireNotNull(relation) {
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

            /*
             * The next relation is always resolved against
             * the target entity of the current relation.
             *
             * For a relationJoin this is the additional joined entity.
             */
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

    /*
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
                sourceEntity.relations[relationName]
                    ?: relationPath.lastOrNull()
                        ?.relation
                        ?.relationJoins
                        ?.firstOrNull { join ->

                            registry.entities().values.any { candidate ->
                                candidate.name == relationName &&
                                        registry.getEntityTable(candidate.name).tableName ==
                                        join.entity
                            }

                        }
                        ?.let { join ->

                            val targetEntity =
                                registry.entities().values.first { candidate ->
                                    candidate.name == relationName &&
                                            registry.getEntityTable(candidate.name).tableName ==
                                            join.entity
                                }

                            RelationInfo(
                                name = relationName,
                                type = RelationType.MANY_TO_ONE,
                                targetEntity = targetEntity.name,
                                joinColumns = join.mappingColumns,
                                inverseJoinColumn = join.targetColumns.first()
                            )
                        }
                    /*?: error(
                        "Unknown relation '$relationName' " +
                                "on entity '${sourceEntity.name}'"
                    )

                     */


            requireNotNull(relation) {
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
*/
    data class ResolvedRelationJoin(
        val entity: EntityType,
        val field: String,
        val info: FieldInfo,
        val relationJoin: RelationJoin?
    )

    @Suppress("ReturnCount")
    private fun resolveRelationJoinField(
        fieldName: String,
        relation: RelationInfo
    ): ResolvedRelationJoin? {

        val joins =
            relation.relationJoins

        if (joins.isEmpty()) {
            return null
        }

        /*
         * Only relation-join qualified fields are handled here.
         *
         * Example:
         *
         *     context.name
         *
         * The first part ("context") is an IQL entity name.
         *
         * RelationJoin.entity, however, contains the physical table name.
         * Therefore we resolve the entity first and compare its table.
         */
        if (!fieldName.contains(".")) {
            return null
        }

        val parts =
            fieldName.split(".", limit = 2)

        val entityName =
            parts[0]

        val nestedField =
            parts[1]

        val entity =
            registry.getEntity(entityName)
                ?: return null

        val tableName =
            registry
                .getEntityTable(entity.name)
                .tableName

        val join =
            joins.firstOrNull {
                it.entity == tableName
            }
                ?: return null

        val info =
            entity.fields[nestedField]
                ?: throw IllegalArgumentException(
                    "Unknown field '$nestedField' " +
                            "on entity '${entity.name}'"
                )

        return ResolvedRelationJoin(
            entity = entity,
            field = nestedField,
            info = info,
            relationJoin = join
        )
    }

    /*
        private fun resolveRelationJoinField(
            fieldName: String,
            relation: RelationInfo
        ): ResolvedRelationJoin? {

            val joins =
                relation.relationJoins

            if (joins.isEmpty()) {
                return null
            }

            // 1. Explicitly qualified:
            // context.name
            if (fieldName.contains(".")) {
                val parts = fieldName.split(".", limit = 2)
                val entityName = parts[0]
                val nestedField = parts[1]

                val join =
                    joins.firstOrNull {
                        it.entity == entityName ||
                                registry.getEntity(entityName)?.table == it.entity
                    }

                if (join != null) {
                    val entity =
                        registry.entities().values.firstOrNull {
                            it.table == join.entity
                        } ?: return null

                    val info =
                        entity.fields[nestedField]
                            ?: throw IllegalArgumentException(
                                "Unknown field '$nestedField' " +
                                        "on entity '${entity.name}'"
                            )

                    return ResolvedRelationJoin(
                        entity = entity,
                        field = nestedField,
                        info = info,
                        relationJoin = join
                    )
                }
            }

            return null
        }
    */
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
