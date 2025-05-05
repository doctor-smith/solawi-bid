package org.evoleq.csv

import kotlin.test.Test
import kotlin.test.assertEquals

class CsvParserTest {

    @Test fun parseCsvTDD() {
        val csv = """
            |  h1 ;h2 ; h3
            |a ; 3; g
            |d;d;d  
        """.trimMargin()

        val lines = csv.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.split(";").map { i -> i.trim() }}

        val (headers, data) = Pair(lines.first(), lines.drop(1).map { it.toTypedArray() })
        val result = data.map {
            mapOf(*headers.mapIndexed { index, s ->  s to it[index]}.toTypedArray())
        }

        assertEquals(2, result.size)
        assertEquals(
            mapOf("h1" to "a", "h2" to "3", "h3" to "g"),
            result.first()
        )
    }

    @Test fun parseCsv() {
        val csv = """
            |  h1 ;h2 ; h3
            |a ; 3; g     
            |d;d;d  
        """.trimMargin()

        val result = parseCsv(csv)

        assertEquals(2, result.size)
        assertEquals(
            mapOf("h1" to "a", "h2" to "3", "h3" to "g"),
            result.first()
        )

    }
}