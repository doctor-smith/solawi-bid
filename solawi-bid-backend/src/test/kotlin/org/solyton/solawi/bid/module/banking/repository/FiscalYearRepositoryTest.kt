package org.solyton.solawi.bid.module.banking.repository

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.uuid.UUID_ZERO
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.banking.exception.FiscalYearException
import org.solyton.solawi.bid.module.banking.schema.FiscalYearEntity
import org.solyton.solawi.bid.module.banking.schema.FiscalYears
import java.util.UUID
import kotlin.test.assertContains
import kotlin.test.assertEquals

val UUID_ONE: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")


class FiscalYearRepositoryTest {
    @DbFunctional@Test
    fun createFiscalYearForTheVeryFirstTime() = runSimpleH2Test(FiscalYears){
        val start = DateTime.now()
        val end = start.plusYears(1)
        val legalEntityId = UUID.randomUUID()
        val creator = UUID_ZERO

        val fiscalYear = createFiscalYear(
            legalEntityId,
            start,
            end,
            creator
        )

        assertEquals(start, fiscalYear.start)
        assertEquals(end, fiscalYear.end)
        assertEquals(legalEntityId,fiscalYear.legalEntityId)
        assertEquals(creator, fiscalYear.createdBy)
    }

    @DbFunctional@Test
    fun createFiscalYearForTheSecondTime1() = runSimpleH2Test(FiscalYears){
        val start = DateTime.now()
        val end = start.plusYears(1)
        val legalEntityId = UUID.randomUUID()
        val creator = UUID_ZERO

        val fiscalYear = createFiscalYear(
            legalEntityId,
            start,
            end,
            creator
        )

        val fiscalYear2 = createFiscalYear(
            legalEntityId,
            start.plusYears(2),
            end.plusYears(2),
            creator
        )

        val fiscalYears = FiscalYearEntity.all().toSet()
        assertEquals(2, fiscalYears.size)
        assertContains(fiscalYears, fiscalYear)
        assertContains(fiscalYears, fiscalYear2)
    }

    @DbFunctional@Test
    fun createFiscalYearForTheSecondTime2() = runSimpleH2Test(FiscalYears){
        val start = DateTime.now()
        val end = start.plusYears(1)
        val legalEntityId = UUID.randomUUID()
        val creator = UUID_ZERO

        val fiscalYear = createFiscalYear(
            legalEntityId,
            start,
            end,
            creator
        )

        val fiscalYear2 = createFiscalYear(
            legalEntityId,
            start.plusYears(-2),
            end.plusYears(-2),
            creator
        )

        val fiscalYears = FiscalYearEntity.all().toSet()
        assertEquals(2, fiscalYears.size)
        assertContains(fiscalYears, fiscalYear)
        assertContains(fiscalYears, fiscalYear2)
    }

    @DbFunctional@Test
    fun createFiscalYearFailDueToIntervalValidation() = runSimpleH2Test(FiscalYears){
        val start = DateTime.now()
        val end = start.plusYears(-1)
        val legalEntityId = UUID.randomUUID()
        val creator = UUID_ZERO
        assertThrows<FiscalYearException.StartAfterEnd> {
            createFiscalYear(
                legalEntityId,
                start,
                end,
                creator
            )
        }
    }

    @DbFunctional@Test
    fun createFiscalYearFailDueToOverlaps1() = runSimpleH2Test(FiscalYears){
        val start = DateTime.now()
        val end = start.plusYears(1)
        val legalEntityId = UUID.randomUUID()
        val creator = UUID_ZERO
        createFiscalYear(
            legalEntityId,
            start,
            end,
            creator
        )
        val shift = 10
        assertThrows<FiscalYearException.Overlaps> {
            createFiscalYear(
                legalEntityId,
                start.plusDays(shift),
                end.plusDays(shift),
                creator
            )
        }
    }

    @DbFunctional@Test
    fun createFiscalYearFailDueToOverlaps2() = runSimpleH2Test(FiscalYears){
        val start = DateTime.now()
        val end = start.plusYears(1)
        val legalEntityId = UUID.randomUUID()
        val creator = UUID_ZERO
        createFiscalYear(
            legalEntityId,
            start,
            end,
            creator
        )
        val shift = -10
        assertThrows<FiscalYearException.Overlaps> {
            createFiscalYear(
                legalEntityId,
                start.plusDays(shift),
                end.plusDays(shift),
                creator
            )
        }
    }

    @DbFunctional@Test
    fun createFiscalYear2WithOverlapsButDifferentLegalEntity() = runSimpleH2Test(FiscalYears){
        val start = DateTime.now()
        val end = start.plusYears(1)
        val legalEntityId = UUID_ONE
        val creator = UUID_ZERO
        createFiscalYear(
            legalEntityId,
            start,
            end,
            creator
        )
        val shift = 10
        assertDoesNotThrow {
            createFiscalYear(
                UUID_ZERO,
                start.plusDays(shift),
                end.plusDays(shift),
                creator
            )
        }
    }

    @DbFunctional@Test
    fun readFiscalYears() = runSimpleH2Test(FiscalYears) {
        val start = DateTime.now()
        val end = start.plusYears(1)
        val legalEntityId = UUID.randomUUID()
        val creator = UUID_ZERO
        val fiscalYear = createFiscalYear(
            legalEntityId,
            start,
            end,
            creator
        )

        val fiscalYears = readFiscalYears(legalEntityId)
        assertEquals(1, fiscalYears.size)
        assertContains(fiscalYears, fiscalYear)
    }

    @DbFunctional@Test
    fun readFiscalYearsEmpty() = runSimpleH2Test(FiscalYears) {
        val start = DateTime.now()
        val end = start.plusYears(1)
        val legalEntityId = UUID_ONE
        val creator = UUID_ZERO
        createFiscalYear(
            legalEntityId,
            start,
            end,
            creator
        )

        val fiscalYears = readFiscalYears(UUID_ZERO)
        assertTrue(fiscalYears.isEmpty())
    }

    @DbFunctional@Test
    fun updateFiscalYearModifyStart() = runSimpleH2Test(FiscalYears) {
        val start = DateTime.now()
        val end = start.plusYears(1)
        val legalEntityId = UUID.randomUUID()
        val creator = UUID_ZERO
        val fiscalYear = createFiscalYear(
            legalEntityId,
            start,
            end,
            creator
        )

        val modifier = UUID.randomUUID()
        val newStart = start.plusDays(1)

        val updatedFiscalYear = updateFiscalYear(
            fiscalYear.id.value,
            legalEntityId,
            newStart,
            end,
            modifier
        )
        assertEquals(legalEntityId, updatedFiscalYear.legalEntityId)
        assertEquals(newStart, updatedFiscalYear.start)
        assertEquals(end, updatedFiscalYear.end)
        assertEquals(modifier, updatedFiscalYear.modifiedBy)
        assertNotNull(updatedFiscalYear.modifiedAt)

    }

    @DbFunctional@Test
    fun updateFiscalYearModifyEnd() = runSimpleH2Test(FiscalYears) {
        val start = DateTime.now()
        val end = start.plusYears(1)
        val legalEntityId = UUID.randomUUID()
        val creator = UUID_ZERO
        val fiscalYear = createFiscalYear(
            legalEntityId,
            start,
            end,
            creator
        )

        val modifier = UUID.randomUUID()
        val newEnd = end.plusDays(1)

        val updatedFiscalYear = updateFiscalYear(
            fiscalYear.id.value,
            legalEntityId,
            start,
            newEnd,
            modifier
        )
        assertEquals(legalEntityId, updatedFiscalYear.legalEntityId)
        assertEquals(start, updatedFiscalYear.start)
        assertEquals(newEnd, updatedFiscalYear.end)
        assertEquals(modifier, updatedFiscalYear.modifiedBy)
        assertNotNull(updatedFiscalYear.modifiedAt)
    }

    @DbFunctional@Test
    fun updateFiscalYearModifyLegalEntity() = runSimpleH2Test(FiscalYears) {
        val start = DateTime.now()
        val end = start.plusYears(1)
        val legalEntityId = UUID_ZERO
        val creator = UUID_ZERO
        val fiscalYear = createFiscalYear(
            legalEntityId,
            start,
            end,
            creator
        )

        val modifier = UUID.randomUUID()
        val newLegalEntityId = UUID_ONE

        val updatedFiscalYear = updateFiscalYear(
            fiscalYear.id.value,
            newLegalEntityId,
            start,
            end,
            modifier
        )
        assertEquals(newLegalEntityId, updatedFiscalYear.legalEntityId)
        assertEquals(start, updatedFiscalYear.start)
        assertEquals(end, updatedFiscalYear.end)
        assertEquals(modifier, updatedFiscalYear.modifiedBy)
        assertNotNull(updatedFiscalYear.modifiedAt)
    }

    @DbFunctional@Test
    fun updateFiscalYearWithoutChanges() = runSimpleH2Test(FiscalYears) {
        val start = DateTime.now()
        val end = start.plusYears(1)
        val legalEntityId = UUID_ZERO
        val creator = UUID_ZERO
        val fiscalYear = createFiscalYear(
            legalEntityId,
            start,
            end,
            creator
        )

        val modifier = UUID.randomUUID()
        val updatedFiscalYear = updateFiscalYear(
            fiscalYear.id.value,
            legalEntityId,
            start,
            end,
            modifier
        )
        assertEquals(legalEntityId, updatedFiscalYear.legalEntityId)
        assertEquals(start, updatedFiscalYear.start)
        assertEquals(end, updatedFiscalYear.end)
        assertNull(updatedFiscalYear.modifiedBy)
        assertNull(updatedFiscalYear.modifiedAt)
    }

    @DbFunctional@Test
    fun updateFiscalYearFailDueToIntervalValidation() = runSimpleH2Test(FiscalYears) {
        val start = DateTime.now()
        val end = start.plusYears(1)
        val legalEntityId = UUID_ZERO
        val creator = UUID_ZERO
        val fiscalYear = createFiscalYear(
            legalEntityId,
            start,
            end,
            creator
        )

        val modifier = UUID.randomUUID()
        assertThrows<FiscalYearException.StartAfterEnd> {
            updateFiscalYear(
                fiscalYear.id.value,
                legalEntityId,
                end,
                start,
                modifier
            )
        }
    }

    @DbFunctional@Test
    fun updateFiscalYearFailDueToOverlaps1() = runSimpleH2Test(FiscalYears) {
        val start = DateTime.now()
        val end = start.plusYears(1)
        val legalEntityId = UUID_ZERO
        val creator = UUID_ZERO
        val fiscalYear = createFiscalYear(
            legalEntityId,
            start,
            end,
            creator
        )
        // val otherFiscalYear =
        createFiscalYear(
            legalEntityId,
            start.plusYears(1).plusDays(1),
            end.plusYears(1).plusDays(1),
            creator
        )

        val modifier = UUID.randomUUID()
        assertThrows<FiscalYearException.Overlaps> {
            updateFiscalYear(
                fiscalYear.id.value,
                legalEntityId,
                start,
                end.plusDays(10),
                modifier
            )
        }
    }

    @DbFunctional@Test
    fun updateFiscalYearFailDueToOverlaps2() = runSimpleH2Test(FiscalYears) {
        val start = DateTime.now()
        val end = start.plusYears(1)
        val legalEntityId = UUID_ZERO
        val creator = UUID_ZERO
        // val fiscalYear =
        createFiscalYear(
            legalEntityId,
            start,
            end,
            creator
        )
        val otherFiscalYear = createFiscalYear(
            legalEntityId,
            start.plusYears(1).plusDays(1),
            end.plusYears(1).plusDays(1),
            creator
        )

        val modifier = UUID.randomUUID()
        assertThrows<FiscalYearException.Overlaps> {
            updateFiscalYear(
                otherFiscalYear.id.value,
                legalEntityId,
                start.plusDays(-10),
                end,
                modifier
            )
        }
    }

    @DbFunctional@Test
    fun updateFiscalYearWithOverlapsButOtherLegalEntity() = runSimpleH2Test(FiscalYears) {
        val start = DateTime.now()
        val end = start.plusYears(1)
        val legalEntityId = UUID_ZERO
        val otherLegalEntityId = UUID_ONE
        val creator = UUID_ZERO
        val fiscalYear = createFiscalYear(
            legalEntityId,
            start,
            end,
            creator
        )
        createFiscalYear(
            otherLegalEntityId,
            start.plusYears(1).plusDays(1),
            end.plusYears(1).plusDays(1),
            creator
        )

        val modifier = UUID.randomUUID()
        assertDoesNotThrow {
            updateFiscalYear(
                fiscalYear.id.value,
                legalEntityId,
                start,
                end.plusDays(10),
                modifier
            )
        }
    }

    @DbFunctional@Test
    fun deleteFiscalYear() = runSimpleH2Test(FiscalYears) {
        val start = DateTime.now()
        val end = start.plusYears(1)
        val legalEntityId = UUID.randomUUID()
        val creator = UUID_ZERO
        val fiscalYear = createFiscalYear(
            legalEntityId,
            start,
            end,
            creator
        )

        deleteFiscalYear(fiscalYear.id.value)

        val fiscalYears = readFiscalYears(legalEntityId)
        assertTrue { fiscalYears.isEmpty()}
    }

    @DbFunctional@Test
    fun deleteFiscalYearsOfLegalEntity() = runSimpleH2Test(FiscalYears) {
        val start = DateTime.now()
        val end = start.plusYears(1)
        val legalEntityId = UUID_ONE
        val creator = UUID_ZERO
        createFiscalYear(
            legalEntityId,
            start,
            end,
            creator
        )
        createFiscalYear(
            legalEntityId,
            start.plusYears(2),
            end.plusYears(2),
            UUID_ZERO
        )
        val other = createFiscalYear(
            UUID_ZERO,
            start,
            end,
            creator
        )

        deleteFiscalYearsOfLegalEntity(legalEntityId)

        val fiscalYears = readFiscalYears(legalEntityId)
        assertTrue { fiscalYears.isEmpty()}

        val allLeftFiscalYears = FiscalYearEntity.all().toList()
        assertEquals(1, allLeftFiscalYears.size)
        assertEquals(other.id, allLeftFiscalYears.first().id)
    }

    @DbFunctional@Test
    fun validateInterval() {
        val start = DateTime.now()
        val end = start.plusDays(1)
        assertDoesNotThrow {
            validateInterval(start, end)
        }
    }

    @DbFunctional@Test
    fun validateIntervalFail() {
        val start = DateTime.now()
        val end = start.plusDays(-1)
        assertThrows<FiscalYearException.StartAfterEnd> {
            validateInterval(start, end)
        }
    }

    @DbFunctional@Test
    fun validateOverlaps1() = runSimpleH2Test(FiscalYears){
        val start1 = DateTime.now()
        val end1 = start1.plusDays(10)

        val legalEntityId = UUID.randomUUID()
        createFiscalYear(
            legalEntityId,
            start1,
            end1,
            UUID_ZERO
        )

        val shift = 11
        val start2 = start1.plusDays(shift)
        val end2 = end1.plusDays(shift)

        assertDoesNotThrow {
            validateOverlaps(
                legalEntityId,
                start2,
                end2
            )
        }
    }

    @DbFunctional@Test
    fun validateOverlaps2() = runSimpleH2Test(FiscalYears){
        val start1 = DateTime.now()
        val end1 = start1.plusDays(10)

        val legalEntityId = UUID.randomUUID()
        createFiscalYear(
            legalEntityId,
            start1,
            end1,
            UUID_ZERO
        )

        val shift = -11
        val start2 = start1.plusDays(shift)
        val end2 = end1.plusDays(shift)

        assertDoesNotThrow {
            validateOverlaps(
                legalEntityId,
                start2,
                end2
            )
        }
    }

    @DbFunctional@Test
    fun validateOverlapsFail1() = runSimpleH2Test(FiscalYears){
        val start1 = DateTime.now()
        val end1 = start1.plusDays(10)

        val legalEntityId = UUID.randomUUID()
        createFiscalYear(
            legalEntityId,
            start1,
            end1,
            UUID_ZERO
        )

        val shift = 5
        val start2 = start1.plusDays(shift)
        val end2 = end1.plusDays(shift)

        assertThrows<FiscalYearException.Overlaps> {
            validateOverlaps(
                legalEntityId,
                start2,
                end2
            )
        }
    }

    @DbFunctional@Test
    fun validateOverlapsFail2() = runSimpleH2Test(FiscalYears){
        val start1 = DateTime.now()
        val end1 = start1.plusDays(10)

        val legalEntityId = UUID.randomUUID()
        createFiscalYear(
            legalEntityId,
            start1,
            end1,
            UUID_ZERO
        )

        val shift = -5
        val start2 = start1.plusDays(shift)
        val end2 = end1.plusDays(shift)

        assertThrows<FiscalYearException.Overlaps> {
            validateOverlaps(
                legalEntityId,
                start2,
                end2
            )
        }
    }

    @DbFunctional@Test
    fun validateOverlapsWithExcludes() = runSimpleH2Test(FiscalYears){
        val start1 = DateTime.now()
        val end1 = start1.plusDays(10)

        val legalEntityId = UUID.randomUUID()
        val fiscalYear = createFiscalYear(
            legalEntityId,
            start1,
            end1,
            UUID_ZERO
        )

        val shift = 5
        val start2 = start1.plusDays(shift)
        val end2 = end1.plusDays(shift)

        assertDoesNotThrow {
            validateOverlaps(
                legalEntityId,
                start2,
                end2,
                listOf(fiscalYear.id.value)
            )
        }
    }

    @DbFunctional@Test
    fun validateOverlapsWithDifferentLegalEntities() = runSimpleH2Test(FiscalYears){
        val start1 = DateTime.now()
        val end1 = start1.plusDays(10)

        val legalEntityId = UUID_ONE
        createFiscalYear(
            legalEntityId,
            start1,
            end1,
            UUID_ZERO
        )

        val shift = 5
        val start2 = start1.plusDays(shift)
        val end2 = end1.plusDays(shift)

        assertDoesNotThrow {
            validateOverlaps(
                UUID_ZERO,
                start2,
                end2,
            )
        }
    }
}
