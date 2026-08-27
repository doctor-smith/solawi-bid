package org.evoleq.exposedx.iql

import org.evoleq.iql.data.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll

fun execute(
    query: Query,
    table: Table,
    registry: Registry
): List<ResultRow> {


    val predicate =
        query.filter?.let {
            ExposedCompiler(registry).compile(
                filter = it,
                table = table
            )
        }

    return table
        .selectAll()
        .apply {
            predicate?.let {
                andWhere { it }
            }
        }
        .toList()
}
