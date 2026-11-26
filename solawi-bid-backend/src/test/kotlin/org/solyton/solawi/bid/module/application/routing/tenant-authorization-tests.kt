package org.solyton.solawi.bid.module.application.routing

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.headers.Header
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.application.application.useApplicationTestDatabase
import org.solyton.solawi.bid.module.application.repository.createApplication
import org.solyton.solawi.bid.module.application.repository.createUserRoleContext
import org.solyton.solawi.bid.module.application.repository.registerOrganizationForApplication
import org.solyton.solawi.bid.module.banking.application.BANKING_APPLICATION_NAME
import org.solyton.solawi.bid.module.banking.data.RemittanceInformation
import org.solyton.solawi.bid.module.banking.data.SepaMessageId
import org.solyton.solawi.bid.module.banking.data.api.MergeSepaMessages
import org.solyton.solawi.bid.module.banking.data.api.SepaMessages
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.repository.createLegalEntity
import org.solyton.solawi.bid.module.banking.schema.AccountType
import org.solyton.solawi.bid.module.banking.schema.BankAccountEntity
import org.solyton.solawi.bid.module.banking.schema.CreditorIdentifierEntity
import org.solyton.solawi.bid.module.banking.schema.LegalEntityType
import org.solyton.solawi.bid.module.banking.schema.SepaMessageEntity
import org.solyton.solawi.bid.module.banking.schema.SepaPaymentsTable
import org.solyton.solawi.bid.module.distribution.data.api.UpdateDistributionPoint
import org.solyton.solawi.bid.module.distribution.data.toApiType
import org.solyton.solawi.bid.module.distribution.repository.createDistributionPoint
import org.solyton.solawi.bid.module.distribution.schema.DistributionPointEntity
import org.solyton.solawi.bid.module.distribution.schema.DistributionPointsTable
import org.solyton.solawi.bid.module.permission.repository.*
import org.solyton.solawi.bid.module.shares.data.api.UpdateShareType
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.shares.repository.createShareType
import org.solyton.solawi.bid.module.shares.schema.ShareTypeEntity
import org.solyton.solawi.bid.module.shares.schema.ShareTypesTable
import org.solyton.solawi.bid.module.testFramework.getTestToken
import org.solyton.solawi.bid.module.user.repository.createAddress
import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UsersTable
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class TenantAuthorizationTests {
    @Api @Test
    fun distributionPointUpdateRequiresPermissionInBothOrganizations() = runBlocking {
        testApplication {
            val database = useApplicationTestDatabase()
            val sourceToken = client.getTestToken(SOURCE_USER)
            val destinationToken = client.getTestToken(DESTINATION_USER)

            val (providers, pointId) = transaction(database) {
                SchemaUtils.createMissingTablesAndColumns(DistributionPointsTable)
                val providers = createProviders("DISTRIBUTION", "UPDATE_DISTRIBUTION_POINTS", "UPDATE_DISTRIBUTION_POINTS")
                val point = createDistributionPoint("Original point", null, providers.source.organizationId, UUID_ZERO)
                providers to point.id.value
            }
            fun storedPoint() = transaction(database) {
                val point = assertNotNull(DistributionPointEntity.findById(pointId))
                point.toApiType(this) to point.modifiedBy
            }
            val before = storedPoint()
            val update = UpdateDistributionPoint(
                pointId.toString(), "Moved point", providers.destination.organizationId.toString(), null
            )

            val path = "distribution-points/update"
            assertEquals(HttpStatusCode.Forbidden, client.patchInContext(path, sourceToken, providers.source.contextId, update))
            assertEquals(before, storedPoint())
            assertEquals(
                HttpStatusCode.Forbidden,
                client.patchInContext(path, destinationToken, providers.destination.contextId, update)
            )
            assertEquals(before, storedPoint())

            transaction(database) { providers.destination.grantTo(providers.sourceUserId) }
            assertEquals(HttpStatusCode.OK, client.patchInContext(path, sourceToken, providers.source.contextId, update))
            assertEquals(
                before.first.copy(name = update.name, organizationId = update.organizationId) to providers.sourceUserId,
                storedPoint()
            )
        }
    }

    @Api @Test
    fun shareTypeUpdateAcceptsDifferentEligibleRightsInTheTwoProviderContexts() = runBlocking {
        testApplication {
            val database = useApplicationTestDatabase()
            val sourceToken = client.getTestToken(SOURCE_USER)
            val destinationToken = client.getTestToken(DESTINATION_USER)

            val (providers, shareTypeId) = transaction(database) {
                SchemaUtils.createMissingTablesAndColumns(ShareTypesTable)
                val providers = createProviders("SHARE_MANAGEMENT", "UPDATE_SHARE_TYPES", "MANAGE_SHARE_TYPES")
                val shareType = createShareType(
                    providers.source.organizationId, "Original shares", "original", "Original description", UUID_ZERO
                )
                providers to shareType.id.value
            }
            fun storedShareType() = transaction(database) {
                val shareType = assertNotNull(ShareTypeEntity.findById(shareTypeId))
                shareType.toApiType() to shareType.modifiedBy
            }
            val before = storedShareType()
            val update = UpdateShareType(
                shareTypeId.toString(), "Moved shares", "moved", "Updated description",
                providers.destination.organizationId.toString()
            )

            val path = "shares/types/update"
            assertEquals(HttpStatusCode.Forbidden, client.patchInContext(path, sourceToken, providers.source.contextId, update))
            assertEquals(before, storedShareType())
            assertEquals(
                HttpStatusCode.Forbidden,
                client.patchInContext(path, destinationToken, providers.destination.contextId, update)
            )
            assertEquals(before, storedShareType())

            transaction(database) { providers.destination.grantTo(providers.sourceUserId) }
            assertEquals(HttpStatusCode.OK, client.patchInContext(path, sourceToken, providers.source.contextId, update))
            assertEquals(
                before.first.copy(
                    name = update.name, key = update.key, description = update.description, providerId = update.providerId
                ) to providers.sourceUserId,
                storedShareType()
            )
        }
    }

    @Api @Test
    fun sepaMessagesAreScopedToTheCreditorAndDeniedBatchesPreserveTheMessages() = runBlocking {
        testApplication {
            val database = useApplicationTestDatabase()
            val sourceToken = client.getTestToken(SOURCE_USER)
            val destinationToken = client.getTestToken(DESTINATION_USER)

            val (providers, source, destination) = transaction(database) {
                SchemaUtils.createMissingTablesAndColumns(SepaPaymentsTable)
                val providers = createProviders(
                    BANKING_APPLICATION_NAME, "MANAGE_BANK_TRANSACTIONS", "MANAGE_BANK_TRANSACTIONS"
                )
                Triple(
                    providers,
                    createSepaMessage(providers.source, "Source"),
                    createSepaMessage(providers.destination, "Destination")
                )
            }
            fun storedMessages() = transaction(database) {
                SepaMessageEntity.all().map { it.toApiType() to it.modifiedBy }.toSet()
            }
            val before = storedMessages()
            assertEquals(2, before.size)

            val path = "banking/sepa/messages/by-legal-entity"
            val foreignRead = client.get(path) {
                bearerAuth(sourceToken)
                header(Header.CONTEXT, providers.source.contextId.toString())
                parameter("legal_entity", destination.legalEntityId.toString())
            }
            assertEquals(HttpStatusCode.Forbidden, foreignRead.status)

            val authorizedRead = client.get(path) {
                bearerAuth(destinationToken)
                header(Header.CONTEXT, providers.destination.contextId.toString())
                parameter("legal_entity", destination.legalEntityId.toString())
            }
            assertEquals(HttpStatusCode.OK, authorizedRead.status)
            val response = Json.decodeFromString(ResultSerializer<SepaMessages>(), authorizedRead.bodyAsText())
            assertEquals(listOf(destination.messageId), assertIs<Result.Success<SepaMessages>>(response).data.all.map { it.sepaMessageId })

            val batch = MergeSepaMessages(
                listOf(source.messageId, destination.messageId), LocalDate(2026, 12, 1), RemittanceInformation("Test")
            )
            val mixedBatch = client.post("banking/sepa/messages/merge") {
                bearerAuth(sourceToken)
                header(Header.CONTEXT, providers.source.contextId.toString())
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(batch))
            }
            assertEquals(HttpStatusCode.Forbidden, mixedBatch.status)
            assertEquals(before, storedMessages())

            val emptyBatch = client.post("banking/sepa/messages/merge") {
                bearerAuth(sourceToken)
                header(Header.CONTEXT, providers.source.contextId.toString())
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(batch.copy(sepaMessageIds = emptyList())))
            }
            assertEquals(HttpStatusCode.BadRequest, emptyBatch.status)
            val failure = Json.decodeFromString(Result.Failure.Message.serializer(), emptyBatch.bodyAsText())
            assertEquals(Result.Failure.Message("At least one SEPA message is required"), failure)
            assertEquals(before, storedMessages())
        }
    }

    private fun Transaction.createSepaMessage(provider: Provider, name: String): SepaFixture {
        val date = DateTime(2026, 12, 1, 0, 0, DateTimeZone.UTC)
        val address = createAddress(name, name, "Test street", "", "Test city", "", "00000", "DE", UUID_ZERO)
        val legalEntity = createLegalEntity(
            provider.organizationId, name, "Test organization", LegalEntityType.ORGANIZATION, address.id.value, UUID_ZERO
        )
        val creditor = CreditorIdentifierEntity.new {
            this.legalEntity = legalEntity
            creditorId = "test-creditor-$name"
            validFrom = date
            createdBy = UUID_ZERO
        }
        val account = BankAccountEntity.new {
            userId = provider.organizationId
            iban = "test-iban"
            bic = "test-bic"
            accountType = AccountType.CREDITOR
            createdBy = UUID_ZERO
        }
        val message = SepaMessageEntity.new {
            creditorIdentifier = creditor
            creditorAccount = account
            messageId = "test-message-$name"
            executionDate = date
            createdBy = UUID_ZERO
        }
        return SepaFixture(legalEntity.id.value, SepaMessageId(message.id.value.toString()))
    }

    private fun Transaction.createProviders(
        applicationName: String,
        sourceRight: String,
        destinationRight: String
    ): Providers {
        val application = createApplication(
            applicationName, "Tenant authorization fixture", UUID_ZERO,
            defaultContextId = createRootContext(applicationName).id.value
        )
        val sourceUserId = findUserId(SOURCE_USER)
        val source = createProvider("Source provider", application.id.value, sourceRight, sourceUserId)
        val destination = createProvider(
            "Destination provider", application.id.value, destinationRight, findUserId(DESTINATION_USER)
        )
        return Providers(source, destination, sourceUserId)
    }

    private fun Transaction.createProvider(
        name: String,
        applicationId: UUID,
        rightName: String,
        userId: UUID
    ): Provider {
        val organizationContext = createRootContext(name)
        val organization = OrganizationEntity.new {
            this.name = name
            context = organizationContext
            createdBy = userId
        }
        val applicationContext = registerOrganizationForApplication(organization.id.value, applicationId).context
        val role = createRole("$name editor", "Can edit the fixture resource", UUID_ZERO)
        val right = createRightIfNotExists(rightName, "Can edit the fixture resource", UUID_ZERO)
        (role of applicationContext).grant(right)
        return Provider(organization.id.value, applicationContext.id.value, role.name).also { it.grantTo(userId) }
    }

    private fun findUserId(username: String) =
        UserEntity.find { UsersTable.username eq username }.single().id.value

    private suspend inline fun <reified T> HttpClient.patchInContext(
        path: String,
        token: String,
        contextId: UUID,
        update: T
    ): HttpStatusCode = patch(path) {
        bearerAuth(token)
        header(Header.CONTEXT, contextId.toString())
        contentType(ContentType.Application.Json)
        setBody(Json.encodeToString(update))
    }.status

    private data class SepaFixture(val legalEntityId: UUID, val messageId: SepaMessageId)

    private data class Providers(val source: Provider, val destination: Provider, val sourceUserId: UUID)

    private data class Provider(val organizationId: UUID, val contextId: UUID, val role: String) {
        fun grantTo(userId: UUID) = createUserRoleContext(userId, role, contextId)
    }

    private companion object {
        const val SOURCE_USER = "dummy_0@solyton.org"
        const val DESTINATION_USER = "dummy_1@solyton.org"
    }
}
