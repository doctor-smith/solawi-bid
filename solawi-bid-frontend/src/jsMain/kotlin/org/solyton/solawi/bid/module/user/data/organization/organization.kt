package org.solyton.solawi.bid.module.user.data.organization

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.evoleq.identity.Identity
import org.evoleq.math.Children
import org.solyton.solawi.bid.module.user.data.member.Member

@Lensify data class Organization(
    @ReadOnly val organizationId: String,
    @ReadWrite val name: String,
    @ReadOnly val contextId: String,
    @ReadWrite val subOrganizations: List<Organization> = listOf(),
    @ReadWrite val members: List<Member> = listOf()
) : Children<Organization>, Identity<String> {
    override val getChildren: () -> List<Organization> = {subOrganizations}
    override val setChildren: (List<Organization>) -> Organization = { list ->copy(subOrganizations = list)}
    override val getIdentity: () -> String = {organizationId}
}
