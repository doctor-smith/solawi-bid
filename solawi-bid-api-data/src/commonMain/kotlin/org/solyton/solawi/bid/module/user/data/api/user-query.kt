package org.solyton.solawi.bid.module.user.data.api

import kotlinx.serialization.Serializable
import org.evoleq.iql.data.Query

@Serializable
data class UserQuery (
    val query: Query
)
