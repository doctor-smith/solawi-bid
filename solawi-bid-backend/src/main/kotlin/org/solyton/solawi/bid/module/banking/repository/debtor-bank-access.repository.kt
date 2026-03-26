package org.solyton.solawi.bid.module.banking.repository

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.banking.exception.BankAccountsException
import org.solyton.solawi.bid.module.banking.schema.DebtorBankAccessEntity
import org.solyton.solawi.bid.module.banking.schema.DebtorBankAccessTable
import org.solyton.solawi.bid.module.banking.schema.DebtorBankAccessType
import org.solyton.solawi.bid.module.banking.service.validatedBankAccount
import java.util.*

/**
 * Creates a new DebtorBankAccessEntity in the database with the specified details.
 *
 * @param bankAccountId The unique identifier of the bank account for which the access is created.
 * @param accessType The type of access to be granted (e.g., read, write).
 * @param accessToken The token required to authenticate access.
 * @param tokenExpiry The date and time when the token expires.
 * @param creatorId The unique identifier of the user initiating the creation.
 * @return A new instance of DebtorBankAccessEntity representing the created access.
 */
fun Transaction.createDebtorBankAccess(
    bankAccountId: UUID,
    accessType: DebtorBankAccessType,
    accessToken: String,
    tokenExpiry: DateTime,
    creatorId: UUID
): DebtorBankAccessEntity {
    val bankAccount = validatedBankAccount(bankAccountId)

    return DebtorBankAccessEntity.new {
        createdBy = creatorId
        this.bankAccount = bankAccount
        this.accessType = accessType
        this.accessToken = accessToken
        this.tokenExpiry = tokenExpiry
    }
}

/**
 * Retrieves the DebtorBankAccessEntity for a specific bank account ID and access type.
 *
 * @param bankAccountId The unique identifier of the bank account.
 * @param accessType The type of access being retrieved.
 * @return The matching DebtorBankAccessEntity if found.
 */
fun Transaction.readDebtorBankAccess(bankAccountId: UUID, accessType: DebtorBankAccessType): DebtorBankAccessEntity =
    validateDebtorBankAccess(bankAccountId, accessType)

/**
 * Retrieves all DebtorBankAccessEntity instances for the specified bank account ID.
 *
 * @param bankAccountId The unique identifier of the bank account.
 * @return A list of DebtorBankAccessEntity entries associated with the bank account.
 */
fun Transaction.readDebtorBankAccess(bankAccountId: UUID): List<DebtorBankAccessEntity> =
    DebtorBankAccessEntity.find { DebtorBankAccessTable.bankAccountId eq bankAccountId }.toList()

/**
 * Updates an existing DebtorBankAccessEntity with new details.
 *
 * @param bankAccountId The unique identifier of the bank account whose access is being updated.
 * @param accessType The type of access to be updated.
 * @param accessToken The new token for authentication.
 * @param tokenExpiry The new expiration date for the token.
 * @param modifierId The unique identifier of the user making the modification.
 * @return The updated DebtorBankAccessEntity.
 */
fun Transaction.updateDebtorBankAccess(
    bankAccountId: UUID,
    accessType: DebtorBankAccessType,
    accessToken: String,
    tokenExpiry: DateTime,
    modifierId: UUID
): DebtorBankAccessEntity {
    val access = validateDebtorBankAccess(bankAccountId, accessType)
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
 * Deletes a specific DebtorBankAccessEntity based on the bank account ID and access type.
 *
 * @param bankAccountId The unique identifier of the bank account.
 * @param accessType The type of access to delete.
 * @throws BankAccountsException.NoSuchDebtorBankAccountAccess If no matching access is found for deletion.
 */
fun Transaction.deleteDebtorBankAccess(bankAccountId: UUID, accessType: DebtorBankAccessType) =
    DebtorBankAccessEntity.find {
        DebtorBankAccessTable.bankAccountId eq bankAccountId and (DebtorBankAccessTable.accessType eq accessType)
    }.firstOrNull()?.delete() ?: throw BankAccountsException.NoSuchDebtorBankAccountAccess(bankAccountId.toString())


/**
 * Validates the existence of a DebtorBankAccessEntity by bank account ID and access type.
 *
 * @param bankAccountId The unique identifier of the bank account.
 * @param accessType The type of access being validated.
 * @return The corresponding DebtorBankAccessEntity if found.
 * @throws BankAccountsException.NoSuchDebtorBankAccountAccess If no matching access is found.
 */
fun Transaction.validateDebtorBankAccess(bankAccountId: UUID, accessType: DebtorBankAccessType): DebtorBankAccessEntity {
    val access =  DebtorBankAccessEntity.find {
        DebtorBankAccessTable.bankAccountId eq bankAccountId and (DebtorBankAccessTable.accessType eq accessType)
    }.firstOrNull() ?: throw BankAccountsException.NoSuchDebtorBankAccountAccess(bankAccountId.toString())
    return access
}
