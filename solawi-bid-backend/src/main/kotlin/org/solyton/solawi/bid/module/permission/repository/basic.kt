package org.solyton.solawi.bid.module.permission.repository

import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.permission.exception.PermissionExceptionD
import org.solyton.solawi.bid.module.permission.schema.RightEntity
import org.solyton.solawi.bid.module.permission.schema.RightsTable
import org.solyton.solawi.bid.module.permission.schema.RoleEntity
import org.solyton.solawi.bid.module.permission.schema.RolesTable
import java.util.*

fun Transaction.createRole(name: String, description:String, creator: UUID): RoleEntity {
    val roleExists = !RoleEntity.find { RolesTable.name eq name }.empty()
    if(roleExists) throw PermissionExceptionD.DuplicateRoleName(name)

    return RoleEntity.new {
        createdBy = creator
        this.name = name
        this.description = description
    }
}

fun Transaction.createRight(name: String, description:String, creator: UUID): RightEntity {
    val rightExists = !RightEntity.find { RightsTable.name eq name }.empty()
    if(rightExists) throw PermissionExceptionD.DuplicateRightName(name)

    return RightEntity.new {
        createdBy = creator
        this.name = name
        this.description = description
    }
}
