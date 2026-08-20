package org.evoleq.ktorx.pages

import kotlinx.serialization.Serializable

sealed interface Pagination {

    interface Request : Pagination {
        val pageInfo: Page.Request
    }
    interface Response : Pagination {
        val pageInfo: Page.Response
        fun nextRequest(): Request
    }
}

class Page {
    /**
     * Represents a request for paginated data, implementing the `Pagination.Request` interface.
     *
     * @property pageSize The maximum number of items to retrieve in the request.
     * @property offset The starting position of the data to be retrieved.
     */
    @Serializable
    data class Request(
        val pageSize: Int,
        val offset: Long
    )

    /**
     * Represents a paginated response, implementing the `Pagination.Response` interface.
     *
     *
     *  Example Usage:
     *  @Serializable
     *  data class GetUsers(
     *      override val pageInfo: Page.Request,
     *      val data ...
     *  ) : Pagination.Request
     *
     *  @Serializabe
     *  data class Users(
     *      override val pageInfo: Page.Response,
     *      val all: List<User>
     * ) : Pagination.Response
     * @property totalItems The total number of items available in the dataset.
     * @property offset The starting position of the current subset of data.
     * @property pageSize The maximum number of items included in the current response.
     *
     * @see Pagination.Response
     */
    @Serializable
    data class Response(
        val totalItems: Long,
        val offset: Long,
        val pageSize: Int
    ){
        private val currentPageEnd = offset + pageSize
        val hasNext = currentPageEnd < totalItems

        fun nextRequest(): Request = Request(pageSize, offset + pageSize)
    }
}
