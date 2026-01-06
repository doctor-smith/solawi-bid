package org.solyton.solawi.bid.module.user.data.address

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite

@Lensify
data class Address(
    @ReadOnly val addressId: String,
    @ReadWrite val recipientName: String,
    @ReadWrite val organizationName: String?,
    @ReadWrite val addressLine1: String,
    @ReadWrite val addressLine2: String,
    @ReadWrite val city: String,
    @ReadWrite val stateOrProvince: String,
    @ReadWrite val postalCode: String,
    @ReadWrite val countryCode: String
) {
    companion object {
        fun default(): Address = Address(
            "",
            "",
            null,
            "",
            "",
            "",
            "",
            "",
            ""
        )}
}
