package org.solyton.solawi.bid.application.data

import kotlinx.serialization.Serializable

@Serializable
data class BuildInfo(
    val commit: String,
    val buildTime: String
)
