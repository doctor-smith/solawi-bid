package org.solyton.solawi.bid.module.parser.csv

import org.solyton.solawi.bid.module.shared.parser.csv.ColumnType
import org.solyton.solawi.bid.module.shared.parser.csv.toColumnType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails


class ColumnTypeTest {
    @Test
    fun `test valid column type string 1`() {
        val result = "name:string.id".toColumnType()
        assertEquals(ColumnType("name", "string", "id"), result)

    }

    @Test
    fun `test valid column type string 2`() {
        val result2 = "title.key".toColumnType()
        assertEquals(ColumnType("title", null, "key"), result2)
    }

    @Test
    fun `test valid column type string 2 a`() {
        val result2 = "title:.key".toColumnType()
        assertEquals(ColumnType("title", null, "key"), result2)
    }

    @Test
    fun `test valid column type string 3`() {
        val result2 = "title:type".toColumnType()
        assertEquals(ColumnType("title", "type", null), result2)
    }

    @Test
    fun `test valid column type string 3 a`() {
        val result2 = "title:type.".toColumnType()
        assertEquals(ColumnType("title", "type", null), result2)
    }

    @Test
    fun `test valid column type string 4`() {
        val result2 = "title".toColumnType()
        assertEquals(ColumnType("title", null, null), result2)
    }

    @Test
    fun `test valid column type string 5`() {
        val result2 = "title:".toColumnType()
        assertEquals(ColumnType("title", null, null), result2)
    }

    @Test
    fun `test valid column type string 6`() {
        val result2 = "title.".toColumnType()
        assertEquals(ColumnType("title", null, null), result2)
    }
    @Test
    fun `test valid column type string 7`() {
        val result2 = "title:.".toColumnType()
        assertEquals(ColumnType("title", null, null), result2)
    }


    @Test
    fun `test invalid column type string 1`() {
        assertFails {
            ":type.key".toColumnType()
        }
    }

    @Test
    fun `test invalid column type string 2`() {
        assertFails {
            ":type.".toColumnType()
        }
    }

    @Test
    fun `test invalid column type string 3`() {
        assertFails {
            ":.key".toColumnType()
        }
    }
    @Test
    fun `test invalid column type string 4`() {
        assertFails {
            ":.".toColumnType()
        }
    }

    @Test
    fun `test invalid column type string 5`() {
        assertFails {
            ".key".toColumnType()
        }
    }

    @Test
    fun `test invalid column type string 6`() {
        assertFails {
            ".".toColumnType()
        }
    }
}
