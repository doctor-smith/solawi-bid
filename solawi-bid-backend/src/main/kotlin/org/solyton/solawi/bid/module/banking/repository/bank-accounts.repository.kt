package org.solyton.solawi.bid.module.banking.repository

import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.solyton.solawi.bid.module.banking.data.BIC
import org.solyton.solawi.bid.module.banking.data.BankAccountId
import org.solyton.solawi.bid.module.banking.data.IBAN
import org.solyton.solawi.bid.module.banking.data.api.ImportBankAccount
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.banking.data.toUUID
import org.solyton.solawi.bid.module.banking.schema.AccountType
import org.solyton.solawi.bid.module.banking.schema.BankAccountAccessorEntity
import org.solyton.solawi.bid.module.banking.schema.BankAccountAccessorsTable
import org.solyton.solawi.bid.module.banking.schema.BankAccountEntity
import org.solyton.solawi.bid.module.banking.schema.BankAccounts.accountHolder
import org.solyton.solawi.bid.module.banking.schema.BankAccountsTable
import org.solyton.solawi.bid.module.banking.service.validateBic
import org.solyton.solawi.bid.module.banking.service.validateIban
import org.solyton.solawi.bid.module.banking.service.validatedBankAccount
import org.solyton.solawi.bid.module.user.data.api.ApiUser
import org.solyton.solawi.bid.module.user.data.api.CreateUser
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UsersTable
import org.solyton.solawi.bid.module.user.service.user.createUser
import org.solyton.solawi.bid.module.user.service.validateUserExists
import java.util.*

/**
 * Creates a new bank account for a user with the provided details.
 * Ensures the IBAN and BIC are valid and verifies that the user exists before creating the bank account entity.
 *
 * @param userId The unique identifier of the user to associate with this bank account.
 * @param iban The International Bank Account Number (IBAN) for the bank account.
 * @param bic The Bank Identifier Code (BIC) for the bank account.
 * @param accountHolder The name of the bank account holder. Defaults to an empty string if not provided.
 * @param isActive A flag indicating whether the bank account is active. Defaults to true.
 * @param accountType The type of the bank account (e.g., DEBTOR). Defaults to `DEBTOR` if not provided.
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
    accountHolder: String = "",
    isActive: Boolean = true,
    accountType: AccountType = AccountType.DEBTOR,
    accessors: List<UUID> = emptyList(),
    description: String? = null,
    creatorId: UUID
) : BankAccountEntity {
    if(bic.value.isNotBlank()) validateBic(bic.value)
    validateIban(iban)
    validateIsUserOrOrganization(userId)

    val bankAccount = BankAccountEntity.new {
        this.createdBy = creatorId
        this.userId = userId
        this.iban = iban.value
        this.bic = bic.value.trim()
        this.accountHolder = accountHolder
        this.isActive = isActive
        this.accountType = accountType
        if(description != null) this.description = description
    }

    BankAccountAccessorEntity.new {
        this.bankAccount = bankAccount
        this.accessorId = userId
    }

    accessors.filter { it != userId }.forEach { accessorId ->
        BankAccountAccessorEntity.new {
            this.bankAccount = bankAccount
            this.accessorId = accessorId
        }
    }

    return bankAccount
}

/**
 * Retrieves a bank account associated with the specified unique account ID, ensuring it exists.
 *
 * @param bankAccountId The unique identifier (UUID) of the bank account to be retrieved.
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
 * Optional parameters such as accountHolder, isActive, and accountType can be set to modify respective values.
 * Automatically updates the `modifiedBy` and `modifiedAt` attributes if any information changes.
 *
 * @param bankAccountId The unique identifier of the bank account to be updated.
 * @param userId The unique identifier of the user associated with the bank account.
 * @param iban The International Bank Account Number (IBAN) of the bank account.
 * @param bic The Bank Identifier Code (BIC) of the bank account.
 * @param accountHolder The name of the bank account holder. Defaults to an empty string.
 * @param isActive A flag indicating active status. Defaults to true.
 * @param accountType The type of the bank account. Defaults to `DEBTOR`.
 * @param modifierId The unique identifier of the entity making the modification.
 * @return The updated bank account entity.
 */
fun Transaction.updateBankAccount(
    bankAccountId: UUID,
    userId: UUID,
    iban: IBAN,
    bic: BIC,
    accountHolder: String = "",
    isActive: Boolean = true,
    accountType: AccountType = AccountType.DEBTOR,
    description: String? = null,
    modifierId: UUID
) : BankAccountEntity {
    val bankAccount = validatedBankAccount(bankAccountId)
    if(bic.value.isNotBlank()) validateBic(bic.value)
    validateIban(iban)
    validateIsUserOrOrganization(userId)
    
    val changed = bankAccount.userId != userId
        || bankAccount.iban != iban.value
        || bankAccount.bic != bic.value
        || bankAccount.accountHolder != accountHolder
        || bankAccount.isActive != isActive
        || bankAccount.accountType != accountType
        || bankAccount.description != description

    bankAccount.userId = userId
    bankAccount.iban = iban.value
    bankAccount.bic = bic.value.trim()
    bankAccount.accountHolder = accountHolder
    bankAccount.isActive = isActive
    bankAccount.accountType = accountType
    bankAccount.description = description

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
    BankAccountAccessorsTable.deleteWhere { BankAccountAccessorsTable.bankAccountId eq bankAccountId }
    bankAccount.delete()
    return bankAccountId
}

/**
 * Imports a list of bank accounts by either updating existing ones or creating new accounts
 * based on the provided data. Associates the accounts with a specified accessor, prioritizing updates when applicable.
 *
 * @param accessorId The unique identifier of the accessor for which the bank accounts are being imported.
 * @param accounts A list of bank accounts to be imported consisting of user ID, IBAN, and BIC details.
 */
fun Transaction.importBankAccounts(
    accessorId: UUID, 
    accounts: List<ImportBankAccount>,
    creatorId: UUID
): List<BankAccountEntity> {
    val existingAccountsMap = readBankAccountsByLegalEntity(accessorId)
        .associateBy { it.userId.toString() }

    val (accountsToUpdate, accountsToCreate) = accounts.partition {
        it.username.value in existingAccountsMap
    }

    val usernames = accounts.map { it.username.value }
    val existingUsersMap = UserEntity.find { UsersTable.username inList usernames }.associateBy({ user ->
        user.username
    }) {
        user -> ApiUser(
        user.id.value.toString(),
            user.username,
        )
    }
    val usersToCreate = usernames.filterNot{ existingUsersMap.keys.contains(it) }

    val newUsers = usersToCreate.map{ username -> createUser(
        CreateUser(username, ""),
        creatorId
    ) }.associateBy ({
        user -> user.username
    }){
        user -> user
    }

    val allUsers: Map<String, ApiUser> = existingUsersMap + newUsers


    val created = accountsToCreate.map { bankAccount ->
        with(bankAccount) {

            val userId = requireNotNull(allUsers[username.value]){
                "User ${username.value} does not exist"
            }.let { UUID.fromString(it.id) }
            val newBankAccount = createBankAccount(
                userId = userId,
                iban = iban,
                bic = bic,
                accountHolder = bankAccountHolder,
                isActive = isActive,
                accountType = accountType.toDomainType(),
                creatorId = accessorId,
                description = description
            )
            createBankAccountAccessor(accessorId, newBankAccount)
            newBankAccount
        }
    }
    
    val updated = accountsToUpdate.map { bankAccount ->
        val existingBankAccount = requireNotNull(existingAccountsMap[bankAccount.username.value]) {
            "Bank account for user ${bankAccount.username.value} does not exist"
        }
        val userId = requireNotNull(allUsers[bankAccount.username.value]){
            "User ${bankAccount.username.value} does not exist"
        }.let { UUID.fromString(it.id) }
        updateBankAccount(
            bankAccountId = existingBankAccount.id.value,
            userId = userId,
            iban = bankAccount.iban,
            bic = bankAccount.bic,
            accountHolder = bankAccount.bankAccountHolder,
            isActive = bankAccount.isActive,
            accountType = bankAccount.accountType.toDomainType(),
            description = bankAccount.description,
            modifierId = accessorId
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
 * Deletes a bank account by its unique identifier and removes corresponding entries from accessors and account tables.
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
