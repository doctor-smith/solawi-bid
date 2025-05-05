package org.solyton.solawi.bid.module.db.repository

import org.solyton.solawi.bid.module.db.schema.OrganizationEntity
import org.solyton.solawi.bid.module.db.schema.UserEntity

fun UserEntity.organizations(): List<OrganizationEntity> = when{
    organizations.empty() -> listOf()
    else -> organizations.toList()
}