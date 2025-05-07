package org.solyton.solawi.bid.module.permission

sealed class PermissionException(override val message: String) : Exception(message) {
    data object AccessDenied : PermissionException("Access denied")
    data class NoSuchContext(val context: String) : PermissionException("No such context: $context")

    data class NoSuchRight(val right: String) : PermissionException("No such right: $right")
}