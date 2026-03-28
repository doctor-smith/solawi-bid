package org.solyton.solawi.bid.module.banking.repository

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.uuid.UUID_ZERO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.banking.data.BIC
import org.solyton.solawi.bid.module.banking.data.IBAN
import org.solyton.solawi.bid.module.banking.schema.AccountType
import org.solyton.solawi.bid.module.banking.exception.BankAccountsException
import org.solyton.solawi.bid.module.banking.exception.LegalEntityException
import org.solyton.solawi.bid.module.banking.schema.BankAccountAccessorsTable
import org.solyton.solawi.bid.module.banking.schema.BankAccountsTable
import org.solyton.solawi.bid.module.permission.schema.*
import org.solyton.solawi.bid.module.user.exception.UserManagementException
import org.solyton.solawi.bid.module.user.schema.OrganizationsTable
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserStatus
import org.solyton.solawi.bid.module.user.schema.UsersTable
import java.util.*

class BankAccountRepositoryTest {

    val tables  = arrayOf(
        ContextsTable,
        RightsTable,
        RolesTable,
        RoleRightContexts,
        UserRoleContext,
        UsersTable,
        BankAccountsTable,
        BankAccountAccessorsTable,
        OrganizationsTable
    )

    @DbFunctional@Test
    fun `createBankAccount successfully creates a new bank account`() = runSimpleH2Test(*tables) {
        val user = UserEntity.new {
            username = "user"
            password = "password"
            status = UserStatus.ACTIVE
            createdBy = UUID_ZERO
        }
        val userId = user.id.value
        val creatorId = UUID.randomUUID()
        val iban = IBAN("DE89370400440532013000")
        val bic = BIC("DEUTDEBBXXX")

        val bankAccount = createBankAccount(userId, iban, bic, "", true, AccountType.DEBTOR, creatorId)

        assertNotNull(bankAccount)
        assertEquals(userId, bankAccount.userId)
        assertEquals(iban.value, bankAccount.iban)
        assertEquals(bic.value, bankAccount.bic)
        assertEquals(creatorId, bankAccount.createdBy)
    }

    @DbFunctional@Test
    fun `createBankAccount fails with invalid IBAN`() = runSimpleH2Test(*tables) {
        val user = UserEntity.new {
            username = "user"
            password = "password"
            status = UserStatus.ACTIVE
            createdBy = UUID_ZERO
        }
        val userId = user.id.value
        val creatorId = UUID.randomUUID()
        val invalidIban = IBAN("INVALIDIBAN123456")
        val bic = BIC("DEUTDEBBXXX")

        val exception = assertThrows<BankAccountsException.InvalidIban> {
            createBankAccount(userId, invalidIban, bic, "", true, AccountType.DEBTOR, creatorId)
        }

        assertEquals("Invalid IBAN format: ${invalidIban.value}", exception.message)

    }

    @DbFunctional@Test
    fun `createBankAccount fails with invalid BIC`()  = runSimpleH2Test(*tables) {
        val user = UserEntity.new {
            username = "user"
            password = "password"
            status = UserStatus.ACTIVE
            createdBy = UUID_ZERO
        }
        val userId = user.id.value
        val creatorId = UUID.randomUUID()
        val iban = IBAN("DE89370400440532013000")
        val invalidBic = BIC("INVALDEDB60")

        val exception = assertThrows<BankAccountsException.InvalidBicCountryCode> {
            createBankAccount(userId, iban, invalidBic,  "", true, AccountType.DEBTOR,creatorId)
        }

        assertEquals("Invalid BIC country code: ${invalidBic.value}", exception.message)
    }

    @DbFunctional@Test
    fun `createBankAccount fails when user does not exist`()  = runSimpleH2Test(*tables) {
        val nonExistentUserId = UUID.randomUUID()
        val creatorId = UUID.randomUUID()
        val iban = IBAN("DE89370400440532013000")
        val bic = BIC("DEUTDEBBXXX")

        assertThrows<LegalEntityException.NoSuchLegalEntity> {
            createBankAccount(nonExistentUserId, iban, bic,  "", true, AccountType.DEBTOR,creatorId)
        }
    }

    @DbFunctional@Test
    fun `getBankAccountAccessor successfully retrieves a bank account accessor`() = runSimpleH2Test(*tables) {
        val user = UserEntity.new {
            username = "user"
            password = "password"
            status = UserStatus.ACTIVE
            createdBy = UUID_ZERO
        }
        val userId = user.id.value
        val creatorId = UUID.randomUUID()
        val iban = IBAN("DE89370400440532013000")
        val bic = BIC("DEUTDEBBXXX")

        val bankAccount = createBankAccount(userId, iban, bic,  "", true, AccountType.DEBTOR,creatorId)
        val accessorId = UUID.randomUUID()
        val accessor = createBankAccountAccessor(accessorId, bankAccount)

        assertNotNull(accessor)
        assertEquals(bankAccount, accessor.bankAccount)
    }

    @DbFunctional
    @Test
    fun `getBankAccount successfully retrieves an existing bank account`() = runSimpleH2Test(*tables) {
        val user = UserEntity.new {
            username = "user"
            password = "password"
            status = UserStatus.ACTIVE
            createdBy = UUID_ZERO
        }
        val userId = user.id.value
        val creatorId = UUID.randomUUID()
        val iban = IBAN("DE89370400440532013000")
        val bic = BIC("DEUTDEBBXXX")

        val createdAccount = createBankAccount(userId, iban, bic,  "", true, AccountType.DEBTOR,creatorId)
        val retrievedAccount = readBankAccount(createdAccount.id.value)

        assertNotNull(retrievedAccount)
        assertEquals(createdAccount, retrievedAccount)
    }

    @DbFunctional
    @Test
    fun `getBankAccount throws exception for non-existent account`() = runSimpleH2Test(*tables) {
        val nonExistentId = UUID.randomUUID()

        assertThrows<BankAccountsException.NoSuchBankAccount> {
            readBankAccount(nonExistentId)
        }
    }

    @DbFunctional
    @Test
    fun `updateBankAccount successfully updates an existing account`() = runSimpleH2Test(*tables) {
        val user = UserEntity.new {
            username = "user"
            password = "password"
            status = UserStatus.ACTIVE
            createdBy = UUID_ZERO
        }
        val userId = user.id.value
        val creatorId = UUID.randomUUID()
        val originalIban = IBAN("DE89370400440532013000")
        val originalBic = BIC("DEUTDEBBXXX")
        val newIban = IBAN("ES9121000418450200051332")
        val newBic = BIC("DEUTDEFFXXX")

        val account = createBankAccount(userId, originalIban, originalBic,  "", true, AccountType.DEBTOR,creatorId)
        val updatedAccount = updateBankAccount(
            account.id.value,
            userId,
            newIban,
            newBic, "", true, AccountType.DEBTOR,
            creatorId
        )

        assertEquals(newIban.value, updatedAccount.iban)
        assertEquals(newBic.value, updatedAccount.bic)
    }

    @DbFunctional
    @Test
    fun `updateBankAccount throws exception for non-existent account`() = runSimpleH2Test(*tables) {
        val nonExistentId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val iban = IBAN("DE89370400440532013000")
        val bic = BIC("DEUTDEBBXXX")

        assertThrows<BankAccountsException.NoSuchBankAccount> {
            updateBankAccount(nonExistentId, userId, iban, bic,  "", true, AccountType.DEBTOR,UUID.randomUUID())
        }
    }

    @DbFunctional
    @Test
    fun `deleteBankAccount successfully deletes an existing account`() = runSimpleH2Test(*tables) {
        val user = UserEntity.new {
            username = "user"
            password = "password"
            status = UserStatus.ACTIVE
            createdBy = UUID_ZERO
        }
        val userId = user.id.value
        val creatorId = UUID.randomUUID()
        val iban = IBAN("DE89370400440532013000")
        val bic = BIC("DEUTDEBBXXX")

        val account = createBankAccount(userId, iban, bic,  "", true, AccountType.DEBTOR,creatorId)
        deleteBankAccount(account.id.value)

        assertThrows<BankAccountsException.NoSuchBankAccount> {
            readBankAccount(account.id.value)
        }
    }

    @DbFunctional
    @Test
    fun `deleteBankAccount throws exception for non-existent account`() = runSimpleH2Test(*tables) {
        val nonExistentId = UUID.randomUUID()

        assertThrows<BankAccountsException.NoSuchBankAccount> {
            deleteBankAccount(nonExistentId)
        }
    }
}
