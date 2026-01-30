package org.solyton.solawi.bid.module.distribution.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.update
import org.solyton.solawi.bid.module.distribution.data.api.ApiDistributionPoint
import org.solyton.solawi.bid.module.distribution.data.api.UpdateDistributionPoint
import org.solyton.solawi.bid.module.distribution.data.management.DistributionManagement
import org.solyton.solawi.bid.module.distribution.data.management.distributionPoints
import org.solyton.solawi.bid.module.distribution.data.toDomainType
import org.solyton.solawi.bid.module.user.data.address.Address
import org.solyton.solawi.bid.module.user.data.transform.toApiType

/**
 * Creates an action to update an existing distribution point with the specified parameters.
 * The action generates an `UpdateDistributionPoint` instance, which includes updated properties
 * and prepares it for processing within the distribution management system.
 *
 * @param distributionPointId The unique identifier of the distribution point to be updated.
 * @param name The updated name for the distribution point.
 * @param organizationId The updated organization ID associated with the distribution point.
 * @param address An optional parameter representing the new address details for the distribution
 *                point. Defaults to null if no changes to the address are required.
 * @param nameSuffix An optional suffix to append to the action's name for distinguishing actions.
 *                   Defaults to an empty string.
 * @return An action that, when executed, updates the specified distribution point in the
 *         distribution management system.
 */
fun updateDistributionPoint(
    distributionPointId: String,
    name: String,
    organizationId: String,
    address: Address? = null,
    nameSuffix: String = ""
): Action<DistributionManagement, UpdateDistributionPoint, ApiDistributionPoint> = Action(
    name = "UpdateDistributionPoint$nameSuffix",
    reader = { _ ->
        UpdateDistributionPoint(
            id = distributionPointId,
            name = name,
            organizationId = organizationId,
            address = address?.toApiType()
        )
    },
    endPoint = UpdateDistributionPoint::class,
    writer = distributionPoints.update {
        p,q -> p.distributionPointId == q.distributionPointId
    } contraMap {
        dP: ApiDistributionPoint -> dP.toDomainType()
    }
)
