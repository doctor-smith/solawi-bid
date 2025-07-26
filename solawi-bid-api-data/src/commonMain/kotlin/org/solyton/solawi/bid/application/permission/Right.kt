package org.solyton.solawi.bid.application.permission

import org.evoleq.permission.Create
import org.evoleq.permission.Delete
import org.evoleq.permission.Read
import org.evoleq.permission.Update
import org.evoleq.value.StringValueWithDescription
import org.solyton.solawi.bid.module.permission.permission.ReadRightRoleContextsRight
import org.solyton.solawi.bid.module.permission.permission.ReadRightsAndRolesRight

// typealias Right = ValueWithDescription

object Right{
    val create = Create
    val read = Read
    val update = Update
    val delete = Delete

    data object ReadRightRoleContexts : StringValueWithDescription by ReadRightRoleContextsRight

    data object ReadRightsAndRoles : StringValueWithDescription by ReadRightsAndRolesRight





}
