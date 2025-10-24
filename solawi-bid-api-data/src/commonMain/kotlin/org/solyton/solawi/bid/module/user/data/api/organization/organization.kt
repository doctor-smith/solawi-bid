package org.solyton.solawi.bid.module.user.data.api.organization

import kotlinx.serialization.Serializable

typealias ApiOrganization = Organization

@Serializable
data class Organization(
    val id: String,
    val name: String,
    val subOrganizations: List<Organization>
)
