package org.evoleq.optics.storage

import kotlin.test.Test
import kotlin.test.assertEquals

class StorageApiTest {

    @Test fun nextId() {

        val map = mapOf(
            1 to ""
        )
        val n1 = map.nextId()
        assertEquals(2, n1)

        val map2 = mapOf(
            2 to ""
        )
        assertEquals(1, map2.nextId())

        val map3 = mapOf(
            1 to "",
            2 to ""
        )
        assertEquals(3, map3.nextId())

        val map4 = mapOf(
            1 to "",
            3 to ""
        )
        assertEquals(2, map4.nextId())

        val map5 = mapOf(
            1 to "",
            2 to "",
            3 to "",
            4 to "",
            5 to ""
        )
        assertEquals(6, map5.nextId())

        val map6 = mapOf(
            1 to "",
            2 to "",
            3 to "",
            4 to "",
            6 to ""
        )
        assertEquals(5, map6.nextId())
    }
}
