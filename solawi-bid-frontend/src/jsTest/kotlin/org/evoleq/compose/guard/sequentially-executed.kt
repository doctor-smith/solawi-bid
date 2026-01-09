package org.evoleq.compose.guard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.evoleq.compose.guard.data.ConditionalActionInput
import org.evoleq.compose.guard.data.ExecutableAction
import org.evoleq.compose.guard.data.onFulfilled
import org.evoleq.compose.guard.data.sequentiallyExecuted
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SequentialExecutionTest {

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun sequentiallyExecutedOneAction() = runTest {
        val read = { ConditionalActionInput(5,5) }
        var r1 = 0

        composition {
            val result = sequentiallyExecuted(
                onFulfilled(
                    source = read,
                    predicate = { x: Int -> x < 10 },
                    action = {a: Int -> r1 = a*a}
                )
            )
            assertTrue { result }
            assertEquals(25, r1)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun sequentiallyExecutedOneAction2() = runTest {
        val read = { ConditionalActionInput(5,5) }
        var r1 = 0

        composition {
            val result = sequentiallyExecuted(
                onFulfilled(
                    source = read,
                    predicate = { x: Int -> x > 10 },
                    action = {a: Int -> r1 = a*a}
                )
            )
            assertFalse { result }
            assertEquals(0, r1)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun sequentiallyExecutedFirstActionOfTwo() = runTest {
        val read = { ConditionalActionInput(5,5) }
        var r1 = 0
        var r2 = 0

        composition {
            val result = sequentiallyExecuted(
                onFulfilled(
                    source = read,
                    predicate = { x: Int -> x < 10 },
                    action = {a: Int -> r1 = a*a}
                ),
                onFulfilled(
                    source = read,
                    predicate = { x: Int -> x < 10 },
                    action = {a: Int -> r2 = a*a}
                )
            )
            assertTrue { result }
            assertEquals(25, r1)
            assertEquals(0, r2)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun sequentiallyExecutedSecondActionOfTwo() = runTest {
        val read = { ConditionalActionInput(5,5) }
        var r1 = 0
        var r2 = 0

        composition {
            val result = sequentiallyExecuted(
                onFulfilled(
                    source = read,
                    predicate = { x: Int -> x > 10 },
                    action = {a: Int -> r1 = a*a}
                ),
                onFulfilled(
                    source = read,
                    predicate = { x: Int -> x < 10 },
                    action = {a: Int -> r2 = a*a}
                )
            )
            assertTrue { result }
            assertEquals(0, r1)
            assertEquals(25, r2)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun sequentiallyExecutedNoActionOfTwo() = runTest {
        val read = { ConditionalActionInput(5,5) }
        var r1 = 0
        var r2 = 0

        composition {
            val result = sequentiallyExecuted(
                onFulfilled(
                    source = read,
                    predicate = { x: Int -> x > 10 },
                    action = {a: Int -> r1 = a*a}
                ),
                onFulfilled(
                    source = read,
                    predicate = { x: Int -> x > 10 },
                    action = {a: Int -> r2 = a*a}
                )
            )
            assertFalse { result }
            assertEquals(0, r1)
            assertEquals(0, r2)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun sequentiallyExecutedCanSucceedAfterRecomposition() = runTest {
        composition {
            var gate by remember { mutableStateOf(false) }
            var effectCalls = 0

            val gated = ExecutableAction(
                inputs = ConditionalActionInput(Unit, Unit),
                action = { _ ->
                    { _ ->
                        if (gate) {
                            effectCalls++
                            true
                        } else {
                            false
                        }
                    }
                }
            )

            // 1) First execution: gate=false => no success
            val first = sequentiallyExecuted(gated)
            assertFalse(first)
            assertEquals(0, effectCalls)

            // 2) Change state -> trigger recomposition
            gate = true

            // After recomposition it is re-evaluated: now successful
            val second = sequentiallyExecuted(gated)
            assertTrue(second)
            assertEquals(1, effectCalls)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun sequentiallyExecutedPulsesThroughActionsWhereEachActionTriggersRecomposition() = runTest {
        composition {
            // Controls which action is "next" in this recompose cycle
            var step by remember { mutableStateOf(0) }

            // For verification: which actions were really executed?
            var a1Calls = 0
            var a2Calls = 0
            var a3Calls = 0

            val a1 = ExecutableAction(
                inputs = ConditionalActionInput(Unit, Unit),
                action = { _ ->
                    { _ ->
                        if (step == 0) {
                            a1Calls++
                            step = 1 // triggers recomposition
                            true
                        } else false
                    }
                }
            )

            val a2 = ExecutableAction(
                inputs = ConditionalActionInput(Unit, Unit),
                action = { _ ->
                    { _ ->
                        if (step == 1) {
                            a2Calls++
                            step = 2 // triggers recomposition
                            true
                        } else false
                    }
                }
            )

            val a3 = ExecutableAction(
                inputs = ConditionalActionInput(Unit, Unit),
                action = { _ ->
                    { _ ->
                        if (step == 2) {
                            a3Calls++
                            step = 3 // triggers recomposition
                            true
                        } else false
                    }
                }
            )

            // 1st pulse: a1 executes, a2/a3 not executed (Short-Circuit)
            val r1 = sequentiallyExecuted(a1, a2, a3)
            assertTrue(r1)
            assertEquals(1, a1Calls)
            assertEquals(0, a2Calls)
            assertEquals(0, a3Calls)

            // 2nd pulse (after recomposition): a2 executes
            val r2 = sequentiallyExecuted(a1, a2, a3)
            assertTrue(r2)
            assertEquals(1, a1Calls)
            assertEquals(1, a2Calls)
            assertEquals(0, a3Calls)

            // 3rd pulse: a3 executes
            val r3 = sequentiallyExecuted(a1, a2, a3)
            assertTrue(r3)
            assertEquals(1, a1Calls)
            assertEquals(1, a2Calls)
            assertEquals(1, a3Calls)

            // 4th pulse: step==3, no action executes anymore => false, no more calls
            val r4 = sequentiallyExecuted(a1, a2, a3)
            assertFalse(r4)
            assertEquals(1, a1Calls)
            assertEquals(1, a2Calls)
            assertEquals(1, a3Calls)
        }
    }
}
