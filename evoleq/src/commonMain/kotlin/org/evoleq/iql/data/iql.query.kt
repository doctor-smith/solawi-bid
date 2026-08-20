package org.evoleq.iql.data

import kotlinx.serialization.Serializable
import org.evoleq.ktorx.pages.Page
import org.evoleq.ktorx.pages.Pagination

@Serializable
data class Query(
    val filter: Filter? = null,
    val sort: List<Sort> = emptyList(),
    override val pageInfo: Page.Request
) : Pagination.Request

@Serializable
data class Sort(
    val field: String,
    val direction: SortDirection = SortDirection.ASC
)

@Serializable
enum class SortDirection {
    ASC,
    DESC
}
