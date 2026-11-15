package org.solyton.solawi.bid.module.application.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.solyton.solawi.bid.Unit
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationDependencyResolverTest {
    private val a = UUID.fromString("00000000-0000-0000-0000-000000000001")
    private val b = UUID.fromString("00000000-0000-0000-0000-000000000002")
    private val c = UUID.fromString("00000000-0000-0000-0000-000000000003")
    private val d = UUID.fromString("00000000-0000-0000-0000-000000000004")
    private val missing = UUID.fromString("00000000-0000-0000-0000-000000000099")

    private fun graph(vararg edges: Pair<UUID, UUID>): ApplicationDependencyResolver =
        ApplicationDependencyResolver(
            ApplicationDependencyCatalog(
                applicationNames = mapOf(a to "A", b to "B", c to "C", d to "D"),
                dependencies = edges.map { (from, to) -> ApplicationDependency(from, to) }.toSet()
            )
        )

    @Unit @Test
    fun cat02SelfEdgeIsInvalid() {
        assertThrows<InvalidApplicationDependency.SelfDependency> {
            graph(a to a).resolve(setOf(a))
        }
    }

    @Unit @Test
    fun cat03DirectCycleIsInvalid() {
        val error = assertThrows<InvalidApplicationDependency.Cycle> {
            graph(a to b, b to a).resolve(setOf(a))
        }
        assertEquals(listOf(a, b, a), error.path)
    }

    @Unit @Test
    fun cat03IndirectCycleIsInvalidEvenIfUnreachableFromRoot() {
        assertThrows<InvalidApplicationDependency.Cycle> {
            graph(a to b, b to c, c to a).resolve(setOf(d))
        }
    }

    @Unit @Test
    fun cat04DiamondHasOneSharedDependencyAndDependencyFirstOrder() {
        val resolved = graph(a to b, a to c, b to d, c to d).resolve(setOf(a))
        assertEquals(setOf(a, b, c, d), resolved.applicationClosure)
        assertEquals(listOf(d, b, c, a), resolved.activationOrder)
    }

    @Unit @Test
    fun cat04TwoRootsDeduplicateTransitiveRequirements() {
        val resolved = graph(a to c, b to c).resolve(setOf(a, b))
        assertEquals(listOf(c, a, b), resolved.activationOrder)
        assertEquals(3, resolved.applicationClosure.size)
    }

    @Unit @Test
    fun cat06MissingEdgeTargetFailsClosed() {
        assertThrows<InvalidApplicationDependency.MissingApplication> {
            graph(a to missing).resolve(setOf(b))
        }
    }

    @Unit @Test
    fun unknownRequestedApplicationFailsClosed() {
        assertThrows<InvalidApplicationDependency.MissingApplication> {
            graph().resolve(setOf(missing))
        }
    }

    @Unit @Test
    fun emptyRootSetHasEmptyClosureButStillValidatesCatalog() {
        assertTrue(graph(a to b).resolve(emptySet()).activationOrder.isEmpty())
    }
}
