package org.evoleq.compose.guard

import org.evoleq.compose.guard.data.ConditionalActionInput
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
}
