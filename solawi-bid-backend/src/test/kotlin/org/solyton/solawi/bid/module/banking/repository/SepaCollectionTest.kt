package org.solyton.solawi.bid.module.banking.repository

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.dao.flushCache
import org.jetbrains.exposed.sql.Transaction
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.banking.data.*
import org.solyton.solawi.bid.module.banking.exception.BankAccountsException
import org.solyton.solawi.bid.module.banking.exception.SepaException
import org.solyton.solawi.bid.module.banking.schema.*
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
import kotlin.test.assertTrue

@Suppress("LargeClass")
class SepaCollectionTest {

    val tables = arrayOf(
        ContextsTable,
        RightsTable,
        RolesTable,
        RoleRightContexts,
        UserRoleContext,
        BankAccountsTable,
        BankAccountAccessorsTable,
        CreditorIdentifiersTable,
        SepaMandates,
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

    // Creates a creditor legal entity together with a creditor bank account and a creditor
    // identifier, so that tests only need to focus on the collection logic itself.
    private fun Transaction.setupCreditor(
        creditorId: String = "DE98ZZZ12345678901",
        creditorName: String = "Kreditinstitut Mustermann",
    ): Pair<LegalEntityEntity, BankAccountEntity> {
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
            IBAN("DE89370400440532013000"),
            BIC("COBADEFFXXX"),
            creditorName,
            true,
            AccountType.CREDITOR,
            emptyList(),
            "Nice bank account",
            UUID_ZERO
        )

        CreditorIdentifierEntity.new {
            createdBy = UUID_ZERO
            this.creditorId = creditorId
            this.legalEntity = legalEntity
            isActive = true
            validUntil = DateTime.now().plusYears(1)
            validFrom = DateTime.now().minusYears(1)
        }

        return legalEntity to bankAccount
    }

    private fun Transaction.creditorIdentifierOf(legalEntity: LegalEntityEntity): CreditorIdentifierEntity =
        CreditorIdentifierEntity.find { CreditorIdentifiersTable.legalEntityId eq legalEntity.id.value }.first()

    // Checks that creating a collection persists all the provided configuration values correctly,
    // and that defaults (charge bearer, lead times, active flag) are applied as documented.
    @DbFunctional@Test
    fun `create sepa collection persists configured values` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (legalEntity, bankAccount) = setupCreditor()
        val creditorIdentifier = creditorIdentifierOf(legalEntity)

        val collection = createSepaCollection(
            UUID_ZERO,
            CreditorIdentifierId(creditorIdentifier.id.value.toString()),
            BankAccountId(bankAccount.id.value.toString()),
            MandateReferencePrefix("MANDATE"),
            SepaCollectionKey("COLLECTION KEY"),
            RemittanceInformation("REMITTANCE INFO"),
            SepaSequenceType.FRST,
            null,
        )
        flushCache()

        assertEquals(creditorIdentifier.id.value, collection.creditorIdentifier.id.value)
        assertEquals(bankAccount.id.value, collection.creditorAccount.id.value)
        assertEquals("MANDATE", collection.mandateReferencePrefix)
        assertEquals("COLLECTION KEY", collection.collectionKey)
        assertEquals("REMITTANCE INFO", collection.remittanceInformation)
        assertEquals(SepaSequenceType.FRST, collection.sequenceType)
        assertNull(collection.localInstrument)
        assertEquals("SLEV", collection.chargeBearer)
        assertEquals(2, collection.leadTimesDays)
        assertTrue(collection.isActive)
        assertEquals(0, collection.maxRetries)
    }

    // Checks that, when given a list of reference IDs (e.g. product/subscription references that
    // should be billed through this collection), creating a collection also persists a
    // SepaCollectionMapping entry for each of them.
    @DbFunctional@Test
    fun `create sepa collection creates mapping for each reference id` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (legalEntity, bankAccount) = setupCreditor()
        val creditorIdentifier = creditorIdentifierOf(legalEntity)

        val referenceId1 = UUID.randomUUID()
        val referenceId2 = UUID.randomUUID()

        val collection = createSepaCollection(
            UUID_ZERO,
            CreditorIdentifierId(creditorIdentifier.id.value.toString()),
            BankAccountId(bankAccount.id.value.toString()),
            MandateReferencePrefix("MANDATE"),
            SepaCollectionKey("COLLECTION KEY"),
            RemittanceInformation("REMITTANCE INFO"),
            SepaSequenceType.FRST,
            null,
            referenceIds = listOf(SepaCollectionReferenceId(referenceId1.toString()), SepaCollectionReferenceId(referenceId2.toString())),
        )
        flushCache()

        val mappedReferenceIds = collection.referenceIds.map { it.referenceId }.toSet()
        assertEquals(setOf(referenceId1, referenceId2), mappedReferenceIds)
    }

    // Checks that updating a collection only changes the fields that actually differ from the
    // current values, and that modifiedBy/modifiedAt are set once a real change happened.
    @DbFunctional@Test
    fun `update sepa collection applies changes and sets modified metadata` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (legalEntity, bankAccount) = setupCreditor()
        val creditorIdentifier = creditorIdentifierOf(legalEntity)

        val collection = createSepaCollection(
            UUID_ZERO,
            CreditorIdentifierId(creditorIdentifier.id.value.toString()),
            BankAccountId(bankAccount.id.value.toString()),
            MandateReferencePrefix("MANDATE"),
            SepaCollectionKey("COLLECTION KEY"),
            RemittanceInformation("REMITTANCE INFO"),
            SepaSequenceType.FRST,
            null,
        )
        flushCache()
        assertNull(collection.modifiedAt)

        val modifier = UUID.randomUUID()
        val updated = updateSepaCollection(
            modifier,
            SepaCollectionId(collection.id.value.toString()),
            CreditorIdentifierId(creditorIdentifier.id.value.toString()),
            BankAccountId(bankAccount.id.value.toString()),
            MandateReferencePrefix("MANDATE"),
            SepaCollectionKey("COLLECTION KEY"),
            RemittanceInformation("NEW REMITTANCE INFO"),
            SepaSequenceType.RCUR,
            null,
            isActive = false,
        )
        flushCache()

        assertEquals("NEW REMITTANCE INFO", updated.remittanceInformation)
        assertEquals(SepaSequenceType.RCUR, updated.sequenceType)
        assertEquals(false, updated.isActive)
        assertEquals(modifier, updated.modifiedBy)
        assertNotNull("modifiedAt must be set once the collection changed", updated.modifiedAt)
    }

    // Checks that updating a collection with exactly the same values as before is a no-op
    // regarding audit metadata: modifiedBy/modifiedAt must stay untouched.
    @DbFunctional@Test
    fun `update sepa collection without changes does not touch modified metadata` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (legalEntity, bankAccount) = setupCreditor()
        val creditorIdentifier = creditorIdentifierOf(legalEntity)

        val collection = createSepaCollection(
            UUID_ZERO,
            CreditorIdentifierId(creditorIdentifier.id.value.toString()),
            BankAccountId(bankAccount.id.value.toString()),
            MandateReferencePrefix("MANDATE"),
            SepaCollectionKey("COLLECTION KEY"),
            RemittanceInformation("REMITTANCE INFO"),
            SepaSequenceType.FRST,
            null,
        )
        flushCache()

        val updated = updateSepaCollection(
            UUID.randomUUID(),
            SepaCollectionId(collection.id.value.toString()),
            CreditorIdentifierId(creditorIdentifier.id.value.toString()),
            BankAccountId(bankAccount.id.value.toString()),
            MandateReferencePrefix("MANDATE"),
            SepaCollectionKey("COLLECTION KEY"),
            RemittanceInformation("REMITTANCE INFO"),
            SepaSequenceType.FRST,
            null,
        )
        flushCache()

        assertNull(updated.modifiedBy)
        assertNull(updated.modifiedAt)
    }

    // Checks that trying to resolve a collection by an ID that does not exist throws the dedicated
    // NoSuchSepaCollection exception instead of e.g. a generic NPE.
    @DbFunctional@Test
    fun `validated sepa collection fails for unknown id` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")

        assertFailsWith<SepaException.NoSuchSepaCollection> {
            validatedSepaCollection(UUID.randomUUID())
        }
    }

    // Checks that reading collections by legal entity only returns collections belonging to
    // creditor identifiers of that legal entity, even if other, unrelated collections exist for a
    // different legal entity - essential for isolating organizations from each other.
    @DbFunctional@Test
    fun `read sepa collections by legal entity only returns own collections` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (legalEntityA, bankAccountA) = setupCreditor(creditorId = "DE98ZZZ12345678901", creditorName = "Creditor A")
        val creditorIdentifierA = creditorIdentifierOf(legalEntityA)
        val (legalEntityB, bankAccountB) = setupCreditor(creditorId = "DE98ZZZ12345678902", creditorName = "Creditor B")
        val creditorIdentifierB = creditorIdentifierOf(legalEntityB)

        val collectionA = createSepaCollection(
            UUID_ZERO,
            CreditorIdentifierId(creditorIdentifierA.id.value.toString()),
            BankAccountId(bankAccountA.id.value.toString()),
            MandateReferencePrefix("MANDATE"),
            SepaCollectionKey("COLLECTION A"),
            RemittanceInformation("REMITTANCE INFO A"),
            SepaSequenceType.FRST,
            null,
        )
        createSepaCollection(
            UUID_ZERO,
            CreditorIdentifierId(creditorIdentifierB.id.value.toString()),
            BankAccountId(bankAccountB.id.value.toString()),
            MandateReferencePrefix("MANDATE"),
            SepaCollectionKey("COLLECTION B"),
            RemittanceInformation("REMITTANCE INFO B"),
            SepaSequenceType.FRST,
            null,
        )
        flushCache()

        val result = readSepaCollectionsByLegalEntity(legalEntityA.id.value)

        assertEquals(1, result.all.size)
        assertEquals(collectionA.id.value.toString(), result.all.first().sepaCollectionId.value)
    }

    // Checks that creating a collection with non-default optional parameters persists those
    // non-default values instead of silently falling back to the defaults.
    @DbFunctional@Test
    fun `create sepa collection persists non default values` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (legalEntity, bankAccount) = setupCreditor()
        val creditorIdentifier = creditorIdentifierOf(legalEntity)

        val collection = createSepaCollection(
            UUID_ZERO,
            CreditorIdentifierId(creditorIdentifier.id.value.toString()),
            BankAccountId(bankAccount.id.value.toString()),
            MandateReferencePrefix("MANDATE"),
            SepaCollectionKey("COLLECTION KEY"),
            RemittanceInformation("REMITTANCE INFO"),
            SepaSequenceType.FRST,
            LocalInstrument("CORE"),
            chargeBearer = ChargeBearer("DEBT"),
            leadTimesDays = 5,
            purposeCode = PurposeCode("SALA"),
            retryOnFailure = true,
            maxRetries = 3,
        )
        flushCache()

        assertEquals("CORE", collection.localInstrument)
        assertEquals("DEBT", collection.chargeBearer)
        assertEquals(5, collection.leadTimesDays)
        assertEquals("SALA", collection.purposeCode)
        assertTrue(collection.retryOnFailure)
        assertEquals(3, collection.maxRetries)
    }

    // Checks that creating a collection without any reference IDs does not create any
    // SepaCollectionMapping entries at all.
    @DbFunctional@Test
    fun `create sepa collection without reference ids creates no mappings` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (legalEntity, bankAccount) = setupCreditor()
        val creditorIdentifier = creditorIdentifierOf(legalEntity)

        val collection = createSepaCollection(
            UUID_ZERO,
            CreditorIdentifierId(creditorIdentifier.id.value.toString()),
            BankAccountId(bankAccount.id.value.toString()),
            MandateReferencePrefix("MANDATE"),
            SepaCollectionKey("COLLECTION KEY"),
            RemittanceInformation("REMITTANCE INFO"),
            SepaSequenceType.FRST,
            null,
        )
        flushCache()

        assertEquals(0, collection.referenceIds.count())
    }

    // Checks that creating a collection with an unknown creditor identifier id fails with the
    // dedicated exception instead of e.g. a generic NPE.
    @DbFunctional@Test
    fun `create sepa collection fails for unknown creditor identifier id` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (_, bankAccount) = setupCreditor()

        assertFailsWith<BankAccountsException.NoSuchCreditorIdentifier> {
            createSepaCollection(
                UUID_ZERO,
                CreditorIdentifierId(UUID.randomUUID().toString()),
                BankAccountId(bankAccount.id.value.toString()),
                MandateReferencePrefix("MANDATE"),
                SepaCollectionKey("COLLECTION KEY"),
                RemittanceInformation("REMITTANCE INFO"),
                SepaSequenceType.FRST,
                null,
            )
        }
    }

    // Checks that creating a collection with an unknown creditor account id fails with the
    // dedicated exception instead of e.g. a generic NPE.
    @DbFunctional@Test
    fun `create sepa collection fails for unknown creditor account id` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (legalEntity, _) = setupCreditor()
        val creditorIdentifier = creditorIdentifierOf(legalEntity)

        assertFailsWith<BankAccountsException.NoSuchBankAccount> {
            createSepaCollection(
                UUID_ZERO,
                CreditorIdentifierId(creditorIdentifier.id.value.toString()),
                BankAccountId(UUID.randomUUID().toString()),
                MandateReferencePrefix("MANDATE"),
                SepaCollectionKey("COLLECTION KEY"),
                RemittanceInformation("REMITTANCE INFO"),
                SepaSequenceType.FRST,
                null,
            )
        }
    }

    // Checks that updating only a single field (collectionKey) does not accidentally depend on, or
    // trigger, changes in other unrelated fields - guards against accidental cross-field coupling
    // of the individual `*Changed` flags.
    @DbFunctional@Test
    fun `update sepa collection with single field change only changes that field` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (legalEntity, bankAccount) = setupCreditor()
        val creditorIdentifier = creditorIdentifierOf(legalEntity)

        val collection = createSepaCollection(
            UUID_ZERO,
            CreditorIdentifierId(creditorIdentifier.id.value.toString()),
            BankAccountId(bankAccount.id.value.toString()),
            MandateReferencePrefix("MANDATE"),
            SepaCollectionKey("COLLECTION KEY"),
            RemittanceInformation("REMITTANCE INFO"),
            SepaSequenceType.FRST,
            null,
        )
        flushCache()

        val updated = updateSepaCollection(
            UUID.randomUUID(),
            SepaCollectionId(collection.id.value.toString()),
            CreditorIdentifierId(creditorIdentifier.id.value.toString()),
            BankAccountId(bankAccount.id.value.toString()),
            MandateReferencePrefix("MANDATE"),
            SepaCollectionKey("NEW COLLECTION KEY"),
            RemittanceInformation("REMITTANCE INFO"),
            SepaSequenceType.FRST,
            null,
        )
        flushCache()

        assertEquals("NEW COLLECTION KEY", updated.collectionKey)
        assertEquals("REMITTANCE INFO", updated.remittanceInformation)
        assertEquals(SepaSequenceType.FRST, updated.sequenceType)
        assertEquals(bankAccount.id.value, updated.creditorAccount.id.value)
        assertEquals(creditorIdentifier.id.value, updated.creditorIdentifier.id.value)
    }

    // Checks that updating a collection's creditor identifier and creditor account to different,
    // valid entities actually swaps the FK relation to the new entities.
    @DbFunctional@Test
    fun `update sepa collection changes creditor identifier and account` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (legalEntityA, bankAccountA) = setupCreditor(creditorId = "DE98ZZZ12345678901", creditorName = "Creditor A")
        val creditorIdentifierA = creditorIdentifierOf(legalEntityA)
        val (legalEntityB, bankAccountB) = setupCreditor(creditorId = "DE98ZZZ12345678902", creditorName = "Creditor B")
        val creditorIdentifierB = creditorIdentifierOf(legalEntityB)

        val collection = createSepaCollection(
            UUID_ZERO,
            CreditorIdentifierId(creditorIdentifierA.id.value.toString()),
            BankAccountId(bankAccountA.id.value.toString()),
            MandateReferencePrefix("MANDATE"),
            SepaCollectionKey("COLLECTION KEY"),
            RemittanceInformation("REMITTANCE INFO"),
            SepaSequenceType.FRST,
            null,
        )
        flushCache()

        val updated = updateSepaCollection(
            UUID.randomUUID(),
            SepaCollectionId(collection.id.value.toString()),
            CreditorIdentifierId(creditorIdentifierB.id.value.toString()),
            BankAccountId(bankAccountB.id.value.toString()),
            MandateReferencePrefix("MANDATE"),
            SepaCollectionKey("COLLECTION KEY"),
            RemittanceInformation("REMITTANCE INFO"),
            SepaSequenceType.FRST,
            null,
        )
        flushCache()

        assertEquals(creditorIdentifierB.id.value, updated.creditorIdentifier.id.value)
        assertEquals(bankAccountB.id.value, updated.creditorAccount.id.value)
    }

    // Checks that trying to update a collection by an ID that does not exist throws the dedicated
    // NoSuchSepaCollection exception via updateSepaCollection's own call path.
    @DbFunctional@Test
    fun `update sepa collection fails for unknown id` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (legalEntity, bankAccount) = setupCreditor()
        val creditorIdentifier = creditorIdentifierOf(legalEntity)

        assertFailsWith<SepaException.NoSuchSepaCollection> {
            updateSepaCollection(
                UUID.randomUUID(),
                SepaCollectionId(UUID.randomUUID().toString()),
                CreditorIdentifierId(creditorIdentifier.id.value.toString()),
                BankAccountId(bankAccount.id.value.toString()),
                MandateReferencePrefix("MANDATE"),
                SepaCollectionKey("COLLECTION KEY"),
                RemittanceInformation("REMITTANCE INFO"),
                SepaSequenceType.FRST,
                null,
            )
        }
    }

    // Checks that calling updateSepaCollection does not alter the collection's existing
    // SepaCollectionMappings - updateSepaCollection does not accept/touch referenceIds at all,
    // so this documents/guards that behavior.
    @DbFunctional@Test
    fun `update sepa collection does not change existing reference mappings` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (legalEntity, bankAccount) = setupCreditor()
        val creditorIdentifier = creditorIdentifierOf(legalEntity)
        val referenceId = UUID.randomUUID()

        val collection = createSepaCollection(
            UUID_ZERO,
            CreditorIdentifierId(creditorIdentifier.id.value.toString()),
            BankAccountId(bankAccount.id.value.toString()),
            MandateReferencePrefix("MANDATE"),
            SepaCollectionKey("COLLECTION KEY"),
            RemittanceInformation("REMITTANCE INFO"),
            SepaSequenceType.FRST,
            null,
            referenceIds = listOf(SepaCollectionReferenceId(referenceId.toString())),
        )
        flushCache()

        updateSepaCollection(
            UUID.randomUUID(),
            SepaCollectionId(collection.id.value.toString()),
            CreditorIdentifierId(creditorIdentifier.id.value.toString()),
            BankAccountId(bankAccount.id.value.toString()),
            MandateReferencePrefix("MANDATE"),
            SepaCollectionKey("NEW COLLECTION KEY"),
            RemittanceInformation("REMITTANCE INFO"),
            SepaSequenceType.FRST,
            null,
        )
        flushCache()

        assertEquals(setOf(referenceId), collection.referenceIds.map { it.referenceId }.toSet())
    }

    // Checks that trying to read collections for a legal entity that does not exist fails with
    // the dedicated NoSuchLegalEntity exception rather than returning an empty result silently.
    @DbFunctional@Test
    fun `read sepa collections by legal entity fails for unknown legal entity` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")

        assertFailsWith<Exception> {
            readSepaCollectionsByLegalEntity(UUID.randomUUID())
        }
    }

    // Checks that a legal entity which has a creditor identifier but no collections at all
    // returns an empty ApiSepaCollections instead of erroring.
    @DbFunctional@Test
    fun `read sepa collections by legal entity returns empty result when no collections exist` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (legalEntity, _) = setupCreditor()

        val result = readSepaCollectionsByLegalEntity(legalEntity.id.value)

        assertEquals(0, result.all.size)
    }

    // Checks the successor-ID enrichment logic in readSepaCollectionsByLegalEntity: when a
    // payment has a next-period successor, the enriched API payment must expose the successor's
    // ID so API consumers can follow the payment successor chain.
    @DbFunctional@Test
    fun `read sepa collections by legal entity enriches payments with next period successor id` () = runSimpleH2Test(*tables) {
        createRootContext("APPLICATION").createChild("ORGANIZATION")
        val (legalEntity, bankAccount) = setupCreditor()
        val creditorIdentifier = creditorIdentifierOf(legalEntity)

        val collection = createSepaCollection(
            UUID_ZERO,
            CreditorIdentifierId(creditorIdentifier.id.value.toString()),
            BankAccountId(bankAccount.id.value.toString()),
            MandateReferencePrefix("MANDATE"),
            SepaCollectionKey("COLLECTION KEY"),
            RemittanceInformation("REMITTANCE INFO"),
            SepaSequenceType.FRST,
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

        val payment = createPayment(
            UUID_ZERO,
            mandate.id.value,
            collection.id.value,
            100.0,
            LocalDate.now(),
        )
        flushCache()
        payment.status = PaymentExecutionStatus.CONFIRMED
        flushCache()

        val successor = createSuccessorOfPayment(
            UUID_ZERO,
            LocalDate.now().plusMonths(1),
            payment,
            SuccessorKind.NEXT_PERIOD,
        )
        flushCache()

        val result = readSepaCollectionsByLegalEntity(legalEntity.id.value)

        val apiPayment = result.all.first().sepaPayments?.all?.first { it.sepaPaymentId.value == payment.id.value.toString() }
        assertEquals(successor.id.value.toString(), apiPayment?.nextPeriodSuccessorId?.value)
    }
}
