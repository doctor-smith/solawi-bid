package org.solyton.solawi.bid.module.application.repository

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.application.schema.ApplicationDependenciesTable
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable
import org.solyton.solawi.bid.module.application.service.ApplicationDependencyResolver
import org.solyton.solawi.bid.module.application.service.InvalidApplicationDependency
import org.solyton.solawi.bid.module.permission.repository.createRootContext
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApplicationDependenciesRepositoryTest {
    private val tables = arrayOf(ContextsTable, ApplicationsTable, ApplicationDependenciesTable)

    @DbFunctional @Test
    fun cat01SeedIsIdempotentAndContainsExactlyTwoEdges() = runSimpleH2Test(*tables) {
        createRootContext("EMPTY")
        val shares = createApplication("SHARE_MANAGEMENT", "Shares", UUID_ZERO)
        val banking = createApplication("BANKING", "Banking", UUID_ZERO)
        val distribution = createApplication("DISTRIBUTION", "Distribution", UUID_ZERO)

        assertTrue(addApplicationDependency(shares.id.value, banking.id.value))
        assertTrue(addApplicationDependency(shares.id.value, distribution.id.value))
        assertFalse(addApplicationDependency(shares.id.value, banking.id.value))
        assertFalse(addApplicationDependency(shares.id.value, distribution.id.value))
        assertEquals(2, ApplicationDependenciesTable.selectAll().count())

        val resolved = ApplicationDependencyResolver(readApplicationDependencyCatalog())
            .resolve(setOf(shares.id.value))
        assertEquals(setOf(shares.id.value, banking.id.value, distribution.id.value), resolved.applicationClosure)
        assertEquals(shares.id.value, resolved.activationOrder.last())
    }

    @DbFunctional @Test
    fun cat05NewCatalogEdgeIsReadDynamicallyWithoutCodeChanges() = runSimpleH2Test(*tables) {
        createRootContext("EMPTY")
        val a = createApplication("A", "A", UUID_ZERO).id.value
        val b = createApplication("B", "B", UUID_ZERO).id.value
        val c = createApplication("C", "C", UUID_ZERO).id.value

        addApplicationDependency(a, b)
        val first = ApplicationDependencyResolver(readApplicationDependencyCatalog()).resolve(setOf(a))
        assertEquals(setOf(a, b), first.applicationClosure)

        addApplicationDependency(b, c)
        val second = ApplicationDependencyResolver(readApplicationDependencyCatalog()).resolve(setOf(a))
        assertEquals(setOf(a, b, c), second.applicationClosure)
        assertEquals(listOf(c, b, a), second.activationOrder)
    }

    @DbFunctional @Test
    fun cat03RejectedCycleDoesNotWritePartialEdge() = runSimpleH2Test(*tables) {
        createRootContext("EMPTY")
        val a = createApplication("A", "A", UUID_ZERO).id.value
        val b = createApplication("B", "B", UUID_ZERO).id.value
        val c = createApplication("C", "C", UUID_ZERO).id.value
        addApplicationDependency(a, b)
        addApplicationDependency(b, c)

        assertThrows<InvalidApplicationDependency.Cycle> {
            addApplicationDependency(c, a)
        }
        assertEquals(2, ApplicationDependenciesTable.selectAll().count())
    }

    @DbFunctional @Test
    fun cat06MissingApplicationRejectsMutationWithoutPartialWrite() = runSimpleH2Test(*tables) {
        createRootContext("EMPTY")
        val a = createApplication("A", "A", UUID_ZERO).id.value
        assertThrows<InvalidApplicationDependency.MissingApplication> {
            addApplicationDependency(a, java.util.UUID.randomUUID())
        }
        assertEquals(0, ApplicationDependenciesTable.selectAll().count())
    }
}
