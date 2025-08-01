package org.solyton.solawi.bid.module.permission.exception

sealed class PermissionException(override val message: String): Exception(message) {
    data class NoSuchRole(val identifier: String): PermissionException("No such role: $identifier")
}
