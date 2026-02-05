package org.solyton.solawi.bid.module.parser.csv

import org.solyton.solawi.bid.module.shared.parser.csv.ColumnType
import org.solyton.solawi.bid.module.shared.parser.csv.toColumnType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails


class ColumnTypeTest {
    
    @Test
    fun `test valid column type string 1`() {
        val result = "name.string?key=value".toColumnType()
        assertEquals(ColumnType("name", "string", "value"), result)
    }

    @Test
    fun `test valid column type string 2`() {
        val result2 = "title?key=value".toColumnType()
        assertEquals(ColumnType("title", null, "value"), result2)
    }

    @Test
    fun `test column type without key value parameter`() {
        val result = "name".toColumnType()
        assertEquals(ColumnType("name", null, null), result)
    }

    @Test
    fun `test column type with type but without key value`() {
        val result = "name.string".toColumnType()
        assertEquals(ColumnType("name", "string", null), result)
    }

    @Test
    fun `test empty column type string`() {
        assertFails { "".toColumnType() }
    }
}
