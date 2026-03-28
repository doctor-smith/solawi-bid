package org.solyton.solawi.bid.module.banking.action

import org.evoleq.math.dispatch
import org.evoleq.math.emit
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.solyton.solawi.bid.application.serialization.installSerializers
import org.solyton.solawi.bid.module.banking.data.BIC
import org.solyton.solawi.bid.module.banking.data.BankAccountId
import org.solyton.solawi.bid.module.banking.data.IBAN
import org.solyton.solawi.bid.module.banking.data.api.ApiBankAccount
import org.solyton.solawi.bid.module.banking.data.api.UpdateBankAccount
import org.solyton.solawi.bid.module.banking.data.application.bankAccounts
import org.solyton.solawi.bid.module.banking.data.bankaccount.AccountType
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.values.UserId
import org.solyton.solawi.bid.test.UUID_1
import org.solyton.solawi.bid.test.base.runComposeTest
import org.solyton.solawi.bid.test.storage.TestStorage
import kotlin.test.Test
import kotlin.test.assertEquals


class BankAccountsUpdateTest {

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun updateBankAccountsTest() = runComposeTest {
        installSerializers()
        val id = BankAccountId(UUID_1)
        val userId = UserId(UUID_1)
        val bic = BIC("DEUTDEFF500")
        val iban = IBAN("DE89370400440532013000")
        val accountHolder = "accountHolder"
        val isActive = true
        val accountType = AccountType.CREDITOR
        val action = updateBankAccount(
            id,
            userId,
            iban,
            bic,
            accountHolder,
            isActive,
            accountType
        )



        composition {
            val storage = TestStorage(testBankingApplication.copy(
                bankAccounts = listOf(
                    BankAccount(id, userId, iban, bic)
                )
            ))

            val args = (storage * action.reader).emit()
            val expectedArgs = UpdateBankAccount(id, userId, bic, iban, accountHolder, isActive, accountType.toApiType())
            assertEquals(expectedArgs, args)


            val response = ApiBankAccount(id, userId, bic, iban, accountHolder, isActive, accountType.toApiType() )
            (storage *action.writer) dispatch response
            val storedResponse = (storage * bankAccounts * FirstBy { it.bankAccountId == id }).read()
            val expectedResponse = response.toDomainType()

            assertEquals(expectedResponse, storedResponse)
        }
    }
}
