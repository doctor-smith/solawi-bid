package org.solyton.solawi.bid.module.permission.exception

sealed class PermissionExceptionD(override val message: String): Exception(message) {
    data class NoSuchRole(val identifier: String): PermissionExceptionD("No such role: $identifier")
    data class DuplicateRoleName(val name: String): PermissionExceptionD("Duplicate role-name $name")
    data class DuplicateRightName(val name: String): PermissionExceptionD("Duplicate right-name $name")
}
