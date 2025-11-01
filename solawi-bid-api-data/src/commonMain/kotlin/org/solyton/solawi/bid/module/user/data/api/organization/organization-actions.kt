package org.solyton.solawi.bid.module.user.data.api.organization

import kotlinx.serialization.Serializable

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
data object ReadOrganizations

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
    val roles: List<String>
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
    val roles: List<String>
)
