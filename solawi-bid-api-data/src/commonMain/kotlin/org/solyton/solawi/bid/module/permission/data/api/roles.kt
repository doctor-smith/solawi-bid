package org.solyton.solawi.bid.module.permission.data.api


import kotlinx.serialization.Serializable
import org.solyton.solawi.bid.module.permission.data.RoleId
import org.solyton.solawi.bid.module.permission.data.RoleName
import org.solyton.solawi.bid.module.values.Description

@Serializable
data class CreateRoles(
    val rights: List<CreateRole>
)

@Serializable
data class CreateRole(
    val name: RoleName,
    val description: Description
)

@Serializable
data class UpdateRole(
    val roleId: RoleId,
    val name: RoleName,
    val description: Description
)

@Serializable
data class DeleteRole(
    val roleId: RoleId
)
