package org.solyton.solawi.bid.module.user.data.api.organization

import kotlinx.serialization.Serializable
import org.solyton.solawi.bid.module.permission.data.api.ApiRole

typealias ApiOrganization = Organization
typealias ApiOrganizations = Organizations
typealias ApiMember = Member

@Serializable
data class Organizations(
    val all: List<Organization>
)

@Serializable
data class Organization(
    val id: String,
    val name: String,
    val contextId: String,
    val subOrganizations: List<Organization>,
    val members: List<Member>
)

@Serializable
data class Member(
    val userId: String,
    val username: String,
    val roles: List<ApiRole>
)
