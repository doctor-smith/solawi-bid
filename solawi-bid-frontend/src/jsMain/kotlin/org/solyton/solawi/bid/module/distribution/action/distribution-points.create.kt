package org.solyton.solawi.bid.module.distribution.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.add
import org.solyton.solawi.bid.module.distribution.data.api.ApiDistributionPoint
import org.solyton.solawi.bid.module.distribution.data.api.CreateDistributionPoint
import org.solyton.solawi.bid.module.distribution.data.api.CreateOrUseAddress
import org.solyton.solawi.bid.module.distribution.data.management.DistributionManagement
import org.solyton.solawi.bid.module.distribution.data.management.distributionPoints
import org.solyton.solawi.bid.module.distribution.data.toDomainType

/**
 * Creates an action to create a distribution point in the system. The action takes in parameters
 * related to the distribution point, constructs a `CreateDistributionPoint` instance, and prepares
 * it for processing by the distribution management system.
 *
 * @param name The name of the distribution point to be created.
 * @param organizationId The ID of the organization associated with the distribution point.
 * @param address An optional parameter representing either an existing address to use or
 *                a new address to create for the distribution point.
 * @param nameSuffix An optional suffix to append to the action's name. Defaults to an empty string.
 * @return An action that, when executed, creates a distribution point and integrates it into the
 *         distribution management system.
 */
fun createDistributionPoint(
    name: String,
    organizationId: String,
    address: CreateOrUseAddress?,
    nameSuffix: String = ""
): Action<DistributionManagement, CreateDistributionPoint, ApiDistributionPoint> = Action(
    name = "CreateDistributionPoint$nameSuffix",
    reader = { _ ->
        CreateDistributionPoint(
            name = name,
            organizationId = organizationId,
            address = address
        )
    },
    endPoint = CreateDistributionPoint::class,
    writer = distributionPoints.add() contraMap { dP: ApiDistributionPoint -> dP.toDomainType()}
)
