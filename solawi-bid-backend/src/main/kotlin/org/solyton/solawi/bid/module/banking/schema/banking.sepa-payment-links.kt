package org.solyton.solawi.bid.module.banking.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.UUID

typealias SepaPaymentLinksTable = SepaPaymentLinks
typealias SepaPaymentLinkEntity = SepaPaymentLink

object SepaPaymentLinks : AuditableUUIDTable("sepa_payment_links") {
    val predecessorId = reference("predecessor_id", SepaPayments, onDelete = ReferenceOption.CASCADE)
    val successorId   = reference("successor_id",   SepaPayments, onDelete = ReferenceOption.CASCADE)
    val kind          = enumerationByName("kind", 20, SuccessorKind::class)

    init {
        uniqueIndex(predecessorId, successorId)              // no duplicate edges
        uniqueIndex(successorId, kind)                       // at most one predecessor of each kind per parent
        uniqueIndex(predecessorId, kind)                     // at most one successor of each kind per parent
    }
}

class SepaPaymentLink(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<SepaPaymentLink>(SepaPaymentLinks)

    var predecessor by SepaPayment referencedOn SepaPaymentLinks.predecessorId
    var successor by SepaPayment referencedOn SepaPaymentLinks.successorId
    var kind by SepaPaymentLinks.kind

    override var createdAt: DateTime by SepaPaymentLinks.createdAt
    override var createdBy: UUID by SepaPaymentLinks.createdBy
    override var modifiedAt: DateTime? by SepaPaymentLinks.modifiedAt
    override var modifiedBy: UUID? by SepaPaymentLinks.modifiedBy
}

enum class SuccessorKind {
    NEXT_PERIOD,    // the regularly scheduled follow-up
    RETRY,          // re-execution after FAILED,
    MERGE,
    // CORRECTION,     // manual correction / adjustment
    // REPLACEMENT,    // replaces a cancelled message
}
