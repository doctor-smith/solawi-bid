package org.solyton.solawi.bid.module.user.schema.repository

import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UsersTable

fun UserEntity.organizations(): List<OrganizationEntity> = when{
    organizations.empty() -> listOf()
    else -> organizations.toList()
}

fun Transaction.readUserByUsername(username: String): UserEntity? =
    UserEntity.find { UsersTable.username eq username }.firstOrNull()
