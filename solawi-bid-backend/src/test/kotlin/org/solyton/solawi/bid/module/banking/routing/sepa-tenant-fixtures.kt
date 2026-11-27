package org.solyton.solawi.bid.module.banking.routing

import java.util.UUID
import kotlinx.datetime.LocalDate
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate as JodaLocalDate
import org.solyton.solawi.bid.module.application.routing.*
import org.solyton.solawi.bid.module.banking.application.BANKING_APPLICATION_NAME
import org.solyton.solawi.bid.module.banking.data.*
import org.solyton.solawi.bid.module.banking.repository.*
import org.solyton.solawi.bid.module.banking.schema.*
import org.solyton.solawi.bid.module.user.repository.createAddress
import org.solyton.solawi.bid.module.values.LegalEntityId

internal fun Transaction.seedBanking(): SepaFixture {
    SchemaUtils.createMissingTablesAndColumns(
        SepaMandateCollectionsTable, SepaMandateDataMappingsTable, SepaCollectionMappings,
        SepaPaymentStatusHistory, SepaPaymentLinksTable, BankAccountAccessorsTable
    )
    val providers = createProviders(BANKING_APPLICATION_NAME, "MANAGE_BANK_TRANSACTIONS", "MANAGE_BANK_TRANSACTIONS")
    return SepaFixture(
        providers,
        createProviderBanking(providers.source, providers.sourceUserId, "source"),
        createProviderBanking(providers.destination, findUserId(DESTINATION_USER), "destination")
    )
}

private fun Transaction.createProviderBanking(provider: Provider, debtorUserId: UUID, name: String): ProviderBanking {
    val date = DateTime(2026, 12, 1, 0, 0, DateTimeZone.UTC)
    val address = createAddress(name, name, "Test street", "", "Test city", "", "00000", "DE", UUID_ZERO)
    val entity = createLegalEntity(
        provider.organizationId, name, "Test organization", LegalEntityType.ORGANIZATION, address.id.value, UUID_ZERO
    )
    val creditor = CreditorIdentifierEntity.new {
        legalEntity = entity
        creditorId = "test-creditor-$name"
        validFrom = date
        createdBy = UUID_ZERO
    }
    val account = BankAccountEntity.new {
        userId = provider.organizationId
        iban = "test-iban"
        bic = "test-bic"
        accountHolder = name
        accountType = AccountType.CREDITOR
        createdBy = UUID_ZERO
    }
    val debtorAccount = BankAccountEntity.new {
        userId = debtorUserId
        iban = "test-debtor-iban"
        bic = "test-bic"
        accountHolder = "Test debtor"
        accountType = AccountType.DEBTOR
        createdBy = UUID_ZERO
    }
    createBankAccountAccessor(entity.id.value, debtorAccount)
    val creditorId = CreditorIdentifierId(creditor.id.value.toString())
    val accountId = BankAccountId(account.id.value.toString())
    val collection = createSepaCollection(
        UUID_ZERO, creditorId, accountId, MandateReferencePrefix("TEST"), SepaCollectionKey(name),
        RemittanceInformation("Test"), SepaSequenceType.FRST, null
    )
    val mandate = createSepaMandateWithRetry(
        UUID_ZERO, creditor.id.value, debtorAccount.id.value, "Test debtor", date, date, null,
        mandateReference = MandateReference("mandate-$name"), collectionId = collection.id.value
    )
    val payment = createPayment(
        UUID_ZERO, mandate.id.value, collection.id.value, 12.0,
        JodaLocalDate(2026, 12, 1)
    )
    val message = SepaMessageEntity.new {
        creditorIdentifier = creditor
        creditorAccount = account
        messageId = "fixture-message-$name"
        executionDate = date
        createdBy = UUID_ZERO
    }
    return ProviderBanking(
        LegalEntityId(entity.id.value.toString()), creditorId, CreditorId(creditor.creditorId), accountId,
        BankAccountId(debtorAccount.id.value.toString()), SepaCollectionId(collection.id.value.toString()),
        SepaMandateId(mandate.id.value.toString()), SepaPaymentId(payment.id.value.toString()),
        SepaMessageId(message.id.value.toString())
    )
}

internal data class SepaFixture(val providers: Providers, val source: ProviderBanking, val destination: ProviderBanking)

internal data class ProviderBanking(
    val legalEntityId: LegalEntityId,
    val creditorId: CreditorIdentifierId,
    val creditorReference: CreditorId,
    val accountId: BankAccountId,
    val debtorAccountId: BankAccountId,
    val collectionId: SepaCollectionId,
    val mandateId: SepaMandateId,
    val paymentId: SepaPaymentId,
    val messageId: SepaMessageId
)

internal val executionDate = LocalDate(2026, 12, 1)
