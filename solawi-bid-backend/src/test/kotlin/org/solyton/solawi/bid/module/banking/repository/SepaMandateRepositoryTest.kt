package org.solyton.solawi.bid.module.banking.repository

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.dao.flushCache
import org.jetbrains.exposed.sql.and
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.banking.data.*
import org.solyton.solawi.bid.module.banking.data.api.CreateSepaMandateReferenceData
import org.solyton.solawi.bid.module.banking.schema.*
import org.solyton.solawi.bid.module.permission.repository.createChild
import org.solyton.solawi.bid.module.permission.repository.createRootContext
import org.solyton.solawi.bid.module.permission.schema.*
import org.solyton.solawi.bid.module.shares.service.UUID_1
import org.solyton.solawi.bid.module.shares.service.UUID_2
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
import kotlin.test.assertTrue

class SepaMandateRepositoryTest {

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
        SepaPaymentTemplatesTable
    )

    @DbFunctional@Test
    fun `create sepa mandate with retry` () = runSimpleH2Test(*tables) {
        val context = createRootContext("APPLICATION")
        // val childContext =
        context.createChild("ORGANIZATION")

        val creditorId = "DE98ZZZ12345678901"
        val creditorName = "Kreditinstitut Mustermann"
        val creditorBic = BIC("COBADEFFXXX")
        val creditorIban = IBAN("DE89370400440532013000")
        val user = UserEntity.new {
            username = "jfdka"
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
            SepaCollectionKey("COLLECTION KEY"),
            RemittanceInformation("REMITTANCE INFO"),
            SepaSequenceType.FRST,
            null,
        )

        val productReferenceId = UUID_1

        val sepaMandate = createSepaMandateWithRetry(
            UUID_ZERO,
            creditorIdentifier.id.value,
            bankAccount.id.value,
            "DEBTOR NAME",
            DateTime.now(),
            DateTime.now(),
            null,
            collectionId = collection.id.value,
            referenceData = CreateSepaMandateReferenceData(
                SepaMandateReferenceId(productReferenceId.toString()),
                100.0
            )
        )

        assertNotNull("",getSepaMandateCollectionEntity(sepaMandate.id.value, collection.id.value))
    }

    @DbFunctional@Test
    fun `create sepa mandate with retry and reuse sepa mandate` () = runSimpleH2Test(*tables) {
        val context = createRootContext("APPLICATION")
        // val childContext =
        context.createChild("ORGANIZATION")

        val creditorId = "DE98ZZZ12345678901"
        val creditorName = "Kreditinstitut Mustermann"
        val creditorBic = BIC("COBADEFFXXX")
        val creditorIban = IBAN("DE89370400440532013000")
        val user = UserEntity.new {
            username = "jfdka"
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

        val mandateReferencePrefix =MandateReferencePrefix("MANDATE")

        val collection = createSepaCollection(
            UUID_ZERO,
            CreditorIdentifierId(creditorIdentifier.id.value.toString()),
            BankAccountId(bankAccount.id.value.toString()),
            mandateReferencePrefix,
            SepaCollectionKey("COLLECTION KEY"),
            RemittanceInformation("REMITTANCE INFO"),
            SepaSequenceType.FRST,
            null,
        )

        val collection2 = createSepaCollection(
            UUID_ZERO,
            CreditorIdentifierId(creditorIdentifier.id.value.toString()),
            BankAccountId(bankAccount.id.value.toString()),
            mandateReferencePrefix,
            SepaCollectionKey("COLLECTION KEY 2"),
            RemittanceInformation("REMITTANCE INFO"),
            SepaSequenceType.FRST,
            null,
        )

        val productReferenceId1 = UUID_1
        val productReferenceId2 = UUID_2

        val now = DateTime.now()

        val sepaMandate = createSepaMandateWithRetry(
            UUID_ZERO,
            creditorIdentifier.id.value,
            bankAccount.id.value,
            "DEBTOR NAME",
            now,
            now,
            null,
            mandateReferencePrefix = mandateReferencePrefix.value,
            collectionId = collection.id.value,
            referenceData = CreateSepaMandateReferenceData(
                SepaMandateReferenceId(productReferenceId1.toString()),
                100.0
            )
        )
        flushCache()
        assertNotNull("",getSepaMandateCollectionEntity(sepaMandate.id.value, collection.id.value))
        assertEquals( MandateStatus.ACTIVE, sepaMandate.status, "Mandate should be active")
        assertTrue(message = "Mandate reference should start with 'MANDATE'") {  sepaMandate.mandateReference.startsWith("MANDATE") }


        val reusedSepaMandate = createSepaMandateWithRetry(
            UUID_ZERO,
            creditorIdentifier.id.value,
            bankAccount.id.value,
            "DEBTOR NAME",
            now,
            now,
            null,
            mandateReferencePrefix = mandateReferencePrefix.value,
            collectionId = collection2.id.value,
            referenceData = CreateSepaMandateReferenceData(
                SepaMandateReferenceId(productReferenceId2.toString()),
                100.0
            ),
            reUseActiveMandate = true
        )

        assertEquals( sepaMandate.id.value, reusedSepaMandate.id.value)
        assertNotNull(
            "",
            getSepaMandateCollectionEntity(
                sepaMandate.id.value,
                collection2.id.value
            )
        )
        assertTrue{ SepaMandateCollectionEntity.all().count() == 2L }

    }
}

fun getSepaMandateCollectionEntity(sepaMandateId: UUID, sepaCollectionId: UUID): SepaMandateCollectionEntity? {
    return SepaMandateCollectionEntity.find{
        (SepaMandateCollectionsTable.sepaMandateId eq sepaMandateId) and
        (SepaMandateCollectionsTable.sepaCollectionId eq sepaCollectionId)
    }.firstOrNull()
}
