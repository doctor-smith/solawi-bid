package org.solyton.solawi.bid.module.user.repository

import org.evoleq.math.arrayOf
import org.jetbrains.exposed.sql.Transaction
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.user.exception.AddressException
import org.solyton.solawi.bid.module.user.schema.AddressEntity
import org.solyton.solawi.bid.module.user.schema.AddressesTable
import java.util.*

fun Transaction.createAddress(
    recipientName: String,
    organizationName: String?,
    addressLine1: String,
    addressLine2: String,
    city: String,
    stateOrProvince: String,
    postalCode: String,
    countryCode: String,
    creator: UUID
): AddressEntity {
    return AddressEntity.new {
        createdBy = creator
        this.recipientName = recipientName
        this.organizationName = organizationName
        this.addressLine1 = addressLine1
        this.addressLine2 = addressLine2
        this.city = city
        this.stateOrProvince = stateOrProvince
        this.postalCode = postalCode
        this.countryCode = countryCode
    }
}

fun Transaction.updateAddress(
    addressId: UUID,
    recipientName: String,
    organizationName: String?,
    addressLine1: String,
    addressLine2: String,
    city: String,
    stateOrProvince: String,
    postalCode: String,
    countryCode: String,
    creator: UUID
): AddressEntity {
    val address = validatedAddress(addressId)

    val changes = arrayOf(
        address.recipientName != recipientName,
        address.organizationName != organizationName,
        address.addressLine1 != addressLine1,
        address.addressLine2 != addressLine2,
        address.city != city,
        address.stateOrProvince != stateOrProvince,
        address.postalCode != postalCode,
        address.countryCode != countryCode

    )
    if(changes.none() { it }) return address

    address.recipientName = recipientName
    address.organizationName = organizationName
    address.addressLine1 = addressLine1
    address.addressLine2 = addressLine2
    address.city = city
    address.stateOrProvince = stateOrProvince
    address.postalCode = postalCode
    address.countryCode = countryCode

    address.modifiedAt = DateTime.now()
    address.modifiedBy = creator

    return address
}

fun Transaction.validatedAddress(addressId: UUID) = AddressEntity.find {
    AddressesTable.id eq addressId
}.firstOrNull() ?: throw AddressException.NoSuchAddress("$addressId")
