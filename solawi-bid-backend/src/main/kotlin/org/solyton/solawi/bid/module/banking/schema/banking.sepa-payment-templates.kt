package org.solyton.solawi.bid.module.banking.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.UUID

typealias SepaPaymentTemplatesTable = SepaPaymentTemplates
typealias SepaPaymentTemplateEntity = SepaPaymentTemplate

object SepaPaymentTemplates : AuditableUUIDTable("sepa_payment_templates", ) {
    val mandateId = reference("mandate_id", SepaMandates)
    val collectionId = optReference("collection_id", SepaCollections)
    val amount = double("amount")
    val initialSequenceType = enumerationByName("initial_sequence_type", 10, SepaSequenceType::class)
}

class SepaPaymentTemplate(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<SepaPaymentTemplate>(SepaPaymentTemplates)

    var mandate by SepaMandate referencedOn SepaPaymentTemplates.mandateId
    var collection by SepaCollection optionalReferencedOn SepaPaymentTemplates.collectionId
    var amount by SepaPaymentTemplates.amount
    var initialSequenceType by SepaPaymentTemplates.initialSequenceType

    val payments by SepaPayment optionalReferrersOn  SepaPayments.templateId

    override var createdAt: DateTime by SepaPaymentTemplates.createdAt
    override var createdBy: UUID by SepaPaymentTemplates.createdBy
    override var modifiedAt: DateTime? by SepaPaymentTemplates.modifiedAt
    override var modifiedBy: UUID? by SepaPaymentTemplates.modifiedBy
}

data class SepaMandateToSepaCollectionKey(val mandateId: UUID, val collectionId: UUID)
