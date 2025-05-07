package org.evoleq.language

import kotlin.test.assertEquals
import kotlin.test.assertIs

class LanguageDslTest {

    // @Test
    fun dsl() {
        val l = texts {
            key = "testKey"
            variable {
                key = "var"
                value = "7"
            }
        }

        assertEquals(l.key, "testKey")
        assertIs<Lang.Block>(l)
        console.log("l = $l")
        assertEquals(1, l.value.size)

        val f = l.value[0] as Lang.Variable

        assertIs<Lang.Variable>(f)
        val v = l.find("var")
        assertIs<Lang.Variable>(v)
        assertEquals("7", v.value )
    }
}