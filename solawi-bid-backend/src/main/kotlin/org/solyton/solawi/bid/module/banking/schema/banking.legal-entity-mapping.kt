package org.solyton.solawi.bid.module.banking.schema

import org.jetbrains.exposed.dao.id.UUIDTable
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable

object LegalEntityMappings : AuditableUUIDTable("legal_entity_mappings") {
    val legalEntityId = reference("legal_entity_id", LegalEntities).uniqueIndex()
    val legalEntityType = enumerationByName("legal_entity_type", 20, LegalEntityType::class)
    val partyId = uuid("party_id").uniqueIndex()
}


