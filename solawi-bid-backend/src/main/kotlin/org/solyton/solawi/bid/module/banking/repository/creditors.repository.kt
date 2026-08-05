package org.solyton.solawi.bid.module.banking.repository

import kotlinx.datetime.LocalDate
import org.evoleq.exposedx.joda.toJoda
import org.evoleq.kotlinx.date.toDateTime
import org.jetbrains.exposed.sql.Transaction
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.banking.data.CreditorId
import org.solyton.solawi.bid.module.banking.data.CreditorIdentifierId
import org.solyton.solawi.bid.module.banking.exception.BankAccountsException
import org.solyton.solawi.bid.module.banking.schema.CreditorIdentifierEntity
import org.solyton.solawi.bid.module.banking.schema.CreditorIdentifiersTable
import org.solyton.solawi.bid.module.values.LegalEntityId
import java.util.*

fun Transaction.validatedCreditorIdentifier(id: UUID): CreditorIdentifierEntity =
    CreditorIdentifierEntity.findById(id)?: throw BankAccountsException.NoSuchCreditorIdentifier(id.toString())

fun Transaction.validatedCreditorIdentifier(id: CreditorId): CreditorIdentifierEntity =
    CreditorIdentifierEntity.find{
        CreditorIdentifiersTable.creditorId eq id.value
    }.firstOrNull()?: throw BankAccountsException.NoSuchCreditor(id.toString())

fun Transaction.createCreditorIdentifier(
    creatorId: UUID,
    creditorId: CreditorId,
    legalEntityId: LegalEntityId,
    validFrom: LocalDate,
    validTo: LocalDate? = null,
    isActive: Boolean = true
): CreditorIdentifierEntity {

    val legalEntity = validatedLegalEntity(UUID.fromString(legalEntityId.value))

    return CreditorIdentifierEntity.new {
        createdBy = creatorId

        this.creditorId = creditorId.value
        this.legalEntity = legalEntity
        this.validFrom = validFrom.toDateTime().toJoda()
        this.validUntil = validTo?.toDateTime()?.toJoda()
        this.isActive = isActive
    }
}

fun Transaction.updateCreditorIdentifier(
    modifierId: UUID,
    creditorIdentifierId: CreditorIdentifierId,
    creditorId: CreditorId,
    legalEntityId: LegalEntityId,
    validFrom: LocalDate,
    validUntil: LocalDate? = null,
    isActive: Boolean = true
): CreditorIdentifierEntity {
    val creditorIdentifier = validatedCreditorIdentifier(UUID.fromString(creditorIdentifierId.value))
    val legalEntity = validatedLegalEntity(UUID.fromString(legalEntityId.value))

    val creditorIdChanged = creditorId.value != creditorIdentifier.creditorId
    val legalEntityIdChanged = legalEntityId.value != creditorIdentifier.legalEntity.id.value.toString()
    val validFromChanged = validFrom != creditorIdentifier.validFrom.toLocalDate()
    val validUntilChanged = validUntil != creditorIdentifier.validUntil?.toLocalDate()
    val isActiveChanged = isActive != creditorIdentifier.isActive

    val changed = creditorIdChanged ||
            legalEntityIdChanged ||
            validFromChanged ||
            validUntilChanged ||
            isActiveChanged

    if(creditorIdChanged) creditorIdentifier.creditorId = creditorId.value
    if(legalEntityIdChanged) creditorIdentifier.legalEntity = legalEntity
    if(validFromChanged) creditorIdentifier.validFrom = validFrom.toDateTime().toJoda()
    if(validUntilChanged) creditorIdentifier.validUntil = validUntil?.toDateTime()?.toJoda()
    if(isActiveChanged) creditorIdentifier.isActive = isActive

    if(changed) {
        creditorIdentifier.modifiedBy = modifierId
        creditorIdentifier.modifiedAt = DateTime.now()
    }

    return creditorIdentifier
}
