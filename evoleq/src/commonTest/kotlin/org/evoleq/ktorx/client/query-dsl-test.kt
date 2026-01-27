package org.evoleq.ktorx.client

import kotlin.test.Test
import kotlin.test.assertEquals

class QueryDSLTest {

    @Test fun queryTest() {
        val queryParams = queryParams {
            "p1"+=1
            "p2"+=2
        }
        assertEquals(
            listOf(
                "p1" to "1",
                "p2" to "2"
            ),
            queryParams
        )
    }

    @Test
    fun queryTestWithSetParams() {
        val queryParams = queryParams {
            "single"+= 1
            "multi"+=setOf(
                1,2,3,4,5
            )
        }
        assertEquals(
            listOf(
                "single" to "1",
                "multi" to "1",
                "multi" to "2",
                "multi" to "3",
                "multi" to "4",
                "multi" to "5",
            ),
            queryParams
        )
    }

    @Test
    fun queryString() {
        val params = queryParams {
            "p" += "p"
            "q" += setOf(
                1, 2
            )
        }.toQueryString()

        assertEquals(
            "?p=p&q=1&q=2",
            params
        )
    }
}
