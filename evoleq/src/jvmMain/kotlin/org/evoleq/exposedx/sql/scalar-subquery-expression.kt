package org.evoleq.exposedx.sql


import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.QueryBuilder

class ScalarSubqueryExpression<T>(
    override val columnType: IColumnType,
    private val sql: QueryBuilder.() -> Unit
) : ExpressionWithColumnType<T>() {

    override fun toQueryBuilder(
        queryBuilder: QueryBuilder
    ) {
        queryBuilder.append("(")
        sql(queryBuilder)
        queryBuilder.append(")")
    }
}
