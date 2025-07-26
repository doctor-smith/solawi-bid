package org.solyton.solawi.bid.module.user.schema.repository

import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import org.solyton.solawi.bid.module.user.schema.UserEntity

fun UserEntity.organizations(): List<OrganizationEntity> = when{
    organizations.empty() -> listOf()
    else -> organizations.toList()
}
