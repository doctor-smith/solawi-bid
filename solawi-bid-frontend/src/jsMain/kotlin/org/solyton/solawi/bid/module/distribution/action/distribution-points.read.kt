package org.solyton.solawi.bid.module.distribution.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.solyton.solawi.bid.module.distribution.data.api.ApiDistributionPoints
import org.solyton.solawi.bid.module.distribution.data.api.ReadDistributionPoints
import org.solyton.solawi.bid.module.distribution.data.management.DistributionManagement
import org.solyton.solawi.bid.module.distribution.data.management.distributionPoints
import org.solyton.solawi.bid.module.distribution.data.toDomainType

const val READ_DISTRIBUTION_POINTS = "ReadDistributionPoints"

/**
 * Reads distribution points associated with a specified provider.
 *
 * @param providerId The unique identifier of the provider whose distribution points are to be read.
 * @param nameSuffix An optional suffix to append to the action's name. Defaults to an empty string.
 * @return An action that, when executed, retrieves and processes the distribution points based on the specified provider ID.
 */
fun readDistributionPoints(
    providerId: String,
    nameSuffix: String? = null
) : Action<DistributionManagement, ReadDistributionPoints, ApiDistributionPoints> = Action(
    name = READ_DISTRIBUTION_POINTS.suffixed(nameSuffix),
    reader = { ReadDistributionPoints(listOf("provider" to providerId)) },
    endPoint = ReadDistributionPoints::class,
    writer = distributionPoints.set contraMap {distributionPoints -> distributionPoints.toDomainType()}
)
