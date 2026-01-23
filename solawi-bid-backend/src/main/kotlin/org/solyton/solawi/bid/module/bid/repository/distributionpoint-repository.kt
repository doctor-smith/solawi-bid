package org.solyton.solawi.bid.module.bid.repository

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.bid.exception.DistributionPointException
import org.solyton.solawi.bid.module.bid.schema.DistributionPointEntity
import org.solyton.solawi.bid.module.bid.schema.DistributionPointsTable
import org.solyton.solawi.bid.module.user.exception.AddressException
import org.solyton.solawi.bid.module.user.exception.OrganizationException
import org.solyton.solawi.bid.module.user.schema.AddressEntity
import org.solyton.solawi.bid.module.user.schema.AddressesTable
import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import java.util.UUID

fun Transaction.createDistributionPoint(
    name: String,
    addressId: UUID?,
    organizationId: UUID,
    creator: UUID
): DistributionPointEntity {
    val address = validateAddress(addressId)
    validateNameInOrganization(name, organizationId)

    val organization = OrganizationEntity.findById(organizationId)
        ?:throw OrganizationException.NoSuchOrganization(organizationId.toString())

    return DistributionPointEntity.new {
        createdBy = creator
        this.name = name
        this.address = address
        this.organization = organization
    }
}

fun Transaction.readDistributionPointsByOrganization(organizationId: UUID): List<DistributionPointEntity> {
    return DistributionPointEntity.find {
        DistributionPointsTable.organisationId eq organizationId
    }.toList()
}

fun Transaction.updateDistributionPoint(
    distributionPointId: UUID,
    name: String,
    addressId: UUID?,
    organizationId: UUID,
    modifier: UUID
) : DistributionPointEntity {
    val distributionPoint = DistributionPointEntity.findById(distributionPointId)
        ?: throw DistributionPointException.NoSuchDistributionPoint(distributionPointId.toString())

    val nameChanged = name != distributionPoint.name
    val orgChanged =  organizationId != distributionPoint.organization.id.value
    val addressChanged = addressId != distributionPoint.address?.id?.value

    val changed = nameChanged || orgChanged || addressChanged
    if(!changed) return distributionPoint

    if (nameChanged) {
        validateNameInOrganization(name, organizationId)
        distributionPoint.name = name
    }
    if (orgChanged) {
        val organization = OrganizationEntity.findById(organizationId)
            ?: throw OrganizationException.NoSuchOrganization(organizationId.toString())
        distributionPoint.organization = organization
    }
    if(addressChanged) {
        if(addressId == null) {
            distributionPoint.address = null
        } else {
            val address = AddressEntity.findById(addressId)
                ?: throw AddressException.NoSuchAddress(addressId.toString())
            distributionPoint.address = address
        }
    }

    // Set data related to modification
    distributionPoint.modifiedBy = modifier
    distributionPoint.modifiedAt = DateTime.now()

    return distributionPoint
}

fun Transaction.deleteDistributionPoint(distributionPointId: UUID): UUID {
    DistributionPointsTable.deleteWhere {
        DistributionPointsTable.id eq distributionPointId
    }

    return distributionPointId
}

fun Transaction.validateAddress(
    addressId: UUID?
): AddressEntity? {
    if(addressId == null) return null

    return AddressEntity.find {
        AddressesTable.id eq addressId
    }.firstOrNull()?: throw AddressException.NoSuchAddress(addressId.toString())
}

fun Transaction.validateNameInOrganization(
    name: String,
    organizationId: UUID
) {
    val exists = !DistributionPointEntity.find {
        (DistributionPointsTable.organisationId eq organizationId) and (DistributionPointsTable.name eq name)
    }.empty()

    if(exists) throw DistributionPointException.DuplicateNameInOrganization(
        name, organizationId.toString()
    )
}
