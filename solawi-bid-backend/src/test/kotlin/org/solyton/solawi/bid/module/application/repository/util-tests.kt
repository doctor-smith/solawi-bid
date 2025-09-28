package org.solyton.solawi.bid.module.application.repository

import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Unit
import kotlin.test.assertEquals

class UtilityTests {
    @Unit@Test fun reduceContextNameTest_1() {
        val contextName = "name.app_id.user_Id"

        assertEquals("name", contextName.reduceContextName())
    }
    @Unit@Test fun reduceContextNameTest_2() {
        val contextName = "name.app_id"

        assertEquals("name", contextName.reduceContextName())
    }
    @Unit@Test fun reduceContextNameTest_3() {
        val contextName = "name"

        assertEquals("name", contextName.reduceContextName())
    }
    @Unit@Test fun reduceContextNameTest_4() {
        val contextName = "name.something.app_id.userId"

        assertEquals("name.something", contextName.reduceContextName())
    }
}
