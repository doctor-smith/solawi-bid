package org.solyton.solawi.bid.module.bid.data.api

import kotlinx.serialization.Serializable

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
    val description: String,
    val providerId: String
)

@Serializable
data class UpdateShareType(
    val id: String,
    val name: String,
    val description: String,
    val providerId: String
)
