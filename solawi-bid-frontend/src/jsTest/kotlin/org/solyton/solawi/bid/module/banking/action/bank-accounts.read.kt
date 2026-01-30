package org.solyton.solawi.bid.module.banking.action

import org.evoleq.math.dispatch
import org.evoleq.math.emit
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import org.solyton.solawi.bid.application.serialization.installSerializers
import org.solyton.solawi.bid.module.banking.data.api.ApiBankAccount
import org.solyton.solawi.bid.module.banking.data.api.ApiBankAccounts
import org.solyton.solawi.bid.module.banking.data.api.ReadBankAccounts
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.bankAccounts
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.test.base.runComposeTest
import org.solyton.solawi.bid.test.storage.TestStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class BankAccountsReadTest {
    @OptIn(ComposeWebExperimentalTestsApi::class)
    //@Test //- corrupted why???
    fun readBankAccountsTest() = runComposeTest {
        installSerializers()
        val userId = "userId"
        val bic = "DEUTDEFF500"
        val iban = "DE89370400440532013000"

        val action = readBankAccounts(
            userId
        )
        composition {
            val storage = TestStorage(BankingApplication())

            val args = (storage * action.reader).emit()
            val expectedArgs = ReadBankAccounts(listOf("legal_entity" to userId))
            assertEquals(expectedArgs, args)

            val id = "id"
            val response = ApiBankAccounts( listOf(ApiBankAccount(id, userId, bic, iban)))
            (storage * action.writer) dispatch response
            val storedResponse = (storage * bankAccounts).read()


            val expectedResponse = response.toDomainType()
            assertNotNull(expectedResponse, "response.toDomainType() hat null geliefert")

            assertEquals(expectedResponse, storedResponse)
        }
    }
}
