package org.evoleq.iql.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.evoleq.ktorx.pages.Page
import org.evoleq.ktorx.pages.Pagination
import org.evoleq.iql.data.Expression as IqlExpression

@Serializable
data class Query(
    val filter: Filter? = null,
    val sort: List<Sort> = emptyList(),
    override val pageInfo: Page.Request
) : Pagination.Request
@Serializable
sealed class Sort {
    abstract val direction: SortDirection


    @Serializable
    @SerialName("field")
    data class Field(
        val field: FieldRef,
        override val direction: SortDirection
    ) : Sort()
    @Serializable
    @SerialName("expression")
    data class Expression(
        val expression: IqlExpression,
        override val direction: SortDirection
    ) : Sort()
}

@Serializable
enum class SortDirection {
    ASC,
    DESC
}
