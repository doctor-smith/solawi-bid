package org.evoleq.ktorx.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ApiTest {
    @Test fun configureApi() {
        class Key1
        class Key2

        val api = Api{
            post<Int, String>(
                key = Key1::class,
                url = "test-url-1"
            )
            get<Int, String>(
                key = Key2::class,
                url = "test-url-2"
            )
        }

        assertEquals(2, api.keys.size)
        assertIs<EndPoint.Post<Int,String>>(api[Key1::class])
        assertIs<EndPoint.Get<Int,String>>(api[Key2::class])
    }
}
