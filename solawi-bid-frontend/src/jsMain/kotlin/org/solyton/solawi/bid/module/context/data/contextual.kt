package org.solyton.solawi.bid.module.context.data

data class Contextual<T>(
    val context: String,
    val data: T
)
