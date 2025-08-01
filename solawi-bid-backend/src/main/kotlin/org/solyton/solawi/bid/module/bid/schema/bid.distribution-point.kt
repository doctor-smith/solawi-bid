package org.solyton.solawi.bid.module.bid.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.solyton.solawi.bid.module.user.schema.Address
import org.solyton.solawi.bid.module.user.schema.Addresses
import org.solyton.solawi.bid.module.user.schema.Organisations
import org.solyton.solawi.bid.module.user.schema.Organization
import java.util.*

typealias DistributionPointsTable = DistributionPoints
typealias DistributionPointEntity = DistributionPoint

object DistributionPoints : UUIDTable("distribution_points") {
    val name = varchar("name", 255)
    val addressId = reference("address_id", Addresses).nullable()
    val organisationId = reference("organization_id", Organisations)
}

class DistributionPoint(id : EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DistributionPoint>(DistributionPoints)

    var name by DistributionPoints.name
    var address by Address optionalReferencedOn DistributionPoints.addressId
    var organization by Organization referencedOn DistributionPoints.organisationId
}
