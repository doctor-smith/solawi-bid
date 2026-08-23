package org.evoleq.exposedx.iql

import org.evoleq.iql.data.FieldExpression
import org.evoleq.iql.data.Query
import org.evoleq.iql.data.Sort
import org.evoleq.iql.data.SortDirection
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.*

data class CompiledQuery(
    val predicate: Op<Boolean>?,
    val orderBy: List<Pair<Expression<*>, SortOrder>>,
    val limit: Int?,
    val offset: Long = 0
)

fun <ID : Comparable<ID>, E : Entity<ID>> CompiledQuery.applyTo(
    entityClass: EntityClass<ID, E>
): List<E> {

    var query =
        entityClass.table.selectAll()

    predicate?.let { predicate ->
        query =
            query.where {
                predicate
            }
    }

    if (orderBy.isNotEmpty()) {
        query =
            query.orderBy(
                *orderBy.toTypedArray()
            )
    }

    limit?.let {
        query =
            query.limit(
                n = it,
                offset = offset
            )
    }

    return entityClass
        .wrapRows(query)
        .toList()
}

fun CompiledQuery.applyTo(table: Table): org.jetbrains.exposed.sql.Query {
    var result =
        table
            .selectAll()

    predicate?.let { predicate ->
        result =
            result.where {
                predicate
            }
    }

    if (orderBy.isNotEmpty()) {
        result =
            result.orderBy(
                *orderBy.toTypedArray()
            )
    }

    limit?.let { limit ->
        result = result.limit(
            n = limit,
            offset = offset
        )
    }

    return result
}

class ExposedQueryCompiler(
    private val registry: Registry,
    private val expressionCompiler: ExposedExpressionCompiler = ExposedExpressionCompiler(registry)
) {

    private val filterCompiler =
        ExposedCompiler(registry, expressionCompiler)

    fun compile(
        query: Query,
        table: Table
    ): CompiledQuery {

        val entity = registry.getEntityByTable(table)

        val predicate =
            query.filter?.let {
                filterCompiler.compile(
                    it,
                    table
                )
            }

        val orderBy =
            query.sort.map { sort ->
                compileSort(
                    sort = sort,
                    sourceTable = table,
                    currentEntity = entity.name
                )
            }

        return CompiledQuery(
            predicate = predicate,
            orderBy = orderBy,
            limit = query.pageInfo.pageSize,
            offset = query.pageInfo.offset
        )
    }

    private fun compileSort(
        sort: Sort,
        sourceTable: Table,
        currentEntity: String
    ): Pair<org.jetbrains.exposed.sql.Expression<*>, SortOrder> {

        val compiledExpression =
            when (sort) {

                is Sort.Field ->
                    expressionCompiler.compile(
                        FieldExpression(sort.field),
                        sourceTable,
                        currentEntity
                    )

                is Sort.Expression ->
                    expressionCompiler.compile(
                        sort.expression,
                        sourceTable,
                        currentEntity
                    )
            }

        val direction =
            when (sort.direction) {
                SortDirection.ASC -> SortOrder.ASC
                SortDirection.DESC -> SortOrder.DESC
            }

        return compiledExpression.expression to direction
    }
}
