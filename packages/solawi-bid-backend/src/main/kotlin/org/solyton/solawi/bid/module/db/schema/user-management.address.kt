package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

typealias AddressesTable = Addresses
typealias AddressEntity = Address

object Addresses : UUIDTable("addresses") {
//    val recipientName VARCHAR(255),   -- Name of the recipient
//    organization_name VARCHAR(255),-- Name of the organization (optional)
//    address_line1 VARCHAR(255),    -- Main street address
//    address_line2 VARCHAR(255),    -- Additional address details (e.g., apartment/suite)
//    city VARCHAR(100),             -- City or locality
//    state_or_province VARCHAR(100),-- State, province, or administrative area
//    postal_code VARCHAR(20),       -- ZIP or postal code
//    country_code CHAR(2),          -- ISO 3166-1 alpha-2 country code
    val userProfile = reference("user_profile_id", UserProfiles)
    val recipientName = varchar("recipient_name", 255)
    val organizationName = varchar("organization_name", 255).nullable()
    val addressLine1 = varchar("address_line_1", 255)
    val addressLine2 = varchar("address_line_2", 255)
    val city = varchar("city", 100)
    val stateOrProvince = varchar("state_or_province", 100)
    val postalCode = varchar("postal_code", 20)
    val countryCode = char("country_code",2)

}

class Address(id : EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Address>(Addresses)

    var userProfile by UserProfile referencedOn Addresses.userProfile
    var recipientName by Addresses.recipientName
    var organizationName by Addresses.organizationName
    var addressLine1 by Addresses.addressLine1
    var addressLine2 by Addresses.addressLine2
    var city by Addresses.city
    var stateOrProvince by Addresses.stateOrProvince
    var postalCode by Addresses.postalCode
    var countryCode by Addresses.countryCode


}

fun Address.dummy() = Address.new {

}