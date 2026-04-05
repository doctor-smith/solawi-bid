package org.solyton.solawi.bid.module.banking.data.api

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.evoleq.ktorx.client.Parameters
import org.evoleq.ktorx.client.QueryParams
import org.solyton.solawi.bid.module.banking.data.CreditorId
import org.solyton.solawi.bid.module.banking.data.CreditorIdentifierId
import org.solyton.solawi.bid.module.values.LegalEntityId

typealias ApiCreditorIdentifier = CreditorIdentifier

@Serializable
data class CreditorIdentifiers(
    val all: List<CreditorIdentifier>
)

@Serializable
data class CreditorIdentifier(
    val creditorIdentifierId: CreditorIdentifierId,
    val legalEntityId: LegalEntityId,
    val creditorId: CreditorId,
    val validFrom: LocalDate,
    val validUntil: LocalDate?,
    val isActive: Boolean = true
)




@Serializable
data class ReadCreditorIdentifierByLegalEntity(
    /**
     * has param legal_entity: UUID
     */
    override val queryParams: QueryParams
) : Parameters()
