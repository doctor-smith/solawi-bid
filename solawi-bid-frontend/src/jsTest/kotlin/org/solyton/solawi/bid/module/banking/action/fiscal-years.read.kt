package org.solyton.solawi.bid.module.banking.action

import kotlinx.datetime.LocalDate
import org.evoleq.math.dispatch
import org.evoleq.math.emit
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.solyton.solawi.bid.application.serialization.installSerializers
import org.solyton.solawi.bid.module.banking.data.api.ApiFiscalYear
import org.solyton.solawi.bid.module.banking.data.api.ApiFiscalYears
import org.solyton.solawi.bid.module.banking.data.api.ReadFiscalYears
import org.solyton.solawi.bid.module.banking.data.application.fiscalYears
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.test.base.runComposeTest
import org.solyton.solawi.bid.test.storage.TestStorage
import kotlin.test.Test
import kotlin.test.assertEquals


class FiscalYearsReadTest {

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun readFiscalYearsTest() = runComposeTest {
        installSerializers()
        val legalEntityId = "legalEntityId"
        val start = LocalDate(2024, 1, 1)
        val end = LocalDate(2024, 12, 31)

        val action = readFiscalYears(
            legalEntityId,
        )

        composition {
            val storage = TestStorage(testBankingApplication)

            val args = (storage * action.reader).emit()
            val expectedArgs = ReadFiscalYears(listOf("legal_entity" to legalEntityId))
            assertEquals(expectedArgs, args)

            val id = "id"
            val response = ApiFiscalYears(listOf(ApiFiscalYear(id, legalEntityId, start, end)))
            (storage *action.writer) dispatch response
            val storedResponse = (storage * fiscalYears).read()
            val expectedResponse = response.toDomainType()

            assertEquals(expectedResponse, storedResponse)
        }
    }
}
