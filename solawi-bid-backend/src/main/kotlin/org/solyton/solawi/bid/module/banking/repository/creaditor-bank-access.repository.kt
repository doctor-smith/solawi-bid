package org.solyton.solawi.bid.module.banking.repository

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.banking.exception.BankAccountsException
import org.solyton.solawi.bid.module.banking.schema.CreditorBankAccessEntity
import org.solyton.solawi.bid.module.banking.schema.CreditorBankAccessTable
import org.solyton.solawi.bid.module.banking.schema.CreditorBankAccessType
import org.solyton.solawi.bid.module.banking.service.validatedBankAccount
import java.util.*

/**
 * Creates a new creditor bank access entry in the database associated with a specific bank account.
 *
 * @param bankAccountId The unique identifier of the bank account.
 * @param accessType The type of access being granted (e.g., read, write).
 * @param accessToken The token used for accessing the bank account.
 * @param tokenExpiry The expiration date and time of the access token.
 * @param creatorId The unique identifier of the entity creating this access entry.
 * @return A new `CreditorBankAccessEntity` representing the created access entry.
 */
fun Transaction.createCreditorBankAccess(
    bankAccountId: UUID,
    accessType: CreditorBankAccessType,
    accessToken: String,
    tokenExpiry: DateTime,
    creatorId: UUID
): CreditorBankAccessEntity {
    val bankAccount = validatedBankAccount(bankAccountId)

    return CreditorBankAccessEntity.new {
        createdBy = creatorId
        this.bankAccount = bankAccount
        this.accessType = accessType
        this.accessToken = accessToken
        this.tokenExpiry = tokenExpiry
    }
}

/**
 * Retrieves a specific creditor bank access entry based on the bank account ID and access type.
 *
 * @param bankAccountId The unique identifier of the bank account.
 * @param accessType The type of access to retrieve.
 * @return The existing `CreditorBankAccessEntity` that matches the provided criteria.
 * @throws BankAccountsException.NoSuchCreditorBankAccountAccess If no matching access entry is found.
 */
fun Transaction.readCreditorBankAccess(bankAccountId: UUID, accessType: CreditorBankAccessType): CreditorBankAccessEntity =
    validateCreditorBankAccess(bankAccountId, accessType)

/**
 * Retrieves all creditor bank access entries associated with a specific bank account.
 *
 * @param bankAccountId The unique identifier of the bank account.
 * @return A list of `CreditorBankAccessEntity` entries associated with the bank account.
 */
fun Transaction.readCreditorBankAccess(bankAccountId: UUID): List<CreditorBankAccessEntity> =
    CreditorBankAccessEntity.find { CreditorBankAccessTable.bankAccountId eq bankAccountId }.toList()

/**
 * Updates an existing creditor bank access entry with new details.
 *
 * @param bankAccountId The unique identifier of the bank account.
 * @param accessType The type of access to be updated.
 * @param accessToken The new token for accessing the bank account.
 * @param tokenExpiry The new expiration date and time of the access token.
 * @param modifierId The unique identifier of the entity making the modifications.
 * @return The updated `CreditorBankAccessEntity` instance.
 * @throws BankAccountsException.NoSuchCreditorBankAccountAccess If the specified access entry does not exist.
 */
fun Transaction.updateCreditorBankAccess(
    bankAccountId: UUID,
    accessType: CreditorBankAccessType,
    accessToken: String,
    tokenExpiry: DateTime,
    modifierId: UUID
): CreditorBankAccessEntity {
    val access = validateCreditorBankAccess(bankAccountId, accessType)
    val bankAccount = validatedBankAccount(bankAccountId)
    val changed = access.bankAccount != bankAccount || access.accessToken != accessToken || access.tokenExpiry != tokenExpiry

    access.bankAccount = bankAccount
    access.accessToken = accessToken
    access.tokenExpiry = tokenExpiry

    if(changed) {
        access.modifiedBy = modifierId
        access.modifiedAt = DateTime.now()
    }

    return access
}


/**
 * Deletes a specific creditor bank access entry based on the bank account ID and access type.
 *
 * @param bankAccountId The unique identifier of the bank account.
 * @param accessType The type of access to be deleted.
 * @throws BankAccountsException.NoSuchCreditorBankAccountAccess If no matching access entry is found.
 */
fun Transaction.deleteCreditorBankAccess(bankAccountId: UUID, accessType: CreditorBankAccessType) =
    CreditorBankAccessEntity.find {
        CreditorBankAccessTable.bankAccountId eq bankAccountId and (CreditorBankAccessTable.accessType eq accessType)
    }.firstOrNull()?.delete() ?: throw BankAccountsException.NoSuchCreditorBankAccountAccess(bankAccountId.toString())


/**
 * Validates the existence of a specific creditor bank access entry.
 *
 * @param bankAccountId The unique identifier of the bank account.
 * @param accessType The type of access to validate.
 * @return The `CreditorBankAccessEntity` if it exists.
 * @throws BankAccountsException.NoSuchCreditorBankAccountAccess If no matching access entry is found.
 */
fun Transaction.validateCreditorBankAccess(bankAccountId: UUID, accessType: CreditorBankAccessType): CreditorBankAccessEntity {
    val access =  CreditorBankAccessEntity.find {
        CreditorBankAccessTable.bankAccountId eq bankAccountId and (CreditorBankAccessTable.accessType eq accessType)
    }.firstOrNull() ?: throw BankAccountsException.NoSuchCreditorBankAccountAccess(bankAccountId.toString())
    return access
}
