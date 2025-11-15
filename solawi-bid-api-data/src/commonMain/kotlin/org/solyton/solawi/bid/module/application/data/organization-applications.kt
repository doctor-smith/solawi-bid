package org.solyton.solawi.bid.module.application.data

import kotlinx.serialization.Serializable

typealias ApiApplicationOrganizationRelations = ApplicationOrganizationRelations
typealias ApiApplicationOrganizationRelation = ApplicationOrganizationRelation

@Serializable
data class ConnectApplicationToOrganization(
    val applicationId: String,
    val organizationId: String,
    val moduleIds: List<String>
)

@Serializable
data class UpdateOrganizationModuleRelations(
    val applicationId: String,
    val organizationId: String,
    val moduleIds: List<String>
)

@Serializable
data class ApplicationOrganizationRelations(
    val all: List<ApplicationOrganizationRelation>
)

@Serializable
data class ApplicationOrganizationRelation(
    val applicationId: String,
    val organizationId: String,
    val moduleIds: List<String>
)
