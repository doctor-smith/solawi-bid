package org.solyton.solawi.bid.application.data.transform.application

import org.solyton.solawi.bid.module.application.data.member.Member
import org.solyton.solawi.bid.module.application.data.organization.Organization
import org.solyton.solawi.bid.module.user.data.organization.Organization as OrganizationUM
import org.solyton.solawi.bid.module.user.data.member.Member as MemberUM

fun List<OrganizationUM>.import(): List<Organization> = map {
    it.import()
}

fun OrganizationUM.import(): Organization = Organization(
    organizationId = organizationId,
    name = name,
    contextId = contextId,
    subOrganizations = subOrganizations.import(),
    members = members.map { it.import() }
)

fun List<MemberUM>.import(): List<Member> = map { it.import() }

fun MemberUM.import(): Member = Member(
    memberId = memberId,
    roles = roles
)
