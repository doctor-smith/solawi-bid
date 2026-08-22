package org.solyton.solawi.bid.module.user.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias AddressesTable = Addresses
typealias AddressEntity = Address

object Addresses : AuditableUUIDTable("addresses") {

    val userProfileId = optReference(
        "user_profile_id",
        UserProfiles,
        onDelete = ReferenceOption.SET_NULL
    )
    // Name of the recipient
    val recipientName = varchar("recipient_name", 255)
    // Name of the organization (optional)
    val organizationName = varchar("organization_name", 255).nullable()
    // Main street address
    val addressLine1 = varchar("address_line_1", 255)
    // Additional address details (e.g., apartment/suite)
    val addressLine2 = varchar("address_line_2", 255)
    // City or locality
    val city = varchar("city", 100)
    // State, province, or administrative area
    val stateOrProvince = varchar("state_or_province", 100)
    // ZIP or postal code
    val postalCode = varchar("postal_code", 20)
    // ISO 3166-1 alpha-2 country code
    val countryCode = char("country_code",2)

}

class Address(id : EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<Address>(Addresses)

    var userProfile by UserProfile optionalReferencedOn  Addresses.userProfileId
    var recipientName by Addresses.recipientName
    var organizationName by Addresses.organizationName
    var addressLine1 by Addresses.addressLine1
    var addressLine2 by Addresses.addressLine2
    var city by Addresses.city
    var stateOrProvince by Addresses.stateOrProvince
    var postalCode by Addresses.postalCode
    var countryCode by Addresses.countryCode


    override var createdAt: DateTime by Addresses.createdAt
    override var createdBy: UUID by Addresses.createdBy
    override var modifiedAt: DateTime? by Addresses.modifiedAt
    override var modifiedBy: UUID? by Addresses.modifiedBy
}
