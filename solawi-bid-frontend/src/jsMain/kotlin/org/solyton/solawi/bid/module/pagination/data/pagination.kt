package org.solyton.solawi.bid.module.pagination.data

typealias PaginationData = Pagination

data class Pagination(
    val totalNumberOfItems: Int,
    val page: Int,
    val itemsPerPage: Int,
    val minimalNumberOfItemsPerPage: Int = 10,
    val incrementBy: Int = 10
) {
    val totalPages: Int = totalNumberOfItems / itemsPerPage + if (totalNumberOfItems % itemsPerPage > 0) 1 else 0
}
