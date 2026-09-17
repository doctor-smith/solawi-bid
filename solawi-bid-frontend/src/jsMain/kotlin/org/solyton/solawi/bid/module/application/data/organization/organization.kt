package org.solyton.solawi.bid.module.application.data.organization

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.solyton.solawi.bid.module.application.data.member.Member

@Lensify data class Organization(
    @ReadOnly val organizationId: String,
    @ReadWrite val name: String,
    @ReadOnly val contextId: String,
    @ReadWrite val subOrganizations: List<Organization> = listOf(),
    @ReadWrite val members: List<Member> = listOf()
)
