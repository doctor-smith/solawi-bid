package org.evoleq.kotlinx.date

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class DateCommonTests {

    @Test fun timeTest() {
        val dateTime = todayWithTime()
        val localDateTime = dateTime.toClientTime(TimeZone.currentSystemDefault())

        println("""
            |dateTime:........$dateTime,
            |localDateTime:...$localDateTime
        """.trimMargin())

        println(TimeZone.availableZoneIds.sorted().joinToString("\n") { it })

        assertEquals(dateTime, localDateTime.toServerTime(TimeZone.currentSystemDefault()))
    }
}
