package org.solyton.solawi.bid.module.banking.action

import org.evoleq.math.dispatch
import org.evoleq.math.emit
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.solyton.solawi.bid.application.serialization.installSerializers
import org.solyton.solawi.bid.module.banking.data.BIC
import org.solyton.solawi.bid.module.banking.data.BankAccountId
import org.solyton.solawi.bid.module.banking.data.IBAN
import org.solyton.solawi.bid.module.banking.data.api.ApiBankAccount
import org.solyton.solawi.bid.module.banking.data.api.ApiBankAccounts
import org.solyton.solawi.bid.module.banking.data.api.ReadBankAccounts
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.bankAccounts
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.module.values.ProviderId
import org.solyton.solawi.bid.module.values.UserId
import org.solyton.solawi.bid.test.UUID_1
import org.solyton.solawi.bid.test.base.runComposeTest
import org.solyton.solawi.bid.test.storage.TestStorage
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class BankAccountsReadTest {
    @OptIn(ComposeWebExperimentalTestsApi::class)
    //@Test //- corrupted why???
    fun readBankAccountsTest() = runComposeTest {
        installSerializers()
        val providerId = ProviderId(UUID_1)
        val userId = UserId(UUID_1)
        val bic = BIC("DEUTDEFF500")
        val iban = IBAN("DE89370400440532013000")

        val action = readBankAccounts(
            providerId
        )
        composition {
            val storage = TestStorage(BankingApplication())

            val args = (storage * action.reader).emit()
            val expectedArgs = ReadBankAccounts(listOf("legal_entity" to userId.value))
            assertEquals(expectedArgs, args)

            val id = BankAccountId(UUID_1)
            val response = ApiBankAccounts( listOf(ApiBankAccount(id, userId, bic, iban)))
            (storage * action.writer) dispatch response
            val storedResponse = (storage * bankAccounts).read()


            val expectedResponse = response.toDomainType()
            assertNotNull(expectedResponse, "response.toDomainType() hat null geliefert")

            assertEquals(expectedResponse, storedResponse)
        }
    }
}
