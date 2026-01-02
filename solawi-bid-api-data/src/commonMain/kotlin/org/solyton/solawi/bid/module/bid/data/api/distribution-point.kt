package org.solyton.solawi.bid.module.bid.data.api

import kotlinx.serialization.Serializable
import org.solyton.solawi.bid.module.user.data.api.userprofile.Address

typealias ApiDistributionPoint = DistributionPoint
typealias ApiDistributionPoints = DistributionPoints

@Serializable
data class DistributionPoints(
    val all: List<DistributionPoint>
)

@Serializable
data class DistributionPoint(
    val id: String,
    val name: String,
    val address: Address
)

@Serializable
data object ReadDistributionPoints

@Serializable
data class ReadDistributionPoint(
    val id: String
)

@Serializable
data class CreateDistributionPoint(
    val name: String,
    val address: Address
)

@Serializable
data class UpdateDistributionPoint(
    val id: String,
    val name: String,
    val address: Address
)

@Serializable
data class DeleteDistributionPoint(
    val id: String
)
