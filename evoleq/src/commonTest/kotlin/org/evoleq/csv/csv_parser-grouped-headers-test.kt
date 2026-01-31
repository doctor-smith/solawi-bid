package org.evoleq.csv

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CsvParserTestGroupedHeaders {

    @Test
    fun `parseCsvWithGroupedHeaders - valid input with single area`() {
        val csv = """
            Area1;;;;
            Header1;Header2;Header3;;
            Value1;Value2;Value3;;
        """.trimIndent()

        val expected = listOf(
            mapOf(
                "Area1" to mapOf(
                    "Header1" to "Value1",
                    "Header2" to "Value2",
                    "Header3" to "Value3"
                )
            )
        )

        val result = parseCsvWithGroupedHeaders(csv, ";")
        assertEquals(expected, result)
    }

    @Test
    fun `parseCsvWithGroupedHeaders - valid input with multiple areas`() {
        val csv = """
            Area1;;;Area2;;
            H1;H2;H3;H4;H5;
            V1;V2;V3;V4;V5;
        """.trimIndent()

        val expected = listOf(
            mapOf(
                "Area1" to mapOf(
                    "H1" to "V1",
                    "H2" to "V2",
                    "H3" to "V3"
                ),
                "Area2" to mapOf(
                    "H4" to "V4",
                    "H5" to "V5"
                )
            )
        )

        val result = parseCsvWithGroupedHeaders(csv, ";")
        assertEquals(expected, result)
    }

    @Test
    fun `parseCsvWithGroupedHeaders - missing data row`() {
        val csv = """
            Area1;;;Area2;;
            H1;H2;H3;H4;H5;
        """.trimIndent()

        val expected = emptyList<Map<String, Map<String, String>>>()

        val result = parseCsvWithGroupedHeaders(csv, ";")
        assertEquals(expected, result)
    }

    @Test
    fun `parseCsvWithGroupedHeaders - valid input with matched area and header counts`() {
        val csv = """
            Area1;;Area2;;
            H1;H2;H3;H4;
            V1;V2;V3;V4;
        """.trimIndent()

        val expected = listOf(
            mapOf(
                "Area1" to mapOf(
                    "H1" to "V1",
                    "H2" to "V2"
                ),
                "Area2" to mapOf(
                    "H3" to "V3",
                    "H4" to "V4"
                )
            )
        )

        val result = parseCsvWithGroupedHeaders(csv, ";")
        assertEquals(expected, result)
    }

    @Test
    fun `parseCsvWithGroupedHeaders - invalid input with less than two rows`() {
        val csv = """
            Area1;;;;
        """.trimIndent()

        val exception = assertFailsWith<IllegalArgumentException> {
            parseCsvWithGroupedHeaders(csv, ";")
        }

        assertEquals("Expecting at least 2 lines (Areas + Header line).", exception.message)
    }

    @Test
    fun `parseCsvWithGroupedHeaders - valid input with empty header cells`() {
        val csv = """
            Area1;;;Area2;;
            H1;;H3;H4;;
            V1;V2;V3;V4;V5;
        """.trimIndent()

        val expected = listOf(
            mapOf(
                "Area1" to mapOf(
                    "H1" to "V1",
                    "H3" to "V3"
                ),
                "Area2" to mapOf(
                    "H4" to "V4"
                )
            )
        )

        val result = parseCsvWithGroupedHeaders(csv, ";")
        assertEquals(expected, result)
    }


    /**
     * Tests for the `groupOnNonEmptyStart` function.
     *
     * This function groups elements of a list into sublists based on non-empty start
     * markers, continuing to group empty elements into the current group.
     */

    @Test
    fun `groupOnNonEmptyStart with multiple groups`() {
        // Arrange
        val input = listOf("Group1", "", "", "Group2", "", "Group3", "")

        // Act
        val result = input.groupOnNonEmptyStart()

        // Assert
        val expected = listOf(
            listOf("Group1", "", ""),
            listOf("Group2", ""),
            listOf("Group3", "")
        )
        assertEquals(expected, result)
    }

    @Test
    fun `groupOnNonEmptyStart with no non-empty start`() {
        // Arrange
        val input = listOf("", "", "")

        // Act
        val result = input.groupOnNonEmptyStart()

        // Assert
        val expected = emptyList<List<String>>()
        assertEquals(expected, result)
    }

    @Test
    fun `groupOnNonEmptyStart with only one group`() {
        // Arrange
        val input = listOf("Group1", "", "", "")

        // Act
        val result = input.groupOnNonEmptyStart()

        // Assert
        val expected = listOf(listOf("Group1", "", "", ""))
        assertEquals(expected, result)
    }

    @Test
    fun `groupOnNonEmptyStart with all elements empty`() {
        // Arrange
        val input = listOf("", "", "")

        // Act
        val result = input.groupOnNonEmptyStart()

        // Assert
        val expected = emptyList<List<String>>()
        assertEquals(expected, result)
    }

    @Test
    fun `groupOnNonEmptyStart with multiple non-empty starts and no trailing empty strings`() {
        // Arrange
        val input = listOf("Group1", "Group2", "Group3")

        // Act
        val result = input.groupOnNonEmptyStart()

        // Assert
        val expected = listOf(
            listOf("Group1"),
            listOf("Group2"),
            listOf("Group3")
        )
        assertEquals(expected, result)
    }

    @Test
    fun `chunkBySizes should split list into chunks of given sizes`() {
        val input = listOf(1, 2, 3, 4, 5)
        val sizes = intArrayOf(2, 3)

        val result = input.chunkBySizes(*sizes)

        assertEquals(listOf(listOf(1, 2), listOf(3, 4, 5)), result)
    }

    @Test
    fun `chunkBySizes should handle an empty list`() {
        val input = emptyList<Int>()
        val sizes = intArrayOf(0)

        val result = input.chunkBySizes(*sizes)

        assertEquals(listOf(emptyList<Int>()), result)
    }

    @Test
    fun `chunkBySizes should fail if sizes contain negative numbers`() {
        val input = listOf(1, 2, 3, 4, 5)
        val sizes = intArrayOf(2, -1, 3)

        assertFailsWith<IllegalArgumentException> {
            input.chunkBySizes(*sizes)
        }
    }

    @Test
    fun `chunkBySizes should fail if there's not enough elements for given sizes`() {
        val input = listOf(1, 2, 3)
        val sizes = intArrayOf(2, 3)

        assertFailsWith<IllegalArgumentException> {
            input.chunkBySizes(*sizes)
        }
    }

    @Test
    fun `chunkBySizes should fail if there are unprocessed elements left`() {
        val input = listOf(1, 2, 3, 4)
        val sizes = intArrayOf(2, 1)

        assertFailsWith<IllegalArgumentException> {
            input.chunkBySizes(*sizes)
        }
    }
}
