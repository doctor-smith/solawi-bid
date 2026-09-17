package org.solyton.solawi.bid.module.application.data.member

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.permissions.data.Role

@Lensify data class Member(
    @ReadOnly val memberId: String,
    @ReadWrite val roles: List<Role>
)
