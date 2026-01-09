package org.solyton.solawi.bid.module.user.data.profile

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.user.data.address.Address

@Lensify data class UserProfile(
    @ReadOnly val userProfileId: String = "",
    @ReadWrite val firstname: String = "",
    @ReadWrite val lastname: String = "",
    @ReadWrite val title: String? = null,
    @ReadWrite val phoneNumber: String? = null,
    @ReadWrite val addresses: List<Address> = listOf()
)
