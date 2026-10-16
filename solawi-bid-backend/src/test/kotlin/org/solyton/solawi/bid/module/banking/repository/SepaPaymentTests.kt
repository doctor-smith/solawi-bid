package org.solyton.solawi.bid.module.banking.repository

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.dao.flushCache
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.banking.data.*
import org.solyton.solawi.bid.module.banking.exception.SepaException
import org.solyton.solawi.bid.module.banking.schema.*
import org.solyton.solawi.bid.module.banking.service.generateMessageId
import org.solyton.solawi.bid.module.permission.repository.createChild
import org.solyton.solawi.bid.module.permission.repository.createRootContext
import org.solyton.solawi.bid.module.permission.schema.*
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateUserProfile
import org.solyton.solawi.bid.module.user.repository.createUserProfile
import org.solyton.solawi.bid.module.user.schema.*
import org.solyton.solawi.bid.module.values.Firstname
import org.solyton.solawi.bid.module.values.Lastname
import org.solyton.solawi.bid.module.values.PhoneNumber
import org.solyton.solawi.bid.module.values.UserId
import java.util.*
import kotlin.test.DefaultAsserter.assertNotNull
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@Suppress("LargeClass")
class SepaPaymentTests {

    val tables = arrayOf(
        ContextsTable,
        RightsTable,
        RolesTable,
        RoleRightContexts,
        UserRoleContext,
        BankAccountsTable,
        BankAccountAccessorsTable,
        org.solyton.solawi.bid.module.banking.schema.SepaMandates,
        SepaMessages,
        LegalEntitiesTable,
        UsersTable,
        OrganizationsTable,
        AddressesTable,
        SepaPaymentsTable,
        SepaCollectionsTable,
        SepaCollectionMappings,
        SepaMandateDataMappingsTable,
        SepaMandateCollectionsTable,
        SepaPaymentStatusHistory,
        SepaPaymentTemplatesTable,
        SepaPaymentLinks,
    )

    // Sets up a fully functional SEPA environment (creditor legal entity, bank account, creditor
    // identifier, debtor user with bank account and an active mandate) so that payment-related
    // tests only need to focus on the payment logic itself.
    private fun Transaction.setupMandateAndCollection(
        collectionKey: String = "COLLECTION KEY",
        sequenceType: SepaSequenceType = SepaSequenceType.FRST,
    ): Pair<SepaMandateEntity, SepaCollectionEntity> {
        val creditorId = "DE98ZZZ12345678901"
        val creditorName = "Kreditinstitut Mustermann"
        val creditorBic = BIC("COBADEFFXXX")
        val creditorIban = IBAN("DE89370400440532013000")
        val user = UserEntity.new {
            username = "user-${UUID.randomUUID()}"
            password = "fjdkal"
            status = UserStatus.ACTIVE
            createdBy = UUID_ZERO
        }

        val userProfile = createUserProfile(
            CreateUserProfile(
                UserId(user.id.value.toString()),
                Firstname("fs"),
                Lastname("ls"),
                null,
                phoneNumber = null,
                phoneNumber1 = PhoneNumber("123456"),
                CreateAddress.empty,
            ),
            creatorId = UUID_ZERO,
        )
        val address = userProfile.addresses.first()

        val legalEntity = createLegalEntity(
            user.id.value,
            creditorName,
            "LF",
            LegalEntityType.HUMAN,
            address.id.value,
            UUID_ZERO
        )

        val bankAccount = createBankAccount(
            user.id.value,
            creditorIban,
            creditorBic,
            creditorName,
            true,
            AccountType.CREDITOR,
            emptyList(),
            "Nice bank account",
            UUID_ZERO
        )

        val creditorIdentifier = CreditorIdentifier.new {
            createdBy = UUID_ZERO
            this.creditorId = creditorId
            this.legalEntity = legalEntity
            isActive = true
            validUntil = DateTime.now().plusYears(1)
            validFrom = DateTime.now().minusYears(1)
        }

        val collection = createSepaCollection(
            UUID_ZERO,
            CreditorIdentifierId(creditorIdentifier.id.value.toString()),
            BankAccountId(bankAccount.id.value.toString()),
            MandateReferencePrefix("MANDATE"),
            SepaCollectionKey(collectionKey),
            RemittanceInformation("REMITTANCE INFO"),
            sequenceType,
            null,
        )

        val mandate = createSepaMandateWithRetry(
            UUID_ZERO,
            creditorIdentifier.id.value,
            bankAccount.id.value,
            "DEBTOR NAME",
            DateTime.now(),
            DateTime.now(),
            null,
            collectionId = collection.id.value,
        )

        return mandate to collection
    }

    // Checks that creating a payment for an active mandate/collection pair persists a payment in
    // status CREATED, with a fresh "FRST" (first collection) sequence type, and that a SEPA payment
    // template is created on the fly to remember the agreed amount for that mandate-collection pair,
    // which is a prerequisite for later recurring ("RCUR") direct debit collections.
    @DbFunctional@Test
    fun `create payment creates payment with template in CREATED status` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val payment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        flushCache()

        assertEquals(PaymentExecutionStatus.CREATED, payment.status)
        assertEquals(SepaSequenceType.FRST, payment.sequenceType)
        assertEquals(100.0, payment.amount)
        assertNotNull("A payment template must be created for the mandate-collection pair", payment.template)
        assertEquals(mandate.id.value, payment.template?.mandate?.id?.value)
    }

    // Checks that creating a second payment for the same mandate/collection reuses the existing
    // SEPA payment template without overwriting its agreed template amount, while recording the
    // payment's own individual amount. Also verifies that updating the template explicitly modifies
    // the template amount.
    @DbFunctional@Test
    fun `create payment reuses existing payment template without modifying its amount` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val firstPayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        flushCache()
        val templateId = firstPayment.template?.id?.value
        assertNotNull("Payment template ID should not be null", templateId)

        val secondPayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            150.0,
            LocalDate.now().plusMonths(1),
            ignoreOldPayments = true,
        )
        flushCache()

        assertEquals(templateId, secondPayment.template?.id?.value)
        assertEquals(150.0, secondPayment.amount)
        assertEquals(100.0, secondPayment.template?.amount)

        // Explicit update of the template modifies its agreed amount
        val updatedTemplate = updateSepaPaymentTemplate(
            UUID_ZERO,
            requireNotNull(templateId),
            mandate.id.value,
            collection.id.value,
            150.0,
            SepaSequenceType.FRST
        )
        flushCache()

        assertEquals(150.0, updatedTemplate.amount)
    }

    // Checks that payments can only be created for mandates with an ACTIVE status: once a mandate
    // has been revoked by the debtor (e.g. a member cancels their direct debit authorization), any
    // attempt to create a new payment against it must be rejected to prevent unauthorized collections.
    @DbFunctional@Test
    fun `create payment fails for revoked mandate` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()
        mandate.status = MandateStatus.REVOKED
        flushCache()

        assertFailsWith<SepaException.Payment.CannotCreate> {
            createPayment(
                UUID_ZERO,
                mandate.id.value,
                collection.id.value,
                100.0,
                LocalDate.now(),
            )
        }
    }

    // Checks that an ad-hoc payment (e.g. a one-off extra collection requested outside the regular
    // collection cycle) is linked to its predecessor payment via a SepaPaymentLink of kind AD_HOC,
    // so that the payment history clearly shows the causal relationship between the original payment
    // and the additionally created one.
    @DbFunctional@Test
    fun `create ad hoc payment links predecessor and successor` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val predecessor = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        flushCache()

        val adHocPayment = createAdHocPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            25.0,
            LocalDate.now().plusDays(5),
            predecessorId = predecessor.id.value,
        )
        flushCache()

        val link = SepaPaymentLinkEntity.find {
            (SepaPaymentLinks.predecessorId eq predecessor.id.value) and
            (SepaPaymentLinks.successorId eq adHocPayment.id.value)
        }.firstOrNull()

        assertNotNull("An AD_HOC link between predecessor and the new payment must be persisted", link)
        assertEquals(SuccessorKind.AD_HOC, link?.kind)
        assertEquals(25.0, adHocPayment.amount)
    }

    // Checks the core SEPA sequence-type lifecycle: a mandate's very first ("FRST") direct debit
    // payment, once CONFIRMED by the bank, must produce a successor payment with sequence type
    // "RCUR" (recurring) for the following collection period - reflecting that only the first
    // collection of a mandate uses the FRST sequence, all subsequent ones use RCUR.
    @DbFunctional@Test
    fun `create successor of confirmed FRST payment shifts sequence type to RCUR` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val firstPayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        flushCache()
        assertEquals(SepaSequenceType.FRST, firstPayment.sequenceType)

        val sepaMessage = SepaMessageEntity.new {
            createdBy = UUID_ZERO
            createdAt = DateTime.now()
            creditorIdentifier = mandate.creditorIdentifier
            creditorAccount = collection.creditorAccount
            messageId = generateMessageId()
            executionDate = LocalDate.now().toDateTimeAtCurrentTime()
            status = SepaMessageStatus.CREATED
            numberOfPayments = 1
            totalAmount = 100.0
        }
        flushCache()

        updatePayment(
            UUID_ZERO,
            firstPayment.id.value,
            firstPayment.amount,
            firstPayment.executionDate.toLocalDate(),
            firstPayment.sequenceType,
            PaymentExecutionStatus.CONFIRMED,
            DateTime.now(),
            null,
            null,
            SepaMessageId(sepaMessage.id.value.toString()),
        )
        flushCache()

        val successor = createSuccessorOfPayment(
            UUID_ZERO,
            LocalDate.now().plusMonths(1),
            firstPayment,
            SuccessorKind.NEXT_PERIOD,
        )
        flushCache()

        assertEquals(SepaSequenceType.RCUR, successor.sequenceType)
        assertEquals(firstPayment.id.value, successor.predecessors.first().id.value)
    }

    // Checks that creating a successor is only allowed for payments related to an ACTIVE mandate:
    // once a mandate is revoked, no further successor payments (e.g. next period collections) may be
    // created for its already confirmed payments, protecting against collecting money without a
    // valid authorization.
    @DbFunctional@Test
    fun `create successor of payment fails when mandate is no longer active` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val payment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        flushCache()

        val sepaMessage = SepaMessageEntity.new {
            createdBy = UUID_ZERO
            createdAt = DateTime.now()
            creditorIdentifier = mandate.creditorIdentifier
            creditorAccount = collection.creditorAccount
            messageId = generateMessageId()
            executionDate = LocalDate.now().toDateTimeAtCurrentTime()
            status = SepaMessageStatus.CREATED
            numberOfPayments = 1
            totalAmount = 100.0
        }
        flushCache()

        updatePayment(
            UUID_ZERO,
            payment.id.value,
            payment.amount,
            payment.executionDate.toLocalDate(),
            payment.sequenceType,
            PaymentExecutionStatus.CONFIRMED,
            DateTime.now(),
            null,
            null,
            sepaMessage,
        )
        mandate.status = MandateStatus.REVOKED
        flushCache()

        assertFailsWith<SepaException.Payment.CannotCreate> {
            createSuccessorOfPayment(
                UUID_ZERO,
                LocalDate.now().plusMonths(1),
                payment,
                SuccessorKind.NEXT_PERIOD,
            )
        }
    }

    // Checks that a payment may only be deleted while it is still in status CREATED, i.e. before it
    // has been submitted to the bank: deleting a freshly created payment succeeds and removes it
    // (together with its history entries), while deleting a payment that was already confirmed by
    // the bank must be rejected to avoid losing the audit trail of a real money transfer.
    @DbFunctional@Test
    fun `delete payment is only allowed while payment is still in CREATED status` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val createdPayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        flushCache()
        val createdPaymentId = createdPayment.id.value

        deletePayment(createdPaymentId)
        flushCache()
        assertNull(SepaPaymentEntity.findById(createdPaymentId))

        val confirmedPayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now().plusMonths(1),
            ignoreOldPayments = true,
        )
        flushCache()

        val sepaMessage = SepaMessageEntity.new {
            createdBy = UUID_ZERO
            createdAt = DateTime.now()
            creditorIdentifier = mandate.creditorIdentifier
            creditorAccount = collection.creditorAccount
            messageId = generateMessageId()
            executionDate = LocalDate.now().toDateTimeAtCurrentTime()
            status = SepaMessageStatus.CREATED
            numberOfPayments = 1
            totalAmount = 100.0
            messageId = generateMessageId()
        }
        flushCache()

        updatePayment(
            UUID_ZERO,
            confirmedPayment.id.value,
            confirmedPayment.amount,
            confirmedPayment.executionDate.toLocalDate(),
            confirmedPayment.sequenceType,
            PaymentExecutionStatus.CONFIRMED,
            DateTime.now(),
            "reason",
            null,
            SepaMessageId(sepaMessage.id.value.toString()),
        )
        flushCache()

        assertFailsWith<SepaException.Payment.StateTransitionForbidden> {
            deletePayment(confirmedPayment.id.value)
        }
    }

    // Checks that updating a payment's status from CREATED to any other status requires a SEPA message ID.
    @DbFunctional@Test
    fun `update payment fails when status changes without providing sepa message` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val payment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        flushCache()

        val exception = assertFailsWith<IllegalArgumentException> {
            updatePayment(
                UUID_ZERO,
                payment.id.value,
                payment.amount,
                payment.executionDate.toLocalDate(),
                payment.sequenceType,
                PaymentExecutionStatus.CONFIRMED,
                DateTime.now(),
                "reason",
                null,
                null as SepaMessageId?,
            )
        }
        assertEquals("Message must be provided if status is changed", exception.message)
    }

    // Checks that updating a payment to CONFIRMED with a SEPA message synchronizes the message status.
    @DbFunctional@Test
    fun `update payment with sepa message succeeds and updates message status to CONFIRMED` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val payment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        val sepaMessage = SepaMessageEntity.new {
            createdBy = UUID_ZERO
            createdAt = DateTime.now()
            creditorIdentifier = mandate.creditorIdentifier
            creditorAccount = collection.creditorAccount
            messageId = generateMessageId()
            executionDate = LocalDate.now().toDateTimeAtCurrentTime()
            status = SepaMessageStatus.CREATED
            numberOfPayments = 1
            totalAmount = 100.0
        }
        flushCache()

        val updatedPayment = updatePayment(
            UUID_ZERO,
            payment.id.value,
            payment.amount,
            payment.executionDate.toLocalDate(),
            payment.sequenceType,
            PaymentExecutionStatus.CONFIRMED,
            DateTime.now(),
            "reason",
            null,
            SepaMessageId(sepaMessage.id.value.toString()),
        )
        flushCache()

        assertEquals(PaymentExecutionStatus.CONFIRMED, updatedPayment.status)
        assertEquals(sepaMessage.id.value, updatedPayment.message?.id?.value)
        assertEquals(SepaMessageStatus.CONFIRMED, updatedPayment.message?.status)
    }

    // Checks that updating a payment's details while keeping status CREATED does not require a message ID.
    @DbFunctional@Test
    fun `update payment in CREATED status succeeds without sepa message` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val payment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        flushCache()

        val newExecutionDate = LocalDate.now().plusDays(7)
        val updatedPayment = updatePayment(
            UUID_ZERO,
            payment.id.value,
            150.0,
            newExecutionDate,
            SepaSequenceType.FRST,
            PaymentExecutionStatus.CREATED,
            null,
            null,
            null,
            null as SepaMessageId?,
        )
        flushCache()

        assertEquals(150.0, updatedPayment.amount)
        assertEquals(PaymentExecutionStatus.CREATED, updatedPayment.status)
        assertNull(updatedPayment.message)
    }

    // Checks that `createPaymentsForCollection` only considers ACTIVE mandates of a collection and,
    // for a mandate that has no payments yet, creates exactly one brand-new payment using the total
    // amount recorded in the mandate's data mappings (e.g. the sum of all product/subscription
    // references billed through that mandate) - the typical "first collection run" of a new member.
    @DbFunctional@Test
    fun `create payments for collection creates new payment from mandate data mapping amount` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        attachSepaMandate(
            modifierId = UUID_ZERO,
            sepaMandateId = mandate.id.value,
            mandateReferenceId = UUID.randomUUID(),
            amount = 42.0,
            collectionId = collection.id.value,
        )
        flushCache()

        val payments = createPaymentsForCollection(
            UUID_ZERO,
            collection.id.value,
            LocalDate.now(),
        )

        assertEquals(1, payments.size)
        assertEquals(42.0, payments.first().amount)
        assertEquals(mandate.id.value, payments.first().mandate.id.value)
    }

    // Checks that if the latest payment of a mandate is already a recurring ("RCUR") payment, the
    // next payment created for that mandate keeps the RCUR sequence type as well, reflecting that
    // once the first ("FRST") collection has shifted to RCUR, all further collections stay RCUR.
    @DbFunctional@Test
    fun `create payment keeps RCUR sequence type when latest payment is RCUR` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val firstPayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        firstPayment.sequenceType = SepaSequenceType.RCUR
        flushCache()

        val secondPayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now().plusMonths(1),
        )
        flushCache()

        assertEquals(SepaSequenceType.RCUR, secondPayment.sequenceType)
    }

    // Checks that a FRST payment that was PAYED_MANUALLY keeps producing a FRST successor payment,
    // since manual settlement does not count as a confirmed bank collection that would allow
    // shifting the sequence type to RCUR.
    @DbFunctional@Test
    fun `create payment keeps FRST sequence type when latest FRST payment was payed manually` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val firstPayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        firstPayment.status = PaymentExecutionStatus.PAYED_MANUALLY
        flushCache()

        val secondPayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now().plusMonths(1),
        )
        flushCache()

        assertEquals(SepaSequenceType.FRST, secondPayment.sequenceType)
    }

    // Checks that a FRST payment that FAILED does not shift to RCUR either: the next payment stays
    // FRST, since a failed collection attempt must be retried as the initial collection.
    @DbFunctional@Test
    fun `create payment keeps FRST sequence type when latest FRST payment failed` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val firstPayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        firstPayment.status = PaymentExecutionStatus.FAILED
        flushCache()

        val secondPayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now().plusMonths(1),
        )
        flushCache()

        assertEquals(SepaSequenceType.FRST, secondPayment.sequenceType)
    }

    // Checks that a FRST payment left in any other status (e.g. still CREATED, not yet processed by
    // the bank) makes the sequence type of the next payment UNCLEAR, since it is not known whether
    // the mandate's first collection will eventually succeed or fail.
    @DbFunctional@Test
    fun `create payment sets UNCLEAR sequence type when latest FRST payment is in an undetermined status` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        flushCache()

        val secondPayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now().plusMonths(1),
        )
        flushCache()

        assertEquals(SepaSequenceType.UNCLEAR, secondPayment.sequenceType)
    }

    // Checks that once a mandate's sequence type has become UNCLEAR, it stays UNCLEAR for any
    // further payment, since the ambiguity about the first collection's outcome is never resolved
    // by simply creating more payments.
    @DbFunctional@Test
    fun `create payment keeps UNCLEAR sequence type when latest payment is UNCLEAR` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val firstPayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        firstPayment.sequenceType = SepaSequenceType.UNCLEAR
        flushCache()

        val secondPayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now().plusMonths(1),
        )
        flushCache()

        assertEquals(SepaSequenceType.UNCLEAR, secondPayment.sequenceType)
    }

    // Checks that creating a payment after the mandate's latest payment was FNAL (the final payment
    // of a mandate) is rejected: no further collection should ever be attempted after a mandate's
    // final payment has been recorded.
    @DbFunctional@Test
    fun `create payment fails when latest payment was FNAL` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val firstPayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        firstPayment.sequenceType = SepaSequenceType.FNAL
        flushCache()

        assertFailsWith<SepaException.Payment.CannotCreate> {
            createPayment(
                UUID_ZERO,
                mandate.id.value,
                collection.id.value,
                100.0,
                LocalDate.now().plusMonths(1),
            )
        }
    }

    // Checks that creating a payment after the mandate's latest payment was an OOFF (one-off)
    // payment is rejected: a one-off collection must not be followed by another regular collection
    // for the same mandate.
    @DbFunctional@Test
    fun `create payment fails when latest payment was OOFF` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val firstPayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        firstPayment.sequenceType = SepaSequenceType.OOFF
        flushCache()

        assertFailsWith<SepaException.Payment.CannotCreate> {
            createPayment(
                UUID_ZERO,
                mandate.id.value,
                collection.id.value,
                100.0,
                LocalDate.now().plusMonths(1),
            )
        }
    }

    // Checks that if the collection itself is configured with the OOFF (one-off) default sequence
    // type, every payment created for it gets the OOFF sequence type, overriding any history-based
    // computation - since one-off collections never participate in the FRST/RCUR lifecycle.
    @DbFunctional@Test
    fun `create payment uses OOFF sequence type when collection default sequence type is OOFF` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection(sequenceType = SepaSequenceType.OOFF)

        val payment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        flushCache()

        assertEquals(SepaSequenceType.OOFF, payment.sequenceType)
    }

    // Checks that a successor can also be created for a predecessor payment that was PAYED_MANUALLY,
    // not only for CONFIRMED ones, since manually settled payments are explicitly allowed as a basis
    // for the next collection period according to the function's documented contract.
    @DbFunctional@Test
    fun `create successor of PAYED_MANUALLY payment succeeds` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val payment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        payment.status = PaymentExecutionStatus.PAYED_MANUALLY
        flushCache()

        val successor = createSuccessorOfPayment(
            UUID_ZERO,
            LocalDate.now().plusMonths(1),
            payment,
            SuccessorKind.NEXT_PERIOD,
        )
        flushCache()

        assertEquals(payment.id.value, successor.predecessors.first().id.value)
    }

    // Checks that a successor can also be created for a predecessor payment that FAILED, since a
    // failed collection attempt should still be followed by a new attempt in the next period.
    @DbFunctional@Test
    fun `create successor of FAILED payment succeeds` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val payment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        payment.status = PaymentExecutionStatus.FAILED
        flushCache()

        val successor = createSuccessorOfPayment(
            UUID_ZERO,
            LocalDate.now().plusMonths(1),
            payment,
            SuccessorKind.NEXT_PERIOD,
        )
        flushCache()

        assertEquals(payment.id.value, successor.predecessors.first().id.value)
    }

    // Checks that creating a successor is rejected when the predecessor payment is still in a status
    // outside the allowed set (CONFIRMED, PAYED_MANUALLY, FAILED), e.g. still CREATED, since no
    // definitive outcome of the current collection is known yet.
    @DbFunctional@Test
    fun `create successor of payment fails when predecessor status is not allowed` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val payment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        flushCache()

        assertFailsWith<SepaException.Payment.CannotCreate> {
            createSuccessorOfPayment(
                UUID_ZERO,
                LocalDate.now().plusMonths(1),
                payment,
                SuccessorKind.NEXT_PERIOD,
            )
        }
    }

    // Checks that `createPaymentsForCollection`, for a mandate that already has a relevant (non-
    // recurred) payment in an allowed status, creates a successor payment for the next period
    // instead of a brand-new one, and links it to the prior payment - the typical "ongoing member"
    // collection run.
    @DbFunctional@Test
    fun `create payments for collection creates successor for mandate with existing confirmed payment` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val existingPayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        existingPayment.status = PaymentExecutionStatus.CONFIRMED
        flushCache()

        val payments = createPaymentsForCollection(
            UUID_ZERO,
            collection.id.value,
            LocalDate.now().plusMonths(1),
        )

        assertEquals(1, payments.size)
        assertEquals(existingPayment.id.value, payments.first().predecessors.first().id.value)
    }

    // Checks that `createPaymentsForCollection` filters out mandates whose only payment is already
    // in a disallowed status (e.g. still CREATED), so no successor nor new payment is created for
    // such a mandate, avoiding duplicate collections while the previous one is still pending.
    @DbFunctional@Test
    fun `create payments for collection skips mandate with payment in disallowed status` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        flushCache()

        val payments = createPaymentsForCollection(
            UUID_ZERO,
            collection.id.value,
            LocalDate.now().plusMonths(1),
        )

        assertEquals(0, payments.size)
    }

    // Checks that `createPaymentsForCollection` respects the optional `mandateIds` filter: when
    // provided, only mandates whose ID is contained in that list are considered, even if other
    // active mandates with pending data mappings exist for the same collection.
    @DbFunctional@Test
    fun `create payments for collection respects mandateIds filter` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        attachSepaMandate(
            modifierId = UUID_ZERO,
            sepaMandateId = mandate.id.value,
            mandateReferenceId = UUID.randomUUID(),
            amount = 42.0,
            collectionId = collection.id.value,
        )
        flushCache()

        val payments = createPaymentsForCollection(
            UUID_ZERO,
            collection.id.value,
            LocalDate.now(),
            mandateIds = listOf(UUID.randomUUID()),
        )

        assertEquals(0, payments.size)
    }

    // Checks that `createPaymentsForCollection` throws when a mandate without any payments also has
    // no `SepaMandateDataMapping` entry, since there is no information on the amount to be collected
    // for a brand-new payment in that case.
    @DbFunctional@Test
    fun `create payments for collection fails when mandate has no data mapping and no payments` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        assertFailsWith<IllegalArgumentException> {
            createPaymentsForCollection(
                UUID_ZERO,
                collection.id.value,
                LocalDate.now(),
            )
        }
    }

    // Checks that updating the `failureReason` of a payment is correctly propagated to the status
    // history entry when the payment status transitions to FAILED, so that the reason for a failed
    // collection attempt is preserved for later inspection.
    @DbFunctional@Test
    fun `update payment to FAILED status records failure reason in history` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val payment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        val sepaMessage = SepaMessageEntity.new {
            createdBy = UUID_ZERO
            createdAt = DateTime.now()
            creditorIdentifier = mandate.creditorIdentifier
            creditorAccount = collection.creditorAccount
            messageId = generateMessageId()
            executionDate = LocalDate.now().toDateTimeAtCurrentTime()
            status = SepaMessageStatus.CREATED
            numberOfPayments = 1
            totalAmount = 100.0
        }
        flushCache()

        val updatedPayment = updatePayment(
            UUID_ZERO,
            payment.id.value,
            payment.amount,
            payment.executionDate.toLocalDate(),
            payment.sequenceType,
            PaymentExecutionStatus.FAILED,
            DateTime.now(),
            "Insufficient funds",
            null,
            SepaMessageId(sepaMessage.id.value.toString()),
        )
        flushCache()

        assertEquals(PaymentExecutionStatus.FAILED, updatedPayment.status)
        assertEquals(SepaMessageStatus.FAILED, updatedPayment.message?.status)

        val historyEntry = SepaPaymentStatusHistoryEntity.find {
            SepaPaymentStatusHistory.paymentId eq updatedPayment.id.value
        }.sortedByDescending { it.reportedAt }.first()

        assertEquals(BankStatusCode.FAILED, historyEntry.status)
        assertEquals("Insufficient funds", historyEntry.reasonText)
    }

    // Checks that once a payment has been submitted (e.g. CONFIRMED), its amount and execution date
    // can no longer be changed, protecting the integrity of an already-processed bank transaction,
    // while a pure status change (e.g. to PAYED_MANUALLY) is still allowed.
    @DbFunctional@Test
    fun `update payment amount is forbidden once payment is no longer in CREATED status` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val payment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        val sepaMessage = SepaMessageEntity.new {
            createdBy = UUID_ZERO
            createdAt = DateTime.now()
            creditorIdentifier = mandate.creditorIdentifier
            creditorAccount = collection.creditorAccount
            messageId = generateMessageId()
            executionDate = LocalDate.now().toDateTimeAtCurrentTime()
            status = SepaMessageStatus.CREATED
            numberOfPayments = 1
            totalAmount = 100.0
        }
        flushCache()

        val confirmedPayment = updatePayment(
            UUID_ZERO,
            payment.id.value,
            payment.amount,
            payment.executionDate.toLocalDate(),
            payment.sequenceType,
            PaymentExecutionStatus.CONFIRMED,
            DateTime.now(),
            null,
            null,
            SepaMessageId(sepaMessage.id.value.toString()),
        )
        flushCache()

        assertFailsWith<SepaException.Payment.ChangesNotAllowed> {
            updatePayment(
                UUID_ZERO,
                confirmedPayment.id.value,
                200.0,
                confirmedPayment.executionDate.toLocalDate(),
                confirmedPayment.sequenceType,
                PaymentExecutionStatus.CONFIRMED,
                null,
                null,
                null,
                null as SepaMessageId?,
            )
        }
    }

    // Checks that `deletePayments` deletes all the given payments as long as they are all still in
    // CREATED status, returning the list of deleted payment IDs, matching the single-payment
    // `deletePayment` semantics for the bulk operation.
    @DbFunctional@Test
    fun `delete payments deletes all given created payments` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val firstPayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        val secondPayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now().plusMonths(1),
            ignoreOldPayments = true,
        )
        flushCache()

        val deletedIds = deletePayments(listOf(firstPayment.id.value, secondPayment.id.value))
        flushCache()

        assertEquals(listOf(firstPayment.id.value, secondPayment.id.value), deletedIds)
        assertNull(SepaPaymentEntity.findById(firstPayment.id.value))
        assertNull(SepaPaymentEntity.findById(secondPayment.id.value))
    }

    // Checks that `deletePayments` fails as soon as it encounters a payment that is no longer in
    // CREATED status (e.g. already CONFIRMED), aborting the bulk deletion rather than silently
    // deleting only some of the requested payments.
    @DbFunctional@Test
    fun `delete payments fails when one of the payments is no longer deletable` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (mandate, collection) = setupMandateAndCollection()

        val deletablePayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        val confirmedPayment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now().plusMonths(1),
            ignoreOldPayments = true,
        )
        confirmedPayment.status = PaymentExecutionStatus.CONFIRMED
        flushCache()

        assertFailsWith<SepaException.Payment.StateTransitionForbidden> {
            deletePayments(listOf(deletablePayment.id.value, confirmedPayment.id.value))
        }
    }
}
