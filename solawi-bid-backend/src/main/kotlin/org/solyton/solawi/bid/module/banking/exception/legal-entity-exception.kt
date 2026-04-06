package org.solyton.solawi.bid.module.banking.exception

import org.evoleq.optics.transform.LensWriterException

sealed class LegalEntityException(override val message: String): Exception(message) {
    data class NoSuchLegalEntity(val id: String): LegalEntityException("No such legal entity: id = $id")
    data class NoSuchLegalEntityParty(val partyId: String): LegalEntityException("No such legal entity party: id = $partyId")

    data class NoAssociatedCreditorIdentifier(val legalEntity: String): LegalEntityException("Legal Entity with id = $legalEntity has no associated creditor id")
}
