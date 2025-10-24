package org.solyton.solawi.bid.module.user.data.api.organization

import kotlinx.serialization.Serializable

@Serializable
data class CreateOrganization(
    val name: String
)

@Serializable
data class CreateChildOrganization(
    val name: String
)
