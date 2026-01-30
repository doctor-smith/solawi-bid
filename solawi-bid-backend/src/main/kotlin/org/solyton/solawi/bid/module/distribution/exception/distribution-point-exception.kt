package org.solyton.solawi.bid.module.distribution.exception

sealed class DistributionPointException(override val message: String): Exception(message) {
    data class DuplicateNameInOrganization(
        val name: String,
        val organizationId: String
    ): DistributionPointException(
        "Duplicate name $name in organization with id $organizationId"
    )

    data class NoSuchDistributionPoint(val distributionPointId: String): DistributionPointException(
        "No such DistributionPoint $distributionPointId"
    )
}
