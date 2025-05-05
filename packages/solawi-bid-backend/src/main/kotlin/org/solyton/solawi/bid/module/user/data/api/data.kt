package org.solyton.solawi.bid.module.user.data.api

import kotlinx.serialization.Serializable
import org.evoleq.serializationx.UUIDSerializer
import java.util.*

@Serializable
data class UserD(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val username: String,
    val password: String
)


