package org.evoleq.optics.storage

import kotlin.test.Test
import kotlin.test.assertEquals

class ActionsTest {
    object SayHello : ActionType

    @Test
    fun actions() {


        var result = ""
        val text = "hello"
        val actions = Actions(
            hashMapOf(
                SayHello to { result = text }
            )
        )

        actions.dispatch(SayHello)
        assertEquals(text, result)
    }
}