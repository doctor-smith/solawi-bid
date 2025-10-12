package org.solyton.solawi.bid.module.permissions.service

import org.solyton.solawi.bid.module.permissions.data.Context
import kotlin.test.Test
import kotlin.test.assertEquals

class ContextServiceTests {
    @Test fun readableNameTest1()  {
        val contextName = "context-name.uuid1.uuid2"
        val context = Context(contextName = contextName)
        val expected = "context-name"

        assertEquals(expected, context.readableName())
    }

    @Test fun readableNameTest2()  {
        val contextName = "context-name.uuid1"
        val context = Context(contextName = contextName)
        val expected = "context-name"

        assertEquals(expected, context.readableName())
    }

    @Test fun readableNameTest3()  {
        val contextName = "context-name"
        val context = Context(contextName = contextName)
        val expected = "context-name"

        assertEquals(expected, context.readableName())
    }
}
