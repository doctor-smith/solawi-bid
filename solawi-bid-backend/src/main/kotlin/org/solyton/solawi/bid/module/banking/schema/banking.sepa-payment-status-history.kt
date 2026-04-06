package org.solyton.solawi.bid.module.banking.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.UUID

enum class BankStatusCode {
    SUCCESS, FAILED, PENDING
}

object SepaPaymentStatusHistory : AuditableUUIDTable("sepa_payment_status_history") {
    val paymentId = reference("payment_id", SepaPayments)
    val status = enumerationByName("status", 20, BankStatusCode::class)
    val reasonCode = varchar("reason_code", 20).nullable() // e.g., InsufficientFunds
    val reasonText = text("reason_text").nullable()
    val reportedAt = datetime("reported_at") // Timestamp of bank message
}

class SepaPaymentStatusHistoryEntity(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<SepaPaymentStatusHistoryEntity>(SepaPaymentStatusHistory)

    var payment by SepaPayment referencedOn SepaPaymentStatusHistory.paymentId
    var status by SepaPaymentStatusHistory.status
    var reasonCode by SepaPaymentStatusHistory.reasonCode
    var reasonText by SepaPaymentStatusHistory.reasonText
    var reportedAt by SepaPaymentStatusHistory.reportedAt

    override var createdAt: DateTime by SepaPaymentStatusHistory.createdAt
    override var createdBy: UUID by SepaPaymentStatusHistory.createdBy
    override var modifiedAt: DateTime? by SepaPaymentStatusHistory.modifiedAt
    override var modifiedBy: UUID? by SepaPaymentStatusHistory.modifiedBy
}
