package org.solyton.solawi.bid.module.banking.action

import kotlinx.datetime.LocalDate
import org.evoleq.math.dispatch
import org.evoleq.math.emit
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import org.solyton.solawi.bid.application.serialization.installSerializers
import org.solyton.solawi.bid.module.banking.data.api.ApiFiscalYear
import org.solyton.solawi.bid.module.banking.data.api.UpdateFiscalYear
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.fiscalYears
import org.solyton.solawi.bid.module.banking.data.fiscalyear.FiscalYear
import org.solyton.solawi.bid.module.banking.data.toDomainType
import org.solyton.solawi.bid.test.base.runComposeTest
import org.solyton.solawi.bid.test.storage.TestStorage
import kotlin.test.Test
import kotlin.test.assertEquals


class FiscalYearsUpdateTest {

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun updateFiscalYearsTest() = runComposeTest {
        installSerializers()
        val legalEntityId = "legalEntityId"
        val start = LocalDate(2024, 1, 1)
        val end = LocalDate(2024, 12, 31)

        val id = "id"

        val action = updateFiscalYear(
            id,
            legalEntityId,
            start,
            end
        )

        composition {
            val storage = TestStorage(BankingApplication(
                fiscalYears = listOf(
                    FiscalYear(id, legalEntityId, start, end)
                )
            ))

            val args = (storage * action.reader).emit()
            val expectedArgs = UpdateFiscalYear(id, legalEntityId, start, end)
            assertEquals(expectedArgs, args)


            val response = ApiFiscalYear(id, legalEntityId, start, end)
            (storage *action.writer) dispatch response
            val storedResponse = (storage * fiscalYears * FirstBy { it.fiscalYearId == id }).read()
            val expectedResponse = response.toDomainType()

            assertEquals(expectedResponse, storedResponse)
        }
    }
}
