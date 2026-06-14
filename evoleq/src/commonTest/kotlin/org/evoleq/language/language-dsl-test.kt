package org.evoleq.language

import kotlin.test.Test
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
        assertEquals(1, l.value.size)

        val f = l.value[0] as Lang.Variable

        assertIs<Lang.Variable>(f)
        val v = l.find("var")
        assertIs<Lang.Variable>(v)
        assertEquals("7", v.value )
    }

    @Test fun dslTest() {
        val l = texts {
            key = "testKey"

            "var_1" colon "7"
            "block_1" block {
                "var_2" colon "1"
                "var_3" colon "2"
                "block_2" block {

                }
            }
            "var_3" to "3"
            "block_3" to {
                "var_4" colon "4"
            }
        }
    }
}
