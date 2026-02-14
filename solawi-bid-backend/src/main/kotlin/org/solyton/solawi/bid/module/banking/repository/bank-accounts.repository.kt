package org.solyton.solawi.bid.module.banking.repository

import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.solyton.solawi.bid.module.banking.data.BIC
import org.solyton.solawi.bid.module.banking.data.BankAccountId
import org.solyton.solawi.bid.module.banking.data.IBAN
import org.solyton.solawi.bid.module.banking.data.api.CreateBankAccount
import org.solyton.solawi.bid.module.banking.data.toUUID
import org.solyton.solawi.bid.module.banking.schema.BankAccountAccessorEntity
import org.solyton.solawi.bid.module.banking.schema.BankAccountAccessorsTable
import org.solyton.solawi.bid.module.banking.schema.BankAccountEntity
import org.solyton.solawi.bid.module.banking.schema.BankAccountsTable
import org.solyton.solawi.bid.module.banking.service.validateBic
import org.solyton.solawi.bid.module.banking.service.validateIban
import org.solyton.solawi.bid.module.banking.service.validateUserExists
import org.solyton.solawi.bid.module.banking.service.validatedBankAccount
import org.solyton.solawil.bid.module.bid.data.api.toUUID
import java.util.*

/**
 * Creates a new bank account for a user with the provided details.
 * Ensures the IBAN and BIC are valid and verifies that the user exists before creating the bank account entity.
 *
 * @param userId The unique identifier of the user to associate with this bank account.
 * @param iban The International Bank Account Number (IBAN) for the bank account.
 * @param bic The Bank Identifier Code (BIC) for the bank account.
 * @param creatorId The unique identifier of the user or process creating this bank account.
 * @return The newly created bank account entity.
 * @throws org.solyton.solawi.bid.module.banking.exception.BankAccountsException.InvalidIban If the provided IBAN is invalid.
 * @throws org.solyton.solawi.bid.module.banking.exception.BankAccountsException.InvalidBic If the provided BIC is invalid.
 * @throws org.solyton.solawi.bid.module.user.exception.UserManagementException.UserDoesNotExist If the user associated with the userId does not exist.
 */
fun Transaction.createBankAccount(
    userId: UUID,
    iban: IBAN,
    bic: BIC,
    creatorId: UUID
) : BankAccountEntity {
    validateBic(bic.value)
    validateIban(iban)
    validateUserExists(userId)

    val bankAccount = BankAccountEntity.new {
        this.createdBy = creatorId
        this.userId = userId
        this.iban = iban.value
        this.bic = bic.value
    }
    return bankAccount
}

/**
 * Retrieves a bank account associated with the specified account ID, ensuring it exists.
 *
 * @param bankAccountId The unique identifier of the bank account to be retrieved.
 * @return The bank account entity corresponding to the provided bank account ID.
 * @throws BankAccountsException.NoSuchBankAccount If no bank account exists with the specified ID.
 */
fun Transaction.readBankAccount(bankAccountId: UUID) : BankAccountEntity = validatedBankAccount(bankAccountId)

/**
 * Reads the list of bank accounts associated with a specified legal entity.
 *
 * @param legalEntityId The unique identifier of the legal entity whose associated bank accounts
 *                      are to be retrieved.
 * @return A list of bank account entities that are associated with the given legal entity.
 */
fun Transaction.readBankAccountsByLegalEntity(legalEntityId: UUID) : List<BankAccountEntity> {
    val query = BankAccountsTable
        .join(
            BankAccountAccessorsTable, JoinType.INNER,
            BankAccountsTable.id,
            BankAccountAccessorsTable.bankAccountId
        )
        .selectAll()
        .where { BankAccountAccessorsTable.accessorId eq legalEntityId }

    return BankAccountEntity.wrapRows(query).toList()
}

/**
 * Updates the details of an existing bank account with new information.
 *
 * @param bankAccountId The unique identifier of the bank account to be updated.
 * @param userId The unique identifier of the user associated with the bank account.
 * @param iban The International Bank Account Number (IBAN) of the bank account.
 * @param bic The Bank Identifier Code (BIC) of the bank account.
 * @param modifierId The unique identifier of the entity making the modification.
 * @return The updated bank account entity.
 */
fun Transaction.updateBankAccount(
    bankAccountId: UUID,
    userId: UUID,
    iban: IBAN,
    bic: BIC,
    modifierId: UUID
) : BankAccountEntity {
    val bankAccount = validatedBankAccount(bankAccountId)
    validateBic(bic.value)
    validateIban(iban)
    validateUserExists(userId)
    
    val changed = bankAccount.userId != userId
        || bankAccount.iban != iban.value
        || bankAccount.bic != bic.value

    bankAccount.userId = userId
    bankAccount.iban = iban.value
    bankAccount.bic = bic.value

    if(changed) {
        bankAccount.modifiedBy = modifierId
        bankAccount.modifiedAt = org.joda.time.DateTime.now()
    }
    
    return bankAccount
}

/**
 * Deletes a bank account by its unique identifier.
 *
 * @param bankAccountId The unique identifier (UUID) of the bank account to be deleted.
 * @return The unique identifier (UUID) of the deleted bank account.
 * @throws org.solyton.solawi.bid.module.banking.exception.BankAccountsException.NoSuchBankAccount If the specified bank account does not exist.
 */
fun Transaction.deleteBankAccount(bankAccountId: UUID) : UUID {
    val bankAccount = validatedBankAccount(bankAccountId)
    bankAccount.delete()
    return bankAccountId
}

/**
 * Imports a list of bank accounts by either creating new accounts or updating existing ones
 * based on the provided data. Associates the accounts with a specified accessor.
 *
 * @param accessorId The unique identifier of the accessor for which the bank accounts are being imported.
 * @param accounts A list of bank accounts to be imported consisting of user ID, IBAN, and BIC details.
 */
fun Transaction.importBankAccounts(
    accessorId: UUID, 
    accounts: List<CreateBankAccount>
): List<BankAccountEntity> {
    val existingAccountsMap = readBankAccountsByLegalEntity(accessorId)
        .associateBy { it.userId.toString() }

    val (toUpdate, toCreate) = accounts.partition {
        it.userId.value in existingAccountsMap
    }

    val created = toCreate.map { bankAccount ->
        with(bankAccount) {
            val userId = userId.toUUID()
            val newBankAccount = createBankAccount(
                userId,
                iban,
                bic,
                accessorId
            )
            createBankAccountAccessor(accessorId, newBankAccount)
            newBankAccount
        }
    }
    
    val updated = toUpdate.map { bankAccount ->
        val existingBankAccount = requireNotNull(existingAccountsMap[bankAccount.userId.value]) {
            "Bank account for user ${bankAccount.userId.value} does not exist" 
        }
        updateBankAccount(
            existingBankAccount.id.value,
            bankAccount.userId.toUUID(),
            bankAccount.iban,
            bankAccount.bic,
            accessorId
        )
    }
    
    return created + updated
}

/**
 * Creates a new bank account accessor and associates it with the specified bank account.
 *
 * @param accessorId The unique identifier of the accessor being associated with the bank account.
 * @param bankAccount The bank account entity to associate with the accessor.
 * @return A new instance of BankAccountAccessorEntity representing the association.
 */
fun Transaction.createBankAccountAccessor(
    accessorId: UUID,
    bankAccount: BankAccountEntity
): BankAccountAccessorEntity = BankAccountAccessorEntity.new {
    this.bankAccount = bankAccount
    this.accessorId = accessorId
}

/**
 * Deletes a bank account by its unique identifier.
 *
 * @param bankAccountId The identifier of the bank account to be deleted.
 * @return A boolean value indicating whether the deletion was successful. Returns `true` if the bank account
 *         was deleted, or `false` if no such account existed.
 */
fun Transaction.deleteBankAccount(bankAccountId: BankAccountId): Boolean {
    val uuid = bankAccountId.toUUID()
    BankAccountAccessorsTable.deleteWhere { BankAccountAccessorsTable.bankAccountId eq uuid }
    val accountDeleted = BankAccountsTable.deleteWhere { BankAccountsTable.id eq uuid }
    return accountDeleted > 0
}
