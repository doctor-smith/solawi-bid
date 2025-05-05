package org.evoleq.math

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsDoubleTest {
    @Test fun isDouble(){
        assertTrue { "2.0".isDouble(2) }
        assertTrue { "2".isDouble(2) }
        assertTrue { "2.".isDouble(2) }
        assertTrue { ".0".isDouble(2) }
        assertTrue { "2.01".isDouble(2) }
        assertTrue { ".04".isDouble(2) }
        assertTrue { "2.0".isDouble(2) }
        assertTrue { "".isDouble() }
        assertTrue { ".".isDouble() }

        assertFalse { ".999".isDouble(2) }
    }
}