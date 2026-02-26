package org.solyton.solawi.bid.module.banking.data.user

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.permissions.data.Permissions
import org.solyton.solawi.bid.module.user.data.organization.Organization

@Lensify
data class User(
    @ReadWrite val username: String = "",
    @ReadOnly val permissions: Permissions = Permissions(),
    @ReadOnly val organizations: List<Organization> = listOf()
)
