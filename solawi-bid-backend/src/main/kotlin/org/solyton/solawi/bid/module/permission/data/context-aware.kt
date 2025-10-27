package org.solyton.solawi.bid.module.permission.data

import org.solyton.solawi.bid.module.permission.schema.ContextEntity

data class ContextAware<T> (
    val data: T,
    val context: ContextEntity
)
