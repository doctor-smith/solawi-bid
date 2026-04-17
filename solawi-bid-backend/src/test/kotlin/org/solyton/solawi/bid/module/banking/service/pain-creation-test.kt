package org.solyton.solawi.bid.module.banking.service

import junit.framework.TestCase.assertFalse
import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.kotlinx.date.now
import org.evoleq.kotlinx.date.today
import org.evoleq.uuid.UUID_ZERO
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.Unit
import org.solyton.solawi.bid.module.banking.data.BIC
import org.solyton.solawi.bid.module.banking.data.BankAccountId
import org.solyton.solawi.bid.module.banking.data.CreditorIdentifierId
import org.solyton.solawi.bid.module.banking.data.IBAN
import org.solyton.solawi.bid.module.banking.data.MandateReferencePrefix
import org.solyton.solawi.bid.module.banking.data.RemittanceInformation
import org.solyton.solawi.bid.module.banking.data.SepaMandateReferenceId
import org.solyton.solawi.bid.module.banking.data.api.CreateSepaMandateReferenceData
import org.solyton.solawi.bid.module.banking.data.api.SepaPayments
import org.solyton.solawi.bid.module.banking.data.internal.Pain008GenerationRequest
import org.solyton.solawi.bid.module.banking.data.internal.Pain008Transaction
import org.solyton.solawi.bid.module.banking.repository.createBankAccount
import org.solyton.solawi.bid.module.banking.repository.createLegalEntity
import org.solyton.solawi.bid.module.banking.repository.createPayment
import org.solyton.solawi.bid.module.banking.repository.createPaymentsForCollection
import org.solyton.solawi.bid.module.banking.repository.createSepaCollection
import org.solyton.solawi.bid.module.banking.repository.createSepaMandateWithRetry
import org.solyton.solawi.bid.module.banking.repository.generateSepaMessageForCollection
import org.solyton.solawi.bid.module.banking.schema.*
import org.solyton.solawi.bid.module.permission.repository.createChild
import org.solyton.solawi.bid.module.permission.repository.createRole
import org.solyton.solawi.bid.module.permission.repository.createRootContext
import org.solyton.solawi.bid.module.permission.schema.*
import org.solyton.solawi.bid.module.shares.service.UUID_1
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateUserProfile
import org.solyton.solawi.bid.module.user.repository.createUserProfile
import org.solyton.solawi.bid.module.user.schema.AddressesTable
import org.solyton.solawi.bid.module.user.schema.OrganizationsTable
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserStatus
import org.solyton.solawi.bid.module.user.schema.UsersTable
import org.solyton.solawi.bid.module.values.Firstname
import org.solyton.solawi.bid.module.values.Lastname
import org.solyton.solawi.bid.module.values.PhoneNumber
import org.solyton.solawi.bid.module.values.UserId
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Duration.Companion.days

class Pain008FunctionsTest {

    val tables = arrayOf(
        ContextsTable,
        RightsTable,
        RolesTable,
        RoleRightContexts,
        UserRoleContext,
        BankAccountsTable,
        BankAccountAccessorsTable,
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
        SepaPaymentStatusHistory
    )

    @DbFunctional
    @Test
    fun generateDirectDebitFile() = runSimpleH2Test(*tables) {

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

        val transactions = listOf(
            Pain008Transaction(
                endToEndId = "TXN-001-20240402",
                amount = BigDecimal("125.50"),
                debtorName = "Max Mustermann",
                debtorIban = "DE89370400440532013000",
                debtorBic = "COBADEFFXXX",
                mandateReference = "MAND-CRED-20240402-ABC123",
                mandateSignDate = LocalDate.parse("2024-03-15"),
                remittanceInfo = "Mitgliedsbeitrag März 2024",
                sequenceType = SepaSequenceType.OOFF
            ),
            Pain008Transaction(
                endToEndId = "TXN-002-20240402",
                amount = BigDecimal("89.99"),
                debtorName = "Anna Schmidt",
                debtorIban = "DE12500105170648489890",
                mandateReference = "MAND-CRED-20240315-XYZ789",
                mandateSignDate = LocalDate.parse("2024-02-20"),
                remittanceInfo = "Jahresbeitrag 2024",
                sequenceType = SepaSequenceType.OOFF
            )
        )

        val request = Pain008GenerationRequest(
            creditorId = creditorIdentifier.id.value,
            creditorAccountId = bankAccount.id.value,
            executionDate = LocalDate.now().plusDays(5),
            transactions = transactions,
            createdBy = UUID_ZERO,
        )

        // ✅ Funktionale Top-Level-Funktion verwenden
        val xmlContent = generatePain008Xml(request)

        // Grundlegende XML-Prüfungen
        assertNotNull(xmlContent)
        assertTrue(xmlContent.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"))
        assertTrue(xmlContent.contains("pain.008.001.02"))
        assertTrue(xmlContent.contains("CstmrDrctDbtInitn"))

        // Schema-Validierung
        val validationResult = validatePain008XmlWithDetails(xmlContent)
        when (validationResult) {
            is DetailedValidationResult.Valid -> {
                println("✅ XML ist schema-konform")
            }

            is DetailedValidationResult.Invalid -> {
                println("❌ XML-Schema-Validierung fehlgeschlagen:")
                validationResult.errors.forEach { error ->
                    println("  ${error.severity} - Zeile ${error.line}, Spalte ${error.column}: ${error.message}")
                }
                fail("XML entspricht nicht dem pain.008 Schema. Fehler: ${validationResult.errors.size}")
            }

            is DetailedValidationResult.Error -> {
                fail("Validierungsfehler: ${validationResult.message}")
            }
        }

        // Inhaltliche Validierung
        assertXmlContent(xmlContent, request)

        println("Generated valid pain.008 XML:")
        println(xmlContent)
    }

    private fun assertXmlContent(xmlContent: String, request: Pain008GenerationRequest) {
        // Prüfe Anzahl Transaktionen
        val txnCount = request.transactions.size
        assertTrue(xmlContent.contains("<NbOfTxs>$txnCount</NbOfTxs>"))

        // Prüfe alle Transaktions-IDs
        request.transactions.forEach { transaction ->
            assertTrue(xmlContent.contains(transaction.endToEndId))
            assertTrue(xmlContent.contains(transaction.debtorIban))
            assertTrue(xmlContent.contains(transaction.mandateReference))
        }
    }


    @DbFunctional@Test
    fun generateSepaMessageFileFromCollection() = runSimpleH2Test(*tables) {
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

        assertTrue {
            SepaCollectionEntity.findById(collection.id.value)?.sepaMandates?.contains(sepaMandate)?:false
        }

        val executionDate = LocalDate.now().plusDays(5)

        val payments = createPaymentsForCollection(
            UUID_ZERO,
            collection.id.value,
            executionDate
        )
        assertTrue {
            payments.isNotEmpty()
            SepaCollectionEntity.findById(collection.id.value)?.sepaPayments?.contains(payments.first())?:false
        }
        val painString = generateSepaMessageForCollection(
            UUID_ZERO,
            collection.id.value,
            executionDate
        )

        println(painString)

        val validationResult = validatePain008XmlWithDetails(painString)

        when (validationResult) {
            is DetailedValidationResult.Valid -> {
                println("✅ XML ist schema-konform")
            }

            is DetailedValidationResult.Invalid -> {
                println("❌ XML-Schema-Validierung fehlgeschlagen:")
                validationResult.errors.forEach { error ->
                    println("  ${error.severity} - Zeile ${error.line}, Spalte ${error.column}: ${error.message}")
                }
                fail("XML entspricht nicht dem pain.008 Schema. Fehler: ${validationResult.errors.size}")
            }

            is DetailedValidationResult.Error -> {
                fail("Validierungsfehler: ${validationResult.message}")
            }
        }
    }
}
class Pain008FunctionsUnitTest{
    @Unit
    @Test
    fun testAmountFormatting() {
        // ✅ Funktionale Top-Level-Funktion testen
        assertEquals("125.50", formatAmount(BigDecimal("125.50")))
        assertEquals("89.99", formatAmount(BigDecimal("89.99")))
        assertEquals("1000.00", formatAmount(BigDecimal("1000")))
        assertEquals("0.01", formatAmount(BigDecimal("0.01")))
    }

    @Unit
    @Test
    fun testMessageIdGeneration() {
        val messageId1 = generateMessageId()
        val messageId2 = generateMessageId()

        assertTrue(messageId1.startsWith("MSG-"))
        assertTrue(messageId2.startsWith("MSG-"))
        assertTrue(messageId1 != messageId2) // Sollten unterschiedlich sein
    }

    @Unit
    @Test
    fun testXmlEscaping() {
        assertEquals("&amp;", escapeXml("&"))
        assertEquals("&lt;Test&gt;", escapeXml("<Test>"))
        assertEquals("&quot;Quote&quot;", escapeXml("\"Quote\""))
    }

    @Unit
    @Test
    fun testIbanValidation() {
        assertTrue(isValidIban("DE89370400440532013000"))
        assertTrue(isValidIban("AT611904300234573201"))
        assertFalse(isValidIban("INVALID"))
        assertFalse(isValidIban("12345"))
    }
}

class Pain008ValidationFunctionsTest {

    @Unit
    @Test
    fun testSchemaAvailability() {
        assertTrue(isPain008SchemaAvailable(), "pain.008 Schema sollte verfügbar sein")
    }

    @Unit
    @Test
    fun testValidXmlStructure() {
        val validXml = """<?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.008.001.02">
                <CstmrDrctDbtInitn>
                    <GrpHdr>
                        <MsgId>TEST</MsgId>
                    </GrpHdr>
                    <PmtInf>
                        <PmtInfId>TEST</PmtInfId>
                    </PmtInf>
                </CstmrDrctDbtInitn>
            </Document>""".trimIndent()

        assertTrue(validateXmlStructure(validXml))
        assertTrue(hasPain008Namespace(validXml))
    }

    @Unit
    @Test
    fun testInvalidXmlStructure() {
        val invalidXml = "<Document><Unclosed></Document>"
        
        assertFalse(validateXmlStructure(invalidXml))
    }

    @Unit
    @Test
    fun testPain008Namespace() {
        val validNamespace = """<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.008.001.02">"""
        val invalidNamespace = """<Document xmlns="some.other.namespace">"""

        assertTrue(hasPain008Namespace(validNamespace))
        assertFalse(hasPain008Namespace(invalidNamespace))
    }

    @Unit
    @Test
    fun testQuickValidation() {
        val validQuickXml = """<?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.008.001.02">
                <CstmrDrctDbtInitn>
                    <GrpHdr></GrpHdr>
                    <PmtInf></PmtInf>
                </CstmrDrctDbtInitn>
            </Document>""".trimIndent()

        val result = quickValidatePain008(validQuickXml)
        assertTrue(result is ValidationResult.Valid)
    }

    @Unit
    @Test
    fun testQuickValidationMissingElements() {
        val invalidXml = """<?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.008.001.02">
                <CstmrDrctDbtInitn>
                    <!-- Missing GrpHdr and PmtInf -->
                </CstmrDrctDbtInitn>
            </Document>""".trimIndent()

        val result = quickValidatePain008(invalidXml)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Unit
    @Test
    fun testValidationErrorHandler() {
        val errorHandler = ValidationErrorHandler()
        
        assertFalse(errorHandler.hasErrors())
        assertFalse(errorHandler.hasWarnings())
        
        // Simuliere Fehler (normalerweise würde das der XML-Parser machen)
        assertEquals(0, errorHandler.getErrors().size)
    }

    @Unit
    @Test
    fun testValidationStatistics() {
        val errors = listOf(
            ValidationError(1, 1, "Warning message", ErrorSeverity.WARNING),
            ValidationError(2, 1, "Error message", ErrorSeverity.ERROR),
            ValidationError(3, 1, "Fatal error message", ErrorSeverity.FATAL_ERROR)
        )

        val stats = getValidationStatistics(errors)
        
        assertEquals(3, stats.totalErrors)
        assertEquals(1, stats.warnings)
        assertEquals(1, stats.errors)
        assertEquals(1, stats.fatalErrors)
        assertFalse(stats.isValid)
        assertTrue(stats.hasIssues)
    }

    @Unit
    @Test
    fun testFormatValidationErrors() {
        val errors = listOf(
            ValidationError(1, 10, "Test error", ErrorSeverity.ERROR),
            ValidationError(null, null, "General warning", ErrorSeverity.WARNING)
        )

        val formatted = formatValidationErrors(errors)
        
        assertTrue(formatted.contains("Zeile 1, Spalte 10"))
        assertTrue(formatted.contains("Test error"))
        assertTrue(formatted.contains("WARNING"))
        assertTrue(formatted.contains("ERROR"))
    }

    @Unit
    @Test
    fun testBatchValidation() {
        val xmls = listOf(
            """<?xml version="1.0"?><Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.008.001.02"><CstmrDrctDbtInitn><GrpHdr></GrpHdr><PmtInf></PmtInf></CstmrDrctDbtInitn></Document>""",
            """<InvalidXml""" // Ungültiges XML
        )

        val results = validateMultiplePain008Xmls(xmls)
        
        assertEquals(2, results.size)
        assertEquals(0, results[0].first) // Index 0
        assertEquals(1, results[1].first) // Index 1
    }
}
