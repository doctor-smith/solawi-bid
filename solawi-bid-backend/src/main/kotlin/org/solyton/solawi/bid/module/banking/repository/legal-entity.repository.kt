package org.solyton.solawi.bid.module.banking.repository

import org.evoleq.exposedx.joda.now
import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.banking.exception.LegalEntityException
import org.solyton.solawi.bid.module.banking.schema.LegalEntity
import org.solyton.solawi.bid.module.banking.schema.LegalEntityType
import org.solyton.solawi.bid.module.user.repository.validatedAddress
import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import org.solyton.solawi.bid.module.user.schema.UserEntity
import java.util.UUID

/**
 * Creates a new legal entity with the specified details in the database.
 *
 * @param name The name of the legal entity to be created.
 * @param legalForm The legal form of the entity (e.g., LLC, Inc.).
 * @param addressId The unique identifier of the address to be associated with the legal entity.
 * @param creatorId The unique identifier of the creator or user initiating the creation of the legal entity.
 * @throws AddressException.NoSuchAddress If the specified addressId does not exist in the database.
 */
fun Transaction.createLegalEntity(
    partyId: UUID,
    name: String,
    legalForm: String,
    legalEntityType: LegalEntityType,
    addressId: UUID,
    creatorId: UUID
) {
    validateIsUserOrOrganization(partyId)
    val address = validatedAddress(addressId)

    LegalEntity.new {
        createdBy = creatorId
        this.partyId = partyId
        this.name = name
        this.legalForm = legalForm
        this.legalEntityType = legalEntityType
        this.address = address
    }
}

/**
 * Reads a LegalEntity based on the provided unique identifier.
 *
 * @param id The unique identifier of the LegalEntity to retrieve.
 * @return The LegalEntity associated with the given identifier, if found.
 */
fun Transaction.readLegalEntity(id: UUID) = LegalEntity.findById(id)

/**
 * Updates the legal entity with new details including its name, legal form, and address.
 * If any changes are made, the modified timestamp and modifier ID are updated.
 *
 * @param id The unique identifier of the legal entity to update.
 * @param name The new name for the legal entity.
 * @param legalForm The new legal form for the legal entity.
 * @param addressId The unique identifier of the new address associated with the legal entity.
 * @param modifierId The unique identifier of the user or process that performs the update.
 * @return The updated legal entity.
 */
fun Transaction.updateLegalEntity(
    id: UUID,
    partyId: UUID,
    name: String,
    legalForm: String,
    legalEntityType: LegalEntityType,
    addressId: UUID,
    modifierId: UUID
): LegalEntity {
    val address = validatedAddress(addressId)

    val legalEntity = validatedLegalEntity(id)
    validateIsUserOrOrganization(partyId)
    val changed = legalEntity.name != name
            || partyId != legalEntity.partyId
            || legalEntity.legalForm != legalForm
            || legalEntity.legalEntityType != legalEntityType
            || legalEntity.address != address

    legalEntity.name = name
    legalEntity.partyId = partyId
    legalEntity.legalForm = legalForm
    legalEntity.legalEntityType = legalEntityType
    legalEntity.address = address

    if(changed) {
        legalEntity.modifiedBy = modifierId
        legalEntity.modifiedAt = now()
    }

    return legalEntity
}

/**
 * Deletes a legal entity from the database using its unique identifier.
 * If the specified legal entity does not exist, a `LegalEntityException.NoSuchLegalEntity` is thrown.
 *
 * @param id The unique identifier of the legal entity to be deleted.
 * @throws LegalEntityException.NoSuchLegalEntity If no legal entity is found with the specified identifier.
 */
fun Transaction.deleteLegalEntity(id: UUID) = LegalEntity.findById(id)?.delete() ?: throw LegalEntityException.NoSuchLegalEntity(id.toString())

/**
 * Validates the existence of a legal entity by its unique identifier.
 *
 * If a legal entity with the provided ID is not found, an exception is thrown.
 *
 * @param id The unique identifier of the legal entity to be validated.
 * @return The `LegalEntity` instance corresponding to the provided ID if found.
 * @throws LegalEntityException.NoSuchLegalEntity If no legal entity exists with the provided ID.
 */
fun Transaction.validatedLegalEntity(id: UUID) = LegalEntity.findById(id) ?: throw LegalEntityException.NoSuchLegalEntity(id.toString())

fun Transaction.validateIsUserOrOrganization(id: UUID): Boolean {
    UserEntity.findById(id) ?: OrganizationEntity.findById(id) ?: throw LegalEntityException.NoSuchLegalEntity(id.toString())
    return true
}
