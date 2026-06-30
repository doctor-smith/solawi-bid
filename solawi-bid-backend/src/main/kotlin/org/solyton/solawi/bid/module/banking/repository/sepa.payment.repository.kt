package org.solyton.solawi.bid.module.banking.repository

import org.evoleq.exposedx.joda.now
import org.jetbrains.exposed.dao.flushCache
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.solyton.solawi.bid.module.banking.data.SepaMessageId
import org.solyton.solawi.bid.module.banking.data.internal.Pain008GenerationRequest
import org.solyton.solawi.bid.module.banking.data.internal.Pain008Transaction
import org.solyton.solawi.bid.module.banking.exception.SepaException
import org.solyton.solawi.bid.module.banking.schema.*
import org.solyton.solawi.bid.module.banking.service.SepaMessageGenerationResult
import org.solyton.solawi.bid.module.banking.service.generateE2ETransactionId
import org.solyton.solawi.bid.module.banking.service.generatePain008Xml
import java.util.*

/**
 * Create payments for the next period of a collection.
 * Only active mandates are considered.
 * Recurring payments are created in the same state as the original payments. And only if the original
 * payments are in one of the following states: CONFIRMED, PAYED_MANUALLY, FAILED and have no accessors yet.
 * Completely New payments are created for those mandates that have no payments yet.
 * @param creator The user who created the payments.
 * @param sepaCollectionId The ID of the SEPA collection for which payments are being created.
 * @param executionDate The date on which the payments will be executed.
 * @param mandateIds If provided, only the mandates with these IDs will be considered.
 * @return A list of created SepaPayment objects.
 */
fun Transaction.createPaymentsForCollection(
    creator: UUID,
    sepaCollectionId: UUID,
    executionDate: LocalDate,
    mandateIds: List<UUID>? = null,
): List<SepaPayment> {

    val sepaCollection = validatedSepaCollection(sepaCollectionId)
    // here we take all mandates that are active and, if the provided list of mandates is not null, we filter by it
    val activeMandates = sepaCollection.sepaMandates.filter{
        it.status == MandateStatus.ACTIVE && when(mandateIds){
            null -> true
            else -> mandateIds.contains(it.id.value)
        }
    }
    val activeMandateIds = activeMandates.map { it.id.value }

    val map: Map<UUID, Double> = SepaMandateDataMapping.find{
        SepaMandateDataMappings.sepaMandateId inList activeMandateIds
    }.groupBy({mapping -> mapping.mandate.id.value }) {
        mapping -> mapping.amount
    }.mapValues { it.value.sum() }

    val allowedStatuses = listOf(
        PaymentExecutionStatus.CONFIRMED,
        PaymentExecutionStatus.PAYED_MANUALLY,
        PaymentExecutionStatus.FAILED
    )
    return activeMandates.flatMap {  mandate ->
        // Note that there are no payments at all if there are only payments with associated successors.
        val relevantPayments = mandate.payments.filter {
            it.nextPeriodSuccessor == null
        }
        when{
            relevantPayments.isNotEmpty() ->
                relevantPayments.filter{ it.status in allowedStatuses }
                    .map { payment -> createSuccessorOfPayment(
                        creator,
                        executionDate,
                        payment,
                        SuccessorKind.NEXT_PERIOD
                    )}

            else ->  {
                val mandateId = mandate.id.value
                val amount = requireNotNull(map[mandateId]) {
                    "Entry must not be null! You need to add a product reference to the mandate-data-mappings!"
                }

                listOf(createPayment(
                    creator,
                    mandateId,
                    sepaCollectionId,
                    amount,
                    executionDate,
                    ignoreOldPayments = true
                ))
            }
        }
    }
}

/**
 * Create a payment for a given mandate and a given collection.
 * Only active mandates are considered.
 * If the pair (mandate, collection) has no associated payment-template yet, a new payment-template is created.
 */
fun Transaction.createPayment(
    creator: UUID,
    sepaMandateId: UUID,
    sepaCollectionId: UUID,
    amount: Double,
    executionDate: LocalDate,
    ignoreOldPayments: Boolean = false,
): SepaPaymentEntity {
    val sepaMandate = validatedSepaMandate(sepaMandateId)
    if(sepaMandate.status != MandateStatus.ACTIVE) throw SepaException.Payment.CannotCreate( "Mandate must be active" )

    val sepaCollection = validatedSepaCollection(sepaCollectionId)
    val defaultSequenceType = sepaCollection.sequenceType
    val template = readSepaPaymentTemplateByMandateAndCollection(sepaMandateId, sepaCollectionId)?: createSepaPaymentTemplate(
        creator, sepaMandate, amount, sepaCollection
    )

    val relatedPayments = sepaMandate.payments
    val latestPayment = if(ignoreOldPayments) null else relatedPayments.maxByOrNull { payment -> payment.executionDate }
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
        this.template = template
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
 * Create an ad-hoc payment for a given mandate and a given collection.
 */
fun Transaction.createAdHocPayment(
    creator: UUID,
    sepaMandateId: UUID,
    sepaCollectionId: UUID,
    amount: Double,
    executionDate: LocalDate,
    predecessorId: UUID? = null,
): SepaPaymentEntity {
    val ignoreOldPayments = predecessorId != null
    val newPayment = createPayment(
        creator,
        sepaMandateId,
        sepaCollectionId,
        amount,
        executionDate,
        ignoreOldPayments
    )

    if(predecessorId != null) {
        val predecessor = validatedPayment(predecessorId)
        SepaPaymentLinkEntity.new {
            createdBy = creator
            this.predecessor = predecessor
            this.successor = newPayment
            kind = SuccessorKind.AD_HOC
        }
    }

    return newPayment
}

/**
 * Update a payment for a given mandate and a given collection.
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
    endToEndId: String? = null,
    messageId: SepaMessageId? = null,
): SepaPaymentEntity {
    val message = messageId?.let {
        val uuid = UUID.fromString(it.value)
        validateSepaMessage(uuid)
    }

    return updatePayment(
        modifier,
        sepaPaymentId,
        amount,
        executionDate,
        sequenceType,
        status,
        changeReportedAt,
        failureReason,
        endToEndId,
        message
    )
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
    endToEndId: String? = null,
    message: SepaMessage? = null,
): SepaPaymentEntity {
    val payment = validatedPayment(sepaPaymentId)

    val amountChanged = payment.amount != amount
    val executionDateChanged = payment.executionDate != executionDate
    val sequenceTypeChanged = payment.sequenceType != sequenceType
    val statusChanged = payment.status != status
    val endToEndIdChanged = endToEndId != null && payment.endToEndId != endToEndId
    val messageChanged = message != null && payment.message != message

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
    if(messageChanged) payment.message = message

    val changed = amountChanged
            || executionDateChanged
            || sequenceTypeChanged
            || statusChanged
            || endToEndIdChanged
            || messageChanged

    if(changed) {
        payment.modifiedBy = modifier
        payment.modifiedAt = now()
    }

    // update message
    if(statusChanged) {
        require(status == PaymentExecutionStatus.CREATED || message != null) { "Message must be provided if status is changed" }
        when(status) {
            PaymentExecutionStatus.MESSAGE_CREATED -> payment.message?.status = SepaMessageStatus.CREATED
            PaymentExecutionStatus.SENT -> payment.message?.status = SepaMessageStatus.SENT
            PaymentExecutionStatus.PENDING -> payment.message?.status = SepaMessageStatus.PENDING
            PaymentExecutionStatus.FAILED -> payment.message?.status = SepaMessageStatus.FAILED
            PaymentExecutionStatus.CONFIRMED -> payment.message?.status = SepaMessageStatus.CONFIRMED
            else -> Unit
        }
    }

    if(amountChanged && payment.message != null) {
        message?.totalAmount = message.payments.sumOf { it.amount }
    }

    // Update history
    if(statusChanged) {
        if(changeReportedAt == null) throw SepaException.Payment.ChangeRequiresDateOfReport

        val statusChange = "${payment.status.name} -> ${status.name}"

        val (newBankingStatus, code, reason) = when(status) {
            PaymentExecutionStatus.MESSAGE_CREATED -> Triple(BankStatusCode.PENDING, "MESSAGE_CREATED", "Sepa Message created for Payment")
            PaymentExecutionStatus.SENT -> Triple(BankStatusCode.PENDING, "SENT", "Payment request sent")
            PaymentExecutionStatus.PENDING -> Triple(BankStatusCode.PENDING, "PENDING",statusChange )
            PaymentExecutionStatus.FAILED, PaymentExecutionStatus.DROPPED -> Triple(BankStatusCode.FAILED, "FAILED", failureReason?: "Reason unknown")
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

/**
 * Create successors of payments.
 * The successors are created in the same state as the original payments.
 * All payments must be related to an active mandate.
 * All payments must be in one of the following states: CONFIRMED, PAYED_MANUALLY, FAILED
 */
fun Transaction.createSuccessorsOfPayments(
    creator: UUID,
    executionDate: LocalDate,
    kind: SuccessorKind,
    paymentIds: List<UUID>
): List<SepaPaymentEntity> {
    val payments = SepaPaymentEntity.find {
        SepaPayments.id inList paymentIds
    }.toList()

    require(payments.size == paymentIds.size) { "All payments must be found" }
    require(payments.none {
        when(kind) {
            SuccessorKind.NEXT_PERIOD -> it.nextPeriodSuccessor != null
            SuccessorKind.RETRY -> it.retrySuccessor != null
            SuccessorKind.MERGE -> it.mergeSuccessor != null
            SuccessorKind.AD_HOC -> false
        }
    }) {
        "All payments must not have successors of the desired kind"
    }

    val allowedStatuses = listOf(
        PaymentExecutionStatus.CONFIRMED,
        PaymentExecutionStatus.PAYED_MANUALLY,
        PaymentExecutionStatus.FAILED
    )
    require(payments.all { it.status in allowedStatuses }) { "All payments must be in one of the following states: ${allowedStatuses.joinToString()}" }
    require(payments.all { it.mandate.status == MandateStatus.ACTIVE }) { "All payments need to be related to an active mandate" }

    return payments.map { payment ->
        createSuccessorOfPayment(creator, executionDate, payment, kind)
    }
}

/**
 * Create a successor of a payment.
 * Associated mandate must be active.
 * Payment must be in one of the following states: CONFIRMED, PAYED_MANUALLY, FAILED
 */
fun Transaction.createSuccessorOfPayment(
    creator: UUID,
    executionDate: LocalDate,
    payment: SepaPaymentEntity,
    kind: SuccessorKind
): SepaPaymentEntity {
    if(payment.mandate.status != MandateStatus.ACTIVE) throw SepaException.Payment.CannotCreate("Mandate must be active")
    val allowedStatuses = listOf(
        PaymentExecutionStatus.CONFIRMED,
        PaymentExecutionStatus.PAYED_MANUALLY,
        PaymentExecutionStatus.FAILED
    )
    if(payment.status !in allowedStatuses) throw SepaException.Payment.CannotCreate(
        "Payment must be in one of the following states: ${allowedStatuses.joinToString(", ")}"
    )
    // create template if necessary
    val template = payment.template?: createSepaPaymentTemplate(
        creator,
        payment.mandate,
        payment.amount,
        payment.collection
    )
    if(payment.template == null) payment.template = template

    val successor = SepaPaymentEntity.new {
        this.createdBy = creator
        this.createdAt = now()
        this.mandate = payment.mandate
        this.collection = payment.collection
        this.status = PaymentExecutionStatus.CREATED
        this.sequenceType = shiftSequenceType(payment)
        this.amount = template.amount
        this.executionDate = executionDate.toDateTimeAtCurrentTime()
        this.template = template
    }

    SepaPaymentLinkEntity.new {
        this.createdBy = creator
        this.createdAt = now()
        this.predecessor = payment
        this.successor = successor
        this.kind = kind
    }

    payment.modifiedBy = creator
    payment.modifiedAt = now()

    // create history entry
    SepaPaymentStatusHistoryEntity.new {
        createdBy = creator
        reportedAt = now()
        this.payment = successor
        status = BankStatusCode.NOT_SUBMITTED
    }

    return successor
}

/**
 * Create a SEPA payment template.
 */
fun Transaction.createSepaPaymentTemplate(
    creator: UUID,
    mandate: SepaMandateEntity,
    amount: Double,
    collection: SepaCollectionEntity,
): SepaPaymentTemplateEntity {
    val template = SepaPaymentTemplateEntity.new {
        this.createdBy = creator
        this.createdAt = now()
        this.mandate = mandate
        this.amount = amount
        this.collection = collection
        this.initialSequenceType = initialSequenceType(collection.sequenceType)
    }
    return template
}

/**
 * Read a SEPA payment template by mandate and collection.
 * The template is not expected to be already persisted.
 * But if it is there, it is uniquely identified by the mandate and collection.
 */
fun Transaction.readSepaPaymentTemplateByMandateAndCollection(
    mandateId: UUID, collectionId: UUID
) : SepaPaymentTemplateEntity? = SepaPaymentTemplateEntity.find {
        (SepaPaymentTemplates.mandateId eq  mandateId) and
        (SepaPaymentTemplates.collectionId eq collectionId)
    }.firstOrNull()

/**
 * Updates the SEPA payment template with new values if any of the provided parameters differ from the existing template.
 * Also updates the `modifiedBy` and `modifiedAt` fields of the template if changes are made.
 *
 * @param modifierId the UUID of the user or system making the modifications
 * @param templateId the UUID of the SEPA payment template to be updated
 * @param sepaMandateId the UUID of the SEPA mandate to associate with the template
 * @param sepaCollectionId the UUID of the SEPA collection to associate with the template
 * @param amount the new amount to be set in the template
 * @param initialSequenceType the initial SEPA sequence type to be set in the template
 * @return the updated SEPA payment template entity
 */
fun Transaction.updateSepaPaymentTemplate(
    modifierId: UUID,
    templateId: UUID,
    sepaMandateId: UUID,
    sepaCollectionId: UUID,
    amount: Double,
    initialSequenceType: SepaSequenceType,
): SepaPaymentTemplateEntity {
    val template = validatedSepaPaymentTemplate(templateId)
    val mandate = validatedSepaMandate(sepaMandateId)
    val collection = validatedSepaCollection(sepaCollectionId)

    val mandateChanged = template.mandate.id.value != sepaMandateId
    val collectionChanged = template.collection?.id?.value != sepaCollectionId
    val amountChanged = template.amount != amount
    val initialSequenceTypeChanged = template.initialSequenceType != initialSequenceType

    if(mandateChanged) template.mandate = mandate
    if(collectionChanged) template.collection = collection
    if(amountChanged) template.amount = amount
    if(initialSequenceTypeChanged) template.initialSequenceType = initialSequenceType

    val changed = mandateChanged ||
            collectionChanged ||
            amountChanged ||
            initialSequenceTypeChanged

    if (changed) {
        template.modifiedBy = modifierId
        template.modifiedAt = now()
    }
    return template
}

fun Transaction.validatedSepaPaymentTemplate(templateId: UUID): SepaPaymentTemplateEntity = SepaPaymentTemplateEntity.find {
    SepaPaymentTemplates.id eq templateId
}.firstOrNull()?: throw SepaException.Payment.NoSuchTemplate(templateId.toString())

/**
 * Get the initial [SepaSequenceType] from a given one.
 */
fun initialSequenceType(sequenceType: SepaSequenceType): SepaSequenceType = when(sequenceType) {
    SepaSequenceType.FRST -> SepaSequenceType.FRST
    SepaSequenceType.RCUR -> SepaSequenceType.FRST
    SepaSequenceType.FNAL -> SepaSequenceType.FRST
    SepaSequenceType.OOFF -> SepaSequenceType.OOFF
    SepaSequenceType.UNCLEAR -> throw SepaException.Payment.CannotCreate("cannot create template for unclear collection")
}

/**
 * Get the next [SepaSequenceType] of a payment.
 * The new sequence type is determined by the current status of the payment.
 */
fun shiftSequenceType(payment: SepaPaymentEntity): SepaSequenceType {
    val newSequenceType = when (payment.sequenceType) {
        SepaSequenceType.FRST -> when(payment.status) {
            PaymentExecutionStatus.CONFIRMED -> SepaSequenceType.RCUR
            else -> SepaSequenceType.FRST
        }
        else -> payment.sequenceType
    }
    return newSequenceType
}

fun Transaction.generateSepaMessageForCollection(
    creator: UUID,
    collectionId: UUID,
    executionDate: LocalDate?,
    sepaPaymentIds: List<UUID>? = null,
    remittanceInformation: String? = null,
): SepaMessageGenerationResult {

    val reportedAt = now()

    val collection = validatedSepaCollection(collectionId)

    val creditorIdentifier = collection.creditorIdentifier

    val createdPayments = collection.sepaPayments.filter {
        it.status == PaymentExecutionStatus.CREATED &&
        sepaPaymentIds?.contains(it.id.value) ?: true
    }

    require(createdPayments.isNotEmpty()){ "No created payments found"}
    if(sepaPaymentIds != null) require(createdPayments.map { it.id.value }.distinct().size == sepaPaymentIds.size){ "All created payments must be found"}
    if(executionDate == null) require(createdPayments.map { it.executionDate }.distinct().size == 1)

    val finalExecutionDate = when(executionDate){
        null -> createdPayments.first().executionDate.toLocalDate()
        else -> executionDate.toDateTimeAtCurrentTime().toLocalDate()
    }

    val finalPayments = createdPayments.filter { it.executionDate.toLocalDate() == finalExecutionDate }

    val paymentWithE2E = finalPayments.map { it to generateE2ETransactionId() }

    val finalRemittanceInformation = remittanceInformation ?: collection.remittanceInformation

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
            finalRemittanceInformation,
            sequenceType
        )
    }

    val pain008XmlResult = generatePain008Xml(Pain008GenerationRequest(
        creditorIdentifier.id.value,
        collection.creditorAccount.id.value,
        finalExecutionDate,
        transactions,
        creator
    ))
    // Flush so that the newly created SepaMessage is persisted before
    // the payments are updated to reference it (otherwise the FK
    // constraint SEPA_PAYMENTS.MESSAGE_ID -> SEPA_MESSAGES.ID fails,
    // because the entity cache may flush the payment updates before
    // the message insert).
    flushCache()
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
            e2eId,
            pain008XmlResult.message
        )
    }

    return pain008XmlResult
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
    else if(newStatus == PaymentExecutionStatus.DROPPED) {
        require(paymentIds.all {
            SepaPaymentEntity.findById(it)?.status == PaymentExecutionStatus.FAILED
        }) { "All payments must be failed in order to be set to dropped" }

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

/**
 * Read all payments for a given legal entity.
 */
fun Transaction.readPaymentsByLegalEntity(legalEntityId: UUID): List<SepaPaymentEntity> {
    val query = SepaPaymentsTable.join(
        otherTable = SepaMandatesTable, 
        onColumn = SepaPaymentsTable.mandateId,
        otherColumn = SepaMandatesTable.id,
        joinType = JoinType.INNER
    ).join(
        otherTable = BankAccountsTable, 
        onColumn = SepaMandatesTable.debtorBankAccountId,
        otherColumn = BankAccountsTable.id,
        joinType = JoinType.INNER
    ).join(
        otherTable = BankAccountAccessorsTable, 
        onColumn = BankAccountsTable.id,
        otherColumn = BankAccountAccessorsTable.bankAccountId,
        joinType = JoinType.INNER
    ).select(SepaPaymentsTable.columns).where {
        BankAccountAccessorsTable.accessorId eq legalEntityId
    }

    return SepaPaymentEntity.wrapRows(query).toList()
}

/**
 * Read all payments for a given user.
 */
fun Transaction.readPaymentsByUser(userId: UUID): List<SepaPaymentEntity> {
    val query = SepaPaymentsTable.join(
        otherTable = SepaMandatesTable,
        onColumn = SepaPaymentsTable.mandateId,
        otherColumn = SepaMandatesTable.id,
        joinType = JoinType.INNER
    ).join(
        otherTable = BankAccountsTable,
        onColumn = SepaMandatesTable.debtorBankAccountId,
        otherColumn = BankAccountsTable.id,
        joinType = JoinType.INNER
    ).select(SepaPaymentsTable.columns).where {
        BankAccountsTable.userId eq userId
    }

    return SepaPaymentEntity.wrapRows(query).toList()
}

fun Transaction.readSepaPaymentLinksByLegalEntity(legalEntityId: UUID): List<SepaPaymentLinkEntity> {

    val payments = readPaymentsByLegalEntity(legalEntityId).map { it.id.value }
    return SepaPaymentLinkEntity.find {
        SepaPaymentLinks.successorId inList payments and (SepaPaymentLinks.predecessorId inList payments)
    }.toList()
}

fun Transaction.readSepaPaymentLinksByUser(userId: UUID): List<SepaPaymentLinkEntity> {
    val payments = readPaymentsByUser(userId).map { it.id.value }
    return SepaPaymentLinkEntity.find {
        SepaPaymentLinks.successorId inList payments and (SepaPaymentLinks.predecessorId inList payments)
    }.toList()
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
        PaymentExecutionStatus.FAILED, PaymentExecutionStatus.DROPPED-> Triple(BankStatusCode.FAILED, "FAILED", reasonText?: "Reason unknown")
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

/**
 * Delete a payment.
 * Only allowed if the payment is in CREATED state.
 * The payment is deleted and all history entries are deleted.
 */
fun Transaction.deletePayment(id: UUID): UUID {
    val payment = validatedPayment(id)
    if(payment.status != PaymentExecutionStatus.CREATED) throw SepaException.Payment.StateTransitionForbidden(
        payment.status.name,
        "DELETED"
    )
    // Delete all trivial history entries
    SepaPaymentStatusHistoryEntity.find { SepaPaymentStatusHistory.paymentId eq id }.forEach { it.delete() }
    payment.delete()
    return id
}

fun Transaction.deletePayments(ids: List<UUID>): List<UUID> {
    ids.forEach { deletePayment(it) }
    return ids
}

fun Transaction.validatedPayment(id: UUID): SepaPaymentEntity = SepaPaymentEntity.findById(id)
    ?: throw SepaException.Payment.NoSuchPayment(id.toString())

// they come with the collections
// fun Transaction.readPayments()

fun Transaction.updateSepaMessage(
    modifier: UUID,
    messageId: UUID,
    executionDate: LocalDate,
    numberOfPayments: Int? = null,
) {
    val message = validateSepaMessage(messageId)
    val finalExecutionDate = executionDate.toDateTimeAtCurrentTime()
    val executionDateChanged = message.executionDate != finalExecutionDate
    val numberOfPaymentsChanged = numberOfPayments != null && message.numberOfPayments != numberOfPayments

    val messageChanged = executionDateChanged || numberOfPaymentsChanged

    if(!messageChanged) return

    if(executionDateChanged) {
        message.executionDate = finalExecutionDate
        message.payments.forEach { it.executionDate = finalExecutionDate }
    }
    if(numberOfPaymentsChanged) {
        message.numberOfPayments = message.payments.count().toInt()
        message.totalAmount = message.payments.sumOf { it.amount }
    }

    message.modifiedBy = modifier
    message.modifiedAt = now()
}

fun Transaction.addPaymentsToMessage(
    modifier: UUID,
    messageId: UUID,
    paymentIds: List<UUID>
) {
    if(paymentIds.isEmpty()) return

    val message = validateSepaMessage(messageId)
    if(message.status != SepaMessageStatus.CREATED) throw SepaException.Message.Locked(messageId.toString())

    val payments = SepaPaymentEntity.find { SepaPayments.id inList paymentIds }.toList()
    require(payments.size == paymentIds.size) { "All payments must be found" }
    require(payments.all { it.message == null }) { "All payments must not have messages" }


    payments.forEach {
        it.message = message
        it.executionDate = message.executionDate
    }
    message.numberOfPayments = message.payments.count().toInt()
    message.totalAmount = message.payments.sumOf { it.amount }

    message.modifiedBy = modifier
    message.modifiedAt = now()
}

fun Transaction.movePaymentsToMessage(
    modifier: UUID,
    messageId: UUID,
    paymentIds: List<UUID>
) {
    if(paymentIds.isEmpty()) return

    val message = validateSepaMessage(messageId)
    if(message.status != SepaMessageStatus.CREATED) throw SepaException.Message.Locked(messageId.toString())
    val payments = SepaPaymentEntity.find { SepaPayments.id inList paymentIds }.toList()

    require(payments.size == paymentIds.size) { "All payments must be found" }
    require(payments.none { it.message == null }) { "All payments must have messages" }

    payments.forEach {
        val oldMessage = requireNotNull(it.message) { "All payments must have a message" }
        it.message = message
        it.executionDate = message.executionDate

        oldMessage.numberOfPayments = oldMessage.payments.count().toInt()
        oldMessage.totalAmount = oldMessage.payments.sumOf { payment -> payment.amount }
        oldMessage.modifiedBy = modifier
        oldMessage.modifiedAt = now()

    }
    message.numberOfPayments = message.payments.count().toInt()
    message.totalAmount = message.payments.sumOf { it.amount }

    message.modifiedBy = modifier
    message.modifiedAt = now()
}

fun Transaction.removePaymentsFromMessage(
    modifier: UUID,
    messageId: UUID,
    paymentIds: List<UUID>
) {
    if(paymentIds.isEmpty()) return

    val message = validateSepaMessage(messageId)
    if(message.status != SepaMessageStatus.CREATED) throw SepaException.Message.Locked(messageId.toString())

    val payments = SepaPaymentEntity.find { SepaPayments.id inList paymentIds }.toList()
    require(payments.size == paymentIds.size) { "All payments must be found" }
    require(payments.all { it.message != null }) { "All payments must not have messages" }


    payments.forEach {
        it.message = null
    }
    message.numberOfPayments = message.payments.count().toInt()
    message.totalAmount = message.payments.sumOf { it.amount }

    message.modifiedBy = modifier
    message.modifiedAt = now()
}
fun Transaction.validateSepaMessage(messageId: UUID): SepaMessageEntity = SepaMessageEntity.findById(messageId)
    ?: throw SepaException.Message.NoSuchMessage(messageId.toString())
