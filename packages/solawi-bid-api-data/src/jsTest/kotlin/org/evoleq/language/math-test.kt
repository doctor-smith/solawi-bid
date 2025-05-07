package org.evoleq.language

import kotlin.test.Test
import kotlin.test.assertTrue

class MathTest {

    @Test
    fun isDoubleTest() {
        assertTrue {
            "1.0".isDouble(Locale.Iso)

        }
        assertTrue {
            ".238".isDouble(Locale.Iso)
        }
        assertTrue {
            "222.222".isDouble(Locale.Iso)
        }

        assertTrue {
            "222.".isDouble(Locale.Iso)
        }
    }

}