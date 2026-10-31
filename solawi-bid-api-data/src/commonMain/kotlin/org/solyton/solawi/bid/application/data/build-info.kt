package org.solyton.solawi.bid.application.data

import kotlinx.serialization.Serializable
import org.evoleq.ktorx.client.EmptyParams

@Serializable
data class BuildInfo(
    val commit: String,
    val version: String,
    // val buildTime: String? = null
)

@Serializable
data object ReadBackendBuildInfo: EmptyParams()
