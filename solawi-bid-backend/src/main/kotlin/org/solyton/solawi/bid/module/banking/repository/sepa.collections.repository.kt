package org.solyton.solawi.bid.module.banking.repository

import org.jetbrains.exposed.sql.Transaction
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.banking.data.*
import org.solyton.solawi.bid.module.banking.data.api.ApiSepaCollections
import org.solyton.solawi.bid.module.banking.exception.SepaException
import org.solyton.solawi.bid.module.banking.schema.CreditorIdentifierEntity
import org.solyton.solawi.bid.module.banking.schema.CreditorIdentifiersTable
import org.solyton.solawi.bid.module.banking.schema.SepaCollectionEntity
import org.solyton.solawi.bid.module.banking.schema.SepaCollectionMapping
import org.solyton.solawi.bid.module.banking.schema.SepaCollectionsTable
import org.solyton.solawi.bid.module.banking.schema.SepaSequenceType
import org.solyton.solawi.bid.module.banking.service.validatedBankAccount
import java.util.*

@Suppress("NoNameShadowing")
fun Transaction.createSepaCollection(
    creator: UUID,
    creditorIdentifierId: CreditorIdentifierId,
    creditorAccountId: BankAccountId,
    mandateReferencePrefix: MandateReferencePrefix,
    remittanceInformation: RemittanceInformation,
    sepaSequenceType: SepaSequenceType,
    localInstrument: LocalInstrument?,
    chargeBearer: ChargeBearer = ChargeBearer("SLEV"),
    leadTimesDays: Int = 2,
    purposeCode: PurposeCode? = null,
    retryOnFailure: Boolean = false,
    maxRetries: Int = 0,
    isActive: Boolean = true,
    referenceIds: List<SepaCollectionReferenceId> = emptyList()

    ): SepaCollectionEntity {

    val creditorIdentifierId = UUID.fromString(creditorIdentifierId.value)
    val creditorAccountId = UUID.fromString(creditorAccountId.value)

    val creditorIdentifier = validatedCreditorIdentifier(creditorIdentifierId)
    val creditorAccount = validatedBankAccount(creditorAccountId)
    val mandateReferencePrefix = mandateReferencePrefix.value
    val remittanceInformation = remittanceInformation.value
    val localInstrument = localInstrument?.value
    val chargeBearer = chargeBearer.value
    val purposeCode = purposeCode?.value

    val collection =  SepaCollectionEntity.new {
        createdBy = creator
        this.creditorIdentifier = creditorIdentifier
        this.creditorAccount = creditorAccount
        this.remittanceInformation = remittanceInformation
        this.mandateReferencePrefix = mandateReferencePrefix
        this.sequenceType = sepaSequenceType
        this.localInstrument = localInstrument
        this.chargeBearer = chargeBearer
        this.leadTimesDays = leadTimesDays
        this.purposeCode = purposeCode
        this.retryOnFailure = retryOnFailure
        this.maxRetries = maxRetries
        this.isActive = isActive
    }

    referenceIds.map { referenceId ->
        SepaCollectionMapping.new{
            sepaCollection = collection
            this.referenceId = UUID.fromString(referenceId.value)
        }
    }

    return collection
}


fun Transaction.readSepaCollectionsByLegalEntity(legalEntityId: UUID): ApiSepaCollections {
    validatedLegalEntity(legalEntityId)

    val creditorIdentifiers = CreditorIdentifierEntity.find{
        CreditorIdentifiersTable.legalEntityId eq legalEntityId
    }.toList()

    val creditorIdentifierIds = creditorIdentifiers.map {it.id.value}

    val collections = SepaCollectionEntity.find {
        SepaCollectionsTable.creditorIdentifierId inList creditorIdentifierIds
    }

    return ApiSepaCollections(collections.map{it.toApiType() })
}

@Suppress("CyclomaticComplexMethod", "NoNameShadowing")
fun Transaction.updateSepaCollection(
    modifier: UUID,
    sepaCollectionId: SepaCollectionId,
    creditorIdentifierId: CreditorIdentifierId,
    creditorAccountId: BankAccountId,
    mandateReferencePrefix: MandateReferencePrefix,
    remittanceInformation: RemittanceInformation,
    sepaSequenceType: SepaSequenceType,
    localInstrument: LocalInstrument?,
    chargeBearer: ChargeBearer = ChargeBearer("SLEV"),
    leadTimesDays: Int = 2,
    purposeCode: PurposeCode? = null,
    // retryOnFailure: Boolean = false,
    // maxRetries: Int = 0,
    isActive: Boolean = true,
): SepaCollectionEntity {
    val sepaCollectionId = UUID.fromString(sepaCollectionId.value)
    val creditorIdentifierId = UUID.fromString(creditorIdentifierId.value)
    val creditorAccountId = UUID.fromString(creditorAccountId.value)


    val sepaCollection = validatedSepaCollection(sepaCollectionId)
    val creditorIdentifier = validatedCreditorIdentifier(creditorIdentifierId)
    val creditorAccount = validatedBankAccount(creditorAccountId)
    val mandateReferencePrefix = mandateReferencePrefix.value
    val remittanceInformation = remittanceInformation.value
    val localInstrument = localInstrument?.value
    val chargeBearer = chargeBearer.value
    val purposeCode = purposeCode?.value

    val creditorIdentifierChanged = creditorIdentifier != sepaCollection.creditorIdentifier
    val creditorAccountChanged = creditorAccount != sepaCollection.creditorAccount
    val mandateReferencePrefixChanged = mandateReferencePrefix != sepaCollection.mandateReferencePrefix
    val remittanceInformationChanged = remittanceInformation != sepaCollection.remittanceInformation
    val localInstrumentChanged = localInstrument != sepaCollection.localInstrument
    val chargeBearerChanged = chargeBearer != sepaCollection.chargeBearer
    val purposeCodeChanged = purposeCode != sepaCollection.purposeCode
    val sepaSequenceTypeChanged = sepaSequenceType != sepaCollection.sequenceType
    val leadTimesDaysChanged = leadTimesDays != sepaCollection.leadTimesDays
    val isActiveChanged = isActive != sepaCollection.isActive

    if(creditorIdentifierChanged) {
        sepaCollection.creditorIdentifier = creditorIdentifier
    }
    if(creditorAccountChanged) {
        sepaCollection.creditorAccount = creditorAccount
    }
    if(mandateReferencePrefixChanged) {
        sepaCollection.mandateReferencePrefix = mandateReferencePrefix
    }
    if(remittanceInformationChanged) {
        sepaCollection.remittanceInformation = remittanceInformation
    }
    if(localInstrumentChanged) {
        sepaCollection.localInstrument = localInstrument
    }
    if(chargeBearerChanged) {
        sepaCollection.chargeBearer = chargeBearer
    }
    if(purposeCodeChanged) {
        sepaCollection.purposeCode = purposeCode
    }
    if(sepaSequenceTypeChanged) {
        sepaCollection.sequenceType = sepaSequenceType
    }
    if(leadTimesDaysChanged) {
        sepaCollection.leadTimesDays = leadTimesDays
    }
    if(isActiveChanged) {
        sepaCollection.isActive = isActive
    }
    val changed = creditorIdentifierChanged
            || creditorAccountChanged
            || mandateReferencePrefixChanged
            || remittanceInformationChanged
            || localInstrumentChanged
            || chargeBearerChanged
            || purposeCodeChanged
            || sepaSequenceTypeChanged
            || leadTimesDaysChanged
            || isActiveChanged

    if(changed) {
        sepaCollection.modifiedBy = modifier
        sepaCollection.modifiedAt = DateTime.now()
    }

    return sepaCollection
}

fun Transaction.validatedSepaCollection(id: UUID): SepaCollectionEntity =
    SepaCollectionEntity.findById(id)?: throw SepaException.NoSuchSepaCollection(id.toString())
