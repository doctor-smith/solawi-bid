package org.solyton.solawi.bid.module.banking.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias CreditorIdentifiersTable = CreditorIdentifiers
typealias CreditorIdentifierEntity = CreditorIdentifier

object CreditorIdentifiers : AuditableUUIDTable("creditor_identifiers") {
    val legalEntityId = reference("legal_entity_id", LegalEntities)

    // todo: dev validation: ISO 7064 Mod 97-10 für die Prüfziffer und nutzt ISO 3166-1 alpha-2 für country code
    val creditorId = varchar("creditor_id", 35) // z. B. DE98ZZZ09999999999

    val validFrom = datetime("valid_from")
    val validUntil = datetime("valid_until").nullable()
    val isActive = bool("is_active").default(true)
}

class CreditorIdentifier(id: EntityID<UUID>) : UUIDEntity(id),  AuditableEntity<UUID> {

    companion object : UUIDEntityClass<CreditorIdentifier>(CreditorIdentifiers)

    var legalEntity by LegalEntity referencedOn CreditorIdentifiers.legalEntityId
    var creditorId by CreditorIdentifiers.creditorId
    var validFrom by CreditorIdentifiers.validFrom
    var validUntil by CreditorIdentifiers.validUntil
    var isActive by CreditorIdentifiers.isActive

    override var createdAt: DateTime by CreditorIdentifiers.createdAt
    override var createdBy: UUID by CreditorIdentifiers.createdBy
    override var modifiedAt: DateTime? by CreditorIdentifiers.modifiedAt
    override var modifiedBy: UUID? by CreditorIdentifiers.modifiedBy
}
