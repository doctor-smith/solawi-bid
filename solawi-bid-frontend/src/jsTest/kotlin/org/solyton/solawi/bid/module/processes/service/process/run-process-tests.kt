package org.solyton.solawi.bid.module.processes.service.process

import org.evoleq.optics.storage.ActionEnvelope
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.process.service.process.sequence
import kotlin.test.Test
import kotlin.test.assertEquals

class RunProcessTests {

    @Test
    fun seqProcessTest() {
        val action1 = Action<Application, Int, Int>(
            name = "action1",
            endPoint = Int::class,
            reader = { 1 },
            writer = {{it}}
        )

        val action2 = Action<Application, Int, Int>(
            name = "action2",
            endPoint = Int::class,
            reader = { 1 },
            writer = {{it}}
        )

        val action3 = Action<Application, Int, Int>(
            name = "action3",
            endPoint = Int::class,
            reader = { 1 },
            writer = { { it } }
        )

        val action4 = Action<Application, Int, Int>(
            name = "action4",
            endPoint = Int::class,
            reader = { 1 },
            writer = { { it } }
        )

        val envelope1 = ActionEnvelope<Application, Int, Int>(
            id = "test1",
            action = action1
        )

        val envelope2 = ActionEnvelope<Application, Int, Int>(
            id = "test2",
            action = action2
        )

        val envelope3 = ActionEnvelope<Application, Int, Int>(
            id = "test3",
            action = action3
        )

        val envelope4 = ActionEnvelope<Application, Int, Int>(
            id = "test4",
            action = action4
        )

        val result = sequence(envelope1, envelope2, envelope3, envelope4)

        val expected = envelope1.copy(
            next = listOf(
                envelope2.copy(
                    next = listOf(
                        envelope3.copy(
                            next = listOf(
                                envelope4
                            )
                        )
                    )
                )
            )
        )

        assertEquals(expected, result)
    }
}
