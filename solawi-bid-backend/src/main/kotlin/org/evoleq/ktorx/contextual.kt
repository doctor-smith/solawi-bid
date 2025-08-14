package org.evoleq.ktorx

import java.util.*

data class Contextual<T>(
    val userId: UUID,
    val context: String,
    val data: T
)
