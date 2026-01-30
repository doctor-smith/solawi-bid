package org.solyton.solawi.bid.module.banking.action

import org.evoleq.math.dispatch
import org.evoleq.math.emit
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import org.solyton.solawi.bid.application.serialization.installSerializers
import org.solyton.solawi.bid.module.banking.data.api.ApiBankAccount
import org.solyton.solawi.bid.module.banking.data.api.UpdateBankAccount
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.bankAccounts
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.test.base.runComposeTest
import org.solyton.solawi.bid.test.storage.TestStorage
import kotlin.test.Test
import kotlin.test.assertEquals


class BankAccountsUpdateTest {

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun updateBankAccountsTest() = runComposeTest {
        installSerializers()
        val id = "id"
        val userId = "userId"
        val bic = "DEUTDEFF500"
        val iban = "DE89370400440532013000"

        val action = updateBankAccount(
            id,
            userId,
            iban,
            bic
        )



        composition {
            val storage = TestStorage(BankingApplication(
                bankAccounts = listOf(
                    BankAccount(id, userId, bic, iban)
                )
            ))

            val args = (storage * action.reader).emit()
            val expectedArgs = UpdateBankAccount(id, userId, bic, iban)
            assertEquals(expectedArgs, args)


            val response = ApiBankAccount(id, userId, bic, iban )
            (storage *action.writer) dispatch response
            val storedResponse = (storage * bankAccounts * FirstBy { it.bankAccountId == id }).read()
            val expectedResponse = response.toDomainType()

            assertEquals(expectedResponse, storedResponse)
        }
    }
}
