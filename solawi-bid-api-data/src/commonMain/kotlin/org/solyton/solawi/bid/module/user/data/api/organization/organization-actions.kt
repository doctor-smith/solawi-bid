package org.solyton.solawi.bid.module.user.data.api.organization

import kotlinx.serialization.Serializable
import org.evoleq.ktorx.client.EmptyParams

@Serializable
data class CreateOrganization(
    val name: String
)

@Serializable
data class CreateChildOrganization(
    val organizationId: String,
    val name: String
)

@Serializable
data object ReadOrganizations : EmptyParams()

@Serializable
data class UpdateOrganization(
    val id: String,
    val name: String
)

@Serializable
data class DeleteOrganization(
    val id: String
)

@Serializable
data class AddMember(
    val organizationId: String,
    val userId: String,
    val roles: List<String>,
    val status: MembershipStatus
)

@Serializable
data class RemoveMember(
    val organizationId: String,
    val userId: String,
)

@Serializable
data class UpdateMember(
    val organizationId: String,
    val userId: String,
    val roles: List<String>,
    val status: MembershipStatus
)

@Serializable
data class ImportMembers(
    val organizationId: String,
    val usernames: List<String>
)
