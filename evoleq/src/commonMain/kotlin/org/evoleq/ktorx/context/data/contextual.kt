package org.evoleq.ktorx.context.data

data class Contextual<T>(
    val context: String,
    val data: T
)
