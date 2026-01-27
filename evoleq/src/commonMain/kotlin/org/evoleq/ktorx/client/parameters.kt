package org.evoleq.ktorx.client

import kotlinx.serialization.Serializable



@Serializable
abstract class Parameters {
    @Serializable(with = QueryParamsSerializer::class)
    abstract val all: Map<String, String>
}

@Serializable
abstract class EmptyParams: Parameters() {
    override val all: Map<String, String>
        get() = emptyMap()
}
