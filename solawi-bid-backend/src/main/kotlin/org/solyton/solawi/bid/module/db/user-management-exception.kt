package org.solyton.solawi.bid.module.db

import java.util.*

sealed class UserManagementException (override val message: String) : Exception(message) {
    data class NoSuchChildOrganization(val id: UUID) : UserManagementException("No such child organization; id = $id")
}
