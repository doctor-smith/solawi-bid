package org.solyton.solawi.bid.module.banking.exception

sealed class LegalEntityException(override val message: String): Exception(message) {
    data class NoSuchLegalEntity(val id: String): LegalEntityException("No such legal entity: id = $id")
}
