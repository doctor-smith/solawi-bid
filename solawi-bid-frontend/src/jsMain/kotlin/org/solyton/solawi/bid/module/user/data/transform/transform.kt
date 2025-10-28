package org.solyton.solawi.bid.module.user.data.transform

import org.solyton.solawi.bid.module.user.data.api.organization.ApiMember
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganizations
import org.solyton.solawi.bid.module.user.data.member.Member
import org.solyton.solawi.bid.module.user.data.organization.Organization
import org.solyton.solawi.bid.module.permissions.data.transform.toDomainType

fun ApiOrganizations.toDomainType(): List<Organization> = all.map {
    organization -> organization.toDomainType()
}

fun ApiOrganization.toDomainType(): Organization = Organization(
    organizationId = id,
    name = name,
    contextId = contextId,
    subOrganizations = subOrganizations.map {
        organization -> organization.toDomainType()
    },
    members = members.map {
        member -> member.toDomainType()
    }
)

fun ApiMember.toDomainType(): Member = Member(
    memberId = userId,
    roles = roles.map { role -> role.toDomainType() }
)
