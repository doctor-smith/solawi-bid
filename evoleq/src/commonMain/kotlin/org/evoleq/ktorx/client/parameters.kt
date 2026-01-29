package org.evoleq.ktorx.client

import kotlinx.serialization.Serializable

typealias QueryParams = List<Pair<String, String>>

@Serializable
abstract class Parameters {
    @Serializable(with = QueryParamsSerializer::class)
    abstract val queryParams: QueryParams
}

@Serializable
abstract class EmptyParams: Parameters() {
    override val queryParams: QueryParams
        get() = emptyList()
}



fun QueryParams.toQueryString(): String =
    if (isEmpty()) ""
    else joinToString(
        prefix = "?",
        separator = "&"
    ) { (key, value) ->
        "${encode(key)}=${encode(value)}"
    }
class QueryParamDsl {
    private val params = mutableListOf<Pair<String, String>>()

    operator fun String.plusAssign(value: Any) {
        params += this to value.toString()
    }

    operator fun <T> String.plusAssign(values: Iterable<T>) {
        values.forEach { value ->
            params += this to value.toString()
        }
    }

    fun build(): QueryParams = params
}

fun queryParams(block: QueryParamDsl.() -> Unit): QueryParams =
    QueryParamDsl().apply(block).build()

