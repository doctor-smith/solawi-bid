package org.solyton.solawi.bid.module.distribution.data.api

import kotlinx.serialization.Serializable
import org.evoleq.ktorx.client.EmptyParams
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
    val organizationId: String,
    val address: Address?
)

@Serializable
data object ReadDistributionPoints : EmptyParams()

@Serializable
data class ReadDistributionPoint(
    val id: String
)

@Serializable
data class CreateDistributionPoint(
    val name: String,
    val organizationId: String,
    val address: CreateOrUseAddress?
)

@Serializable
data class UpdateDistributionPoint(
    val id: String,
    val name: String,
    val organizationId: String,
    val address: Address?
)

@Serializable
data class DeleteDistributionPoint(
    val id: String
)

@Serializable
sealed class CreateOrUseAddress {

    @Serializable
    data class Use(val addressId: String) : CreateOrUseAddress()

    @Serializable
    data class Create(
        val recipientName: String,
        val organizationName: String?,
        val addressLine1: String,
        val addressLine2: String,
        val city: String,
        val stateOrProvince: String,
        val postalCode: String,
        val countryCode: String
    ) : CreateOrUseAddress()
}
