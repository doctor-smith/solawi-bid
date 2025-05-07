package org.evoleq.compose.date

import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.evoleq.language.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class DateTest {

    @Test fun format() {
        val year = 2024
        val month = 11
        val day = 22
        val result = LocalDate(year,month,day).format(Locale.De)
        val expected = "$day.$month.$year"

        assertEquals(expected, result)
    }

    @Test fun parseDate() {

        val year = 2024
        val month = 11
        val day = 22

        val dateString = "$day.$month.$year"
        val date = dateString.parse(Locale.De)

        assertEquals(year,date.year)
        assertEquals(month, date.month.number)
        assertEquals(day, date.dayOfMonth)



    }
}