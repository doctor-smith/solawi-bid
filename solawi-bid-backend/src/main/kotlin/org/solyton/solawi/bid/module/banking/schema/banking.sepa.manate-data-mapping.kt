package org.solyton.solawi.bid.module.banking.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.UUID

typealias SepaMandateDataMappingsTable = SepaMandateDataMappings
typealias SepaMandateDataMappingEntity = SepaMandateDataMapping

object SepaMandateDataMappings: AuditableUUIDTable("sepa_mandate_data_mappings") {
    val sepaMandateId = reference("sepa_mandate_id", SepaMandates)
    val referenceId = uuid("reference_id")
    val amount = double("amount")
}

class SepaMandateDataMapping(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<SepaMandateDataMapping>(SepaMandateDataMappings)

    var mandate by SepaMandate referencedOn SepaMandateDataMappings.sepaMandateId
    var referenceId by SepaMandateDataMappings.referenceId
    var amount by SepaMandateDataMappings.amount

    override var createdAt: DateTime by SepaMandateDataMappings.createdAt
    override var createdBy: UUID by SepaMandateDataMappings.createdBy
    override var modifiedAt: DateTime? by SepaMandateDataMappings.modifiedAt
    override var modifiedBy: UUID? by SepaMandateDataMappings.modifiedBy
}
