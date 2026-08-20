package org.evoleq.exposedx.iql

import org.evoleq.iql.data.Query
import org.evoleq.iql.data.Sort
import org.evoleq.iql.data.SortDirection
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table

data class CompiledQuery(
    val predicate: Op<Boolean>?,
    val orderBy: List<Pair<Expression<*>, SortOrder>>
)

class ExposedQueryCompiler(
    private val registry: Registry
) {

    private val filterCompiler =
        ExposedCompiler(registry)

    fun compile(
        query: Query,
        table: Table
    ): CompiledQuery {

        val predicate =
            query.filter?.let {
                filterCompiler.compile(it, table)
            }

        val orderBy =
            query.sort.map { sort ->
                compileSort(sort)
            }

        return CompiledQuery(
            predicate = predicate,
            orderBy = orderBy
        )
    }

    private fun compileSort(
        sort: Sort
    ): Pair<Expression<*>, SortOrder> {

        val parts = sort.field.split(".")

        require(parts.size == 2) {
            "Invalid sort field: ${sort.field}"
        }

        val entity = parts[0]
        val field = parts[1]

        val column =
            registry.getColumn(entity, field)

        val direction =
            when (sort.direction) {
                SortDirection.ASC -> SortOrder.ASC
                SortDirection.DESC -> SortOrder.DESC
            }

        return column to direction
    }
}
