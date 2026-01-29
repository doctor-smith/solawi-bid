package org.solyton.solawi.bid.module.bid.data.api

import kotlinx.serialization.Serializable
import org.evoleq.ktorx.client.Parameters
import org.evoleq.ktorx.client.QueryParams

typealias ApiShareType = ShareType
typealias ApiShareTypes = ShareTypes

@Serializable
data class ShareType(
    val id: String,
    val name: String,
    val description: String,
    val providerId: String
)

@Serializable
data class ShareTypes(
    val all: List<ShareType>
)

@Serializable
data class CreateShareType(
    val name: String,
    val key: String,
    val description: String,
    val providerId: String
)

@Serializable
data class ReadShareTypes(override val all: QueryParams): Parameters()

@Serializable
data class UpdateShareType(
    val id: String,
    val name: String,
    val key: String,
    val description: String,
    val providerId: String
)
