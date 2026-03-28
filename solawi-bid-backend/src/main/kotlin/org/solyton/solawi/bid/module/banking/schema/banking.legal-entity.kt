package org.solyton.solawi.bid.module.banking.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import org.solyton.solawi.bid.module.user.schema.Address
import org.solyton.solawi.bid.module.user.schema.AddressesTable
import java.util.*

typealias LegalEntitiesTable = LegalEntities
typealias LegalEntityEntity = LegalEntity

object LegalEntities : AuditableUUIDTable("legal_entities") {
    val name = varchar("name", 255)

    // Person, Foundation, Company, Gmbh, etc
    val legalForm = varchar("legal_form", 50).nullable()
    // Human or Organization
    val legalEntityType = enumerationByName("legal_entity_type", 20, LegalEntityType::class)
    // Who
    val partyId = uuid("party_id").uniqueIndex()

    val address = reference("address_id", AddressesTable)
}

class LegalEntity(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<LegalEntity>(LegalEntities)

    var name by LegalEntities.name
    var legalForm by LegalEntities.legalForm
    var address by Address referencedOn LegalEntities.address
    var legalEntityType by LegalEntities.legalEntityType
    var partyId by LegalEntities.partyId

    override var createdAt: DateTime by LegalEntities.createdAt
    override var createdBy: UUID by LegalEntities.createdBy
    override var modifiedAt: DateTime? by LegalEntities.modifiedAt
    override var modifiedBy: UUID? by LegalEntities.modifiedBy
}

enum class LegalEntityType {
    HUMAN,
    ORGANIZATION
}
