package org.solyton.solawi.bid.module.user.data

import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.permission.action.db.getRolesByUserAndContext
import org.solyton.solawi.bid.module.permission.data.api.ApiRole
import org.solyton.solawi.bid.module.user.data.api.UserD
import org.solyton.solawi.bid.module.user.data.api.organization.ApiMember
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganization
import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import org.solyton.solawi.bid.module.user.schema.repository.getChildren
import org.solyton.solawi.bid.module.user.schema.User as UserEntity

fun UserEntity.toApiType(): UserD = UserD(
    id.value,
    username,
    password
)

fun OrganizationEntity.toApiType(transaction: Transaction): ApiOrganization = ApiOrganization(
    id = id.value.toString(),
    name = name,
    contextId = context.id.value.toString(),
    members = members.map {
        user -> ApiMember(
            user.id.value.toString(),
            with(transaction) {
                getRolesByUserAndContext(user.id.value, context.id.value)
            }.map { role -> ApiRole(
                role.id.value.toString(),
                name = role.name,
                description = role.description,
                rights = listOf()
            ) }
        )
    },
    subOrganizations = getChildren().map {
        organization -> organization.toApiType(transaction)
    }
)
