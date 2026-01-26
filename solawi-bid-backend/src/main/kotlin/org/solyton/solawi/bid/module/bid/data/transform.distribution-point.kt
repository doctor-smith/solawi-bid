package org.solyton.solawi.bid.module.bid.data

import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.bid.data.api.ApiDistributionPoint
import org.solyton.solawi.bid.module.bid.data.api.ApiDistributionPoints
import org.solyton.solawi.bid.module.bid.schema.DistributionPointEntity
import org.solyton.solawi.bid.module.user.data.toApiType


fun List<DistributionPointEntity>.toApiType(transaction: Transaction): ApiDistributionPoints =
    ApiDistributionPoints(map { it.toApiType(transaction) })

fun DistributionPointEntity.toApiType(transaction: Transaction): ApiDistributionPoint =
    with(transaction) {
        ApiDistributionPoint(
            id = this@toApiType.id.value.toString(),
            name = name,
            organizationId = organization.id.value.toString(),
            address = address?.toApiType()
        )
    }
