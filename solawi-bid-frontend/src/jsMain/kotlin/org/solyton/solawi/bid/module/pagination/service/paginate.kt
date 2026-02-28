package org.solyton.solawi.bid.module.pagination.service

import kotlin.math.min

/**
 * Splits this list into pages (chunks) of size [pageSize].
 * Ensures all items are included, even if the last page is not fully filled.
 *
 * @throws IllegalArgumentException if [pageSize] is less than or equal to 0
 */
fun <T> List<T>.paginate(pageSize: Int): List<List<T>> {
    require(pageSize > 0) { "pageSize must be > 0." }
    val totalPages = (size + pageSize - 1) / pageSize
    return List(totalPages) { pageIndex ->
        subList(
            pageIndex * pageSize,
            min((pageIndex + 1) * pageSize, size)
        )
    }
}

/**
 * Returns exactly one page (1-based), e.g. pageNumber=1 -> first page.
 *
 * Notes:
 * - If [pageNumber] is greater than the number of pages, the last page is returned.
 * - If the list is empty, an empty list is returned.
 *
 * @throws IllegalArgumentException if [pageSize] is less than or equal to 0, or if [pageNumber] is less than 1
 */
fun <T> List<T>.paginate(pageSize: Int, pageNumber: Int): List<T> {
    require(pageSize > 0) { "pageSize must be > 0." }
    require(pageNumber >= 1) { "pageNumber must be >= 1." }

    val pages = paginate(pageSize)
    if (pages.isEmpty()) return emptyList()

    // Clamp to the last page so out-of-range page numbers don't crash.
    val index = min(pageNumber, pages.size) - 1
    return pages[index]
}
