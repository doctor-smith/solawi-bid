package org.evoleq.iql.dsl


import org.evoleq.iql.data.Filter
import org.evoleq.iql.data.Query
import org.evoleq.iql.data.Sort
import org.evoleq.iql.data.SortDirection
import org.evoleq.ktorx.pages.Page

fun query(
    entity: String? = null,
    block: QueryBuilder.() -> Unit
): Query =
    QueryBuilder(entity)
        .apply(block)
        .build()


class QueryBuilder(private val entity: String? = null) {

    private var _entity: String? = entity

    private var filter: Filter? = null

    private val sort =
        mutableListOf<Sort>()

    private var pageSize: Int = 50

    private var offset: Long = 0

    fun where(
        block: FilterBuilder.() -> Unit
    ) {
        filter = when(_entity){
            null -> org.evoleq.iql.dsl.where( block)
            else -> org.evoleq.iql.dsl.where(_entity!!, block)
        }
    }

    fun select(entity: String) {
        _entity = entity
    }

    fun orderBy(
        field: String,
        direction: SortDirection = SortDirection.ASC
    ) {
        sort += Sort(
            field = field,
            direction = direction
        )
    }

    fun asc(field: String) {
        orderBy(
            field,
            SortDirection.ASC
        )
    }

    fun desc(field: String) {
        orderBy(
            field,
            SortDirection.DESC
        )
    }

    fun page(
        size: Int,
        offset: Long = 0
    ) {
        require(size > 0) {
            "Page size must be greater than zero"
        }

        require(offset >= 0) {
            "Offset must not be negative"
        }

        pageSize = size
        this.offset = offset
    }

    fun build(): Query =
        Query(
            filter = filter,
            sort = sort.toList(),
            pageInfo = Page.Request(
                pageSize = pageSize,
                offset = offset
            )
        )
}
