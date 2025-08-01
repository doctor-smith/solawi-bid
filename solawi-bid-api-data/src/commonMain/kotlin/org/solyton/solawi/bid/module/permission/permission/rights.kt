package org.solyton.solawi.bid.module.permission.permission

import org.evoleq.value.StringValueWithDescription

object ReadRightRoleContextsRight : StringValueWithDescription {
    override val value: String = "READ_ROLE_RIGHT_CONTEXTS"
    override val description: String = "Read role-right-contexts of users"
}

object ReadRightsAndRolesRight: StringValueWithDescription {
    override val value: String = "READ_ROLES_AND_RIGHTS"
    override val description: String = "Read roles and rights of users"
}

object Right {
    data object ReadRightRoleContexts : StringValueWithDescription by ReadRightRoleContextsRight
    data object ReadRightsAndRoles : StringValueWithDescription by ReadRightsAndRolesRight
}
