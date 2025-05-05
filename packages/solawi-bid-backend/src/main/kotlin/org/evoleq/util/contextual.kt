package org.evoleq.util

import java.util.UUID

data class Contextual<T>(
    val userId: UUID,
    val context: String,
    val data: T
)