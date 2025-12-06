package org.evoleq.math

import kotlin.test.Test
import kotlin.test.assertEquals

class ReaderTest {

    @Test fun pairSourcesTest() {
        val one = 1
        val two = "2"
        val source1 = Source{ one }
        val source2 = Source{ two }
        val source1x2 = source1 x source2
        val expected = one x two
        val result = source1x2.emit()
        assertEquals(expected, result)
    }
}
