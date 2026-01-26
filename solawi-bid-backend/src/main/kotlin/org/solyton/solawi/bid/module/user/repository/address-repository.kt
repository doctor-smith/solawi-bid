package org.solyton.solawi.bid.module.user.repository

import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.user.schema.AddressEntity
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
