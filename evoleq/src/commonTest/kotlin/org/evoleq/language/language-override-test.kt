package org.evoleq.language

import kotlin.test.Test
import kotlin.test.assertEquals

class LanguageOverrideTest {

    @Test fun `override single lang`() {
        val lang = "dialog" texts {
            "title" block {
                "value" colon "Title"
            }

            "other" colon "other"
        }


        val titleVar = Lang.Block(
            "title",
            listOf(Lang.Variable("value", "NewTitle"))
        )

        val result = lang.override(titleVar)

        val expected = "dialog" texts {
            "other" colon "other"
            "title" block {
                "value" colon "NewTitle"
            }

        }

        assertEquals(expected, result)
    }

    @Test fun `override many langs`() {
        val lang = "dialog" texts {
            "title" block {
                "value" colon "Title"
            }
            "other" colon "other"
        }


        val titleVar = Lang.Block(
            "title",
            listOf(Lang.Variable("value", "NewTitle"))
        )

        val otherVar = Lang.Variable(
            "other",
            "new other"
        )

        val result = lang.override(titleVar, otherVar)

        val expected = "dialog" texts {
            "title" block {
                "value" colon "NewTitle"
            }
            "other" colon "new other"
        }

        assertEquals(expected, result)
    }

}
