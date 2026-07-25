package org.evoleq.math

import kotlin.test.Test
import kotlin.test.assertEquals

class WriterTest {

    @Test
    fun appendItemTest() {
        val writer = Append<String>()

        val list = listOf("a","b","c")
        val result = writer write "d" on list

        assertEquals(listOf("a","b","c","d"), result)
    }
}
