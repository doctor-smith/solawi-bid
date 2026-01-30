package org.solyton.solawi.bid.module.distribution.data

import org.solyton.solawi.bid.module.distribution.data.api.ApiDistributionPoint
import org.solyton.solawi.bid.module.distribution.data.api.ApiDistributionPoints
import org.solyton.solawi.bid.module.distribution.data.distributionpoint.DistributionPoint
import org.solyton.solawi.bid.module.user.data.transform.toDomainType


/**
 * Converts an instance of ApiDistributionPoints to a list of its domain type, DistributionPoint.
 *
 * @return a list of DistributionPoint objects, each created by converting the corresponding ApiDistributionPoint instances.
 */
fun ApiDistributionPoints.toDomainType(): List<DistributionPoint> = all.map { it.toDomainType() }

/**
 * Converts an instance of ApiDistributionPoint to its corresponding domain type, DistributionPoint.
 *
 * @return a DistributionPoint object created from the ApiDistributionPoint fields.
 */
fun ApiDistributionPoint.toDomainType(): DistributionPoint = DistributionPoint(
    distributionPointId = id,
    name = name,
    address = address?.toDomainType(),
    organizationId = organizationId
)
