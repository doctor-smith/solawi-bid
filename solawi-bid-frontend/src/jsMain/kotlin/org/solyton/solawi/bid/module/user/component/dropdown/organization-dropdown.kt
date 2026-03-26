package org.solyton.solawi.bid.module.user.component.dropdown

import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.solyton.solawi.bid.module.user.data.organization.Organization
import org.solyton.solawi.bid.module.control.dropdown.DropdownStyles

fun generateOrganizationOptions(
    organizations: List<Organization>,
    path: String = "",
    accumulator: MutableMap<String, Organization> = mutableMapOf()
): Map<String, Organization> {
    if (organizations.isEmpty()) return accumulator
    val currentOrg = organizations.first()
    val updatedPath = if (path.isEmpty()) currentOrg.name else "$path/${currentOrg.name}"
    accumulator[updatedPath] = currentOrg

    // Recursive call for sub-organizations with updatedPath
    generateOrganizationOptions(
        currentOrg.subOrganizations,
        updatedPath,
        accumulator
    )

    // Recursive call for the remaining organizations with the original path
    return generateOrganizationOptions(
        organizations.drop(1),
        path,
        accumulator
    )
}

fun flatMap(organizations: List<Organization>, initMap: MutableMap<String, Organization> = mutableMapOf()): Map<String, Organization> {
    organizations.forEach { organization ->
        initMap[organization.organizationId] = organization
        flatMap(organization.subOrganizations, initMap)
    }
    return initMap
}

fun Map<String, String>.nameOfSelectedOrganization(organizationId: String): String? =
    entries.firstOrNull { (_, value) -> value == organizationId }?.let { (key, _) -> key}

fun Map<String, Organization>.nameOfSelectedOrganization(organizationId: String): String? =
    entries.firstOrNull { (_, value) -> value.organizationId == organizationId }?.let { (key, _) -> key}


val dropdownStyles = DropdownStyles.modifyContainerStyle {
    width(100.percent)
}
