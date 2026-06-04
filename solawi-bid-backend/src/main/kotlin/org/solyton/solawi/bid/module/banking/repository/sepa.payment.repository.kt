package org.solyton.solawi.bid.module.banking.repository

import org.evoleq.exposedx.joda.now
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.solyton.solawi.bid.module.banking.data.internal.Pain008GenerationRequest
import org.solyton.solawi.bid.module.banking.data.internal.Pain008Transaction
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.exception.SepaException
import org.solyton.solawi.bid.module.banking.schema.*
import org.solyton.solawi.bid.module.banking.schema.SepaPaymentEntity
import org.solyton.solawi.bid.module.banking.service.generateE2ETransactionId
import org.solyton.solawi.bid.module.banking.service.generatePain008Xml
import org.solyton.solawi.bid.module.permission.action.db.no
import java.util.*

fun Transaction.createPaymentsForCollection(
    creator: UUID,
    sepaCollectionId: UUID,
    executionDate: LocalDate,
): List<SepaPayment> {
    val sepaCollection = validatedSepaCollection(sepaCollectionId)
    val mandateIds = sepaCollection.sepaMandates.map { it.id.value }

    val map: Map<UUID, Double> = SepaMandateDataMapping.find{
        SepaMandateDataMappings.sepaMandateId inList mandateIds
    }.groupBy({mapping -> mapping.mandate.id.value }) {
        mapping -> mapping.amount
    }.mapValues { it.value.sum() }

    return sepaCollection.sepaMandates.map { mandate ->
        val mandateId = mandate.id.value
        val amount = requireNotNull(map[mandateId]) {
            "Entry must not be null! You need to add a product reference to the mandate-data-mappings!"
        }
        createPayment(
            creator,
            mandateId,
            sepaCollectionId,
            amount,
            executionDate
        )
    }
}

fun Transaction.createPayment(
    creator: UUID,
    sepaMandateId: UUID,
    sepaCollectionId: UUID,
    amount: Double,
    executionDate: LocalDate,
): SepaPaymentEntity {
    val sepaMandate = validatedSepaMandate(sepaMandateId)
    val sepaCollection = validatedSepaCollection(sepaCollectionId)
    val defaultSequenceType = sepaCollection.sequenceType
    val relatedPayments = sepaMandate.payments
    val latestPayment = relatedPayments.maxByOrNull { payment -> payment.executionDate }
    val sequenceType = when{
        defaultSequenceType == SepaSequenceType.OOFF -> SepaSequenceType.OOFF
        latestPayment != null -> when(latestPayment.sequenceType){
            SepaSequenceType.RCUR -> SepaSequenceType.RCUR
            SepaSequenceType.FRST -> when(latestPayment.status) {
                PaymentExecutionStatus.CONFIRMED -> SepaSequenceType.RCUR
                PaymentExecutionStatus.PAYED_MANUALLY -> latestPayment.sequenceType
                PaymentExecutionStatus.FAILED -> SepaSequenceType.FRST
                else -> SepaSequenceType.UNCLEAR
            }
            SepaSequenceType.UNCLEAR -> SepaSequenceType.UNCLEAR
            SepaSequenceType.FNAL -> throw SepaException.Payment.CannotCreate("cannot rcur: latest payment was final")
            SepaSequenceType.OOFF -> throw SepaException.Payment.CannotCreate("cannot rcur after OOFF payment")
         }
        else -> SepaSequenceType.FRST
    }

    val payment = SepaPaymentEntity.new{
        createdBy = creator
        this.mandate = sepaMandate
        this.collection = sepaCollection
        this.status = PaymentExecutionStatus.CREATED
        this.sequenceType = sequenceType
        this.amount = amount
        this.executionDate = executionDate.toDateTimeAtCurrentTime()
    }

    // create history entry
    SepaPaymentStatusHistoryEntity.new {
        createdBy = creator
        reportedAt = now()
        this.payment = payment
        status = BankStatusCode.NOT_SUBMITTED
    }

    return payment
}

/**
 * Update payment.
 * Some properties of Payments might be updated as long as they have not been submitted to the bank:
 *  - amount
 *  - sequenceType
 *  - execution date
 *
 * Payment status can be updated. History will be updated as well
 *
 */
@Suppress("CyclomaticComplexMethod", "CognitiveComplexMethod")
fun Transaction.updatePayment(
    modifier: UUID,
    sepaPaymentId: UUID,
    amount: Double,
    executionDate: LocalDate,
    sequenceType: SepaSequenceType,
    status: PaymentExecutionStatus,
    changeReportedAt: DateTime?,
    failureReason: String?,
    endToEndId: String? = null
): SepaPaymentEntity {
    val payment = validatedPayment(sepaPaymentId)

    val amountChanged = payment.amount != amount
    val executionDateChanged = payment.executionDate != executionDate
    val sequenceTypeChanged = payment.sequenceType != sequenceType
    val statusChanged = payment.status != status
    val endToEndIdChanged = endToEndId != null && payment.endToEndId != endToEndId

    val changesAllowed = when{
        payment.status == PaymentExecutionStatus.CREATED -> true
        else -> when{
            amountChanged -> false
            executionDateChanged -> false
            statusChanged -> true
            endToEndIdChanged -> true
            else -> false
        }
    }
    if(!changesAllowed) throw SepaException.Payment.ChangesNotAllowed("Payment Processing. Only status changes are possible")

    if(amountChanged) payment.amount = amount
    if(executionDateChanged) payment.executionDate = executionDate.toDateTimeAtCurrentTime()
    if(sequenceTypeChanged) payment.sequenceType = sequenceType
    if(statusChanged) payment.status = status
    if(endToEndIdChanged) payment.endToEndId = endToEndId

    val changed = amountChanged
            || executionDateChanged
            || sequenceTypeChanged
            || statusChanged
            || endToEndIdChanged

    if(changed) {
        payment.modifiedBy = modifier
        payment.modifiedAt = now()
    }

    // Update history
    if(statusChanged) {
        if(changeReportedAt == null) throw SepaException.Payment.ChangeRequiresDateOfReport

        val statusChange = "${payment.status.name} -> ${status.name}"

        val (newBankingStatus, code, reason) = when(status) {
            PaymentExecutionStatus.MESSAGE_CREATED -> Triple(BankStatusCode.PENDING, "MESSAGE_CREATED", "Sepa Message created for Payment")
            PaymentExecutionStatus.SENT -> Triple(BankStatusCode.PENDING, "SENT", "Payment request sent")
            PaymentExecutionStatus.PENDING -> Triple(BankStatusCode.PENDING, "PENDING",statusChange )
            PaymentExecutionStatus.FAILED -> Triple(BankStatusCode.FAILED, "FAILED", failureReason?: "Reason unknown")
            PaymentExecutionStatus.CONFIRMED -> Triple(BankStatusCode.SUCCESS, "SUCCESS", statusChange)
            PaymentExecutionStatus.PAYED_MANUALLY -> Triple(BankStatusCode.SUCCESS, "SUCCESS", statusChange)
            PaymentExecutionStatus.CREATED -> throw SepaException.Payment.StateTransitionForbidden(
                payment.status.name,
                status.name
            )
        }

        SepaPaymentStatusHistoryEntity.new {
            createdBy = modifier
            this.payment = payment
            this.status = newBankingStatus
            this.reasonCode = code
            this.reasonText = reason
            this.reportedAt = changeReportedAt
        }
    }

    return payment
}


fun Transaction.generateSepaMessageForCollection(
    creator: UUID,
    collectionId: UUID,
    executionDate: LocalDate?
): String {

    val reportedAt = now()

    val collection = validatedSepaCollection(collectionId)

    val creditorIdentifier = collection.creditorIdentifier

    val createdPayments = collection.sepaPayments.filter {
        it.status == PaymentExecutionStatus.CREATED
    }

    require(createdPayments.isNotEmpty())
    if(executionDate == null) require(createdPayments.map { it.executionDate }.distinct().size == 1)
    val finalExecutionDate = when(executionDate){
        null -> createdPayments.first().executionDate.toLocalDate()
        else -> executionDate.toDateTimeAtCurrentTime().toLocalDate()
    }

    val finalPayments = createdPayments.filter { it.executionDate.toLocalDate() == finalExecutionDate }

    val paymentWithE2E = finalPayments.map { it to generateE2ETransactionId() }

    val transactions: List<Pain008Transaction> = paymentWithE2E.map { (payment, e2eId) ->
        val mandate = payment.mandate
        val sequenceType = payment.sequenceType
        Pain008Transaction(
            e2eId,
            payment.amount.toBigDecimal(),
            mandate.debtorName.let{ when{
                it.isBlank() -> mandate.debtorBankAccount.accountHolder
                else -> it
            } },
            mandate.debtorBankAccount.iban,
            mandate.debtorBankAccount.bic,
            mandate.mandateReference,
            mandate.signedAt.toLocalDate(),
            collection.remittanceInformation,
            sequenceType
        )
    }

    val pain008XmlString = generatePain008Xml(Pain008GenerationRequest(
        creditorIdentifier.id.value,
        collection.creditorAccount.id.value,
        finalExecutionDate,
        transactions,
        creator
    ))
    // update payments
    paymentWithE2E.forEach { (payment, e2eId) ->
        updatePayment(
            creator,
            payment.id.value,
            payment.amount,
            payment.executionDate.toLocalDate(),
            payment.sequenceType,
            PaymentExecutionStatus.MESSAGE_CREATED,
            reportedAt,
            null,
            e2eId
        )
    }

    return pain008XmlString
}

fun Transaction.updateSepaPaymentExecutionStatuses(
    modifier: UUID,
    paymentIds: List<UUID>,
    newStatus: PaymentExecutionStatus,
    failureReasons: Map<UUID, String> = emptyMap()
): List<SepaPaymentEntity> {
    if(newStatus == PaymentExecutionStatus.FAILED) {
        // there must be a failure reason
        require(failureReasons.isNotEmpty()) { "Failure reason must be provided for failed payments" }
        // all payments must be failed
        require(paymentIds.size == failureReasons.size) { "All transferred payments must be failed" }
        // all payments must be created
        require(paymentIds.all {
            SepaPaymentEntity.findById(it)?.status in listOf(
                PaymentExecutionStatus.PENDING,
                PaymentExecutionStatus.CONFIRMED,
                PaymentExecutionStatus.PAYED_MANUALLY
            )
        }) { "All payments must be pending, confirmed or payed-manually in order to be set to failed" }

        failureReasons.forEach { (paymentId, reason) ->
            SepaPaymentsTable.update({ SepaPayments.id eq paymentId }) {
                it[status] = newStatus
                it[failureReason] = reason
                it[modifiedBy] = modifier
                it[modifiedAt] = now()
            }

            addHistoryEntry(
                modifier,
                paymentId,
                newStatus,
                reason,
                now()
            )
        }
    }
    else {
        SepaPaymentsTable.update({ SepaPayments.id inList paymentIds }) {
            it[status] = newStatus
            it[modifiedBy] = modifier
            it[modifiedAt] = now()
        }

        paymentIds.forEach {
            addHistoryEntry(
                modifier,
                it,
                newStatus,
                failureReasons[it],
                now()
            )
        }
    }

    return SepaPaymentEntity.find { SepaPayments.id inList paymentIds }.toList()
}

fun Transaction.addHistoryEntry(
    modifier: UUID,
    paymentId: UUID,
    status: PaymentExecutionStatus,
    reasonText: String?,
    reportedAt: DateTime
): SepaPaymentStatusHistoryEntity {
    val payment = validatedPayment(paymentId)
    // if(changeReportedAt == null) throw SepaException.Payment.ChangeRequiresDateOfReport

    val statusChange = "${payment.status.name} -> ${status.name}"

    val (newBankingStatus, code, reason) = when(status) {
        PaymentExecutionStatus.MESSAGE_CREATED -> Triple(BankStatusCode.PENDING, "MESSAGE_CREATED", "Sepa Message created for Payment")
        PaymentExecutionStatus.SENT -> Triple(BankStatusCode.PENDING, "SENT", "Payment request sent")
        PaymentExecutionStatus.PENDING -> Triple(BankStatusCode.PENDING, "PENDING",statusChange )
        PaymentExecutionStatus.FAILED -> Triple(BankStatusCode.FAILED, "FAILED", reasonText?: "Reason unknown")
        PaymentExecutionStatus.CONFIRMED -> Triple(BankStatusCode.SUCCESS, "SUCCESS", statusChange)
        PaymentExecutionStatus.PAYED_MANUALLY -> Triple(BankStatusCode.SUCCESS, "SUCCESS", statusChange)
        PaymentExecutionStatus.CREATED -> throw SepaException.Payment.StateTransitionForbidden(
            payment.status.name,
            status.name
        )
    }

    return SepaPaymentStatusHistoryEntity.new {
        createdBy = modifier
        this.payment = payment
        this.status = newBankingStatus
        this.reasonCode = code
        this.reasonText = reason
        this.reportedAt = reportedAt
    }
}


fun Transaction.validatedPayment(id: UUID): SepaPaymentEntity = SepaPaymentEntity.findById(id)
    ?: throw SepaException.Payment.NoSuchPayment(id.toString())

// they come with the collections
// fun Transaction.readPayments()
