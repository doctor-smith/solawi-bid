package org.solyton.solawi.bid.module.user.exception

sealed class AddressException(override val message: String): Exception(message) {
    data class NoSuchAddress(val id: String): AddressException("No such address $id")
}
