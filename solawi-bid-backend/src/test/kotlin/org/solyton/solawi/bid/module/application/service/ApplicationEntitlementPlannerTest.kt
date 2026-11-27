package org.solyton.solawi.bid.module.application.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.solyton.solawi.bid.Unit
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationEntitlementPlannerTest {
    private val shares = id(1)
    private val banking = id(2)
    private val distribution = id(3)
    private val other = id(4)
    private val auctions = id(5)
    private val missing = id(99)
    private val sharesPurchase = id(101)
    private val bankingPurchase = id(102)
    private val otherPurchase = id(103)

    private fun id(value: Long): UUID = UUID(0L, value)

    private fun planner(
        vararg extraEdges: ApplicationDependency
    ): ApplicationEntitlementPlanner {
        val catalog = ApplicationDependencyCatalog(
            applicationNames = mapOf(
                shares to "SHARE_MANAGEMENT",
                banking to "BANKING",
                distribution to "DISTRIBUTION",
                other to "OTHER",
                auctions to "AUCTIONS"
            ),
            dependencies = setOf(
                ApplicationDependency(shares, banking),
                ApplicationDependency(shares, distribution),
                ApplicationDependency(other, banking)
            ) + extraEdges
        )
        return ApplicationEntitlementPlanner(ApplicationDependencyResolver(catalog))
    }

    private fun root(id: UUID, vararg apps: UUID): ApplicationGrantRoot =
        ApplicationGrantRoot(id, apps.toSet())

    @Unit @Test
    fun sharesAndDirectBankingHaveTwoIndependentSources() {
        val plan = planner().plan(
            listOf(root(sharesPurchase, shares), root(bankingPurchase, banking))
        )
        assertEquals(setOf(shares, banking, distribution), plan.applicationClosure)
        assertEquals(listOf(banking, distribution, shares), plan.activationOrder)
        assertEquals(setOf(sharesPurchase, bankingPurchase), plan.sourcesFor(banking))
        assertEquals(setOf(sharesPurchase), plan.sourcesFor(distribution))
        assertEquals(setOf(sharesPurchase), plan.sourcesFor(shares))
        assertTrue(
            ApplicationEntitlementContribution(
                sharesPurchase, banking, ApplicationGrantReason.DEPENDENCY
            ) in plan.contributions
        )
        assertTrue(
            ApplicationEntitlementContribution(
                bankingPurchase, banking, ApplicationGrantReason.EXPLICIT
            ) in plan.contributions
        )
    }

    @Unit @Test
    fun endingSharesDoesNotRemoveSeparatelyPurchasedBanking() {
        val plan = planner().plan(listOf(root(bankingPurchase, banking)))
        assertEquals(setOf(banking), plan.applicationClosure)
        assertEquals(setOf(bankingPurchase), plan.sourcesFor(banking))
        assertTrue(plan.sourcesFor(shares).isEmpty())
        assertTrue(plan.sourcesFor(distribution).isEmpty())
    }

    @Unit @Test
    fun twoUnrelatedRootsCanBothContributeBanking() {
        val plan = planner().plan(listOf(root(sharesPurchase, shares), root(otherPurchase, other)))
        assertEquals(setOf(sharesPurchase, otherPurchase), plan.sourcesFor(banking))
        assertEquals(
            setOf(
                ApplicationEntitlementContribution(sharesPurchase, banking, ApplicationGrantReason.DEPENDENCY),
                ApplicationEntitlementContribution(otherPurchase, banking, ApplicationGrantReason.DEPENDENCY)
            ),
            plan.contributions.filter { it.applicationId == banking }.toSet()
        )
    }

    @Unit @Test
    fun repeatedIdenticalRootIsIdempotent() {
        val planner = planner()
        val first = root(sharesPurchase, shares)
        assertEquals(planner.plan(listOf(first)), planner.plan(listOf(first, first)))
    }

    @Unit @Test
    fun sourceOrderDoesNotChangeThePlan() {
        val planner = planner()
        val a = root(sharesPurchase, shares)
        val b = root(bankingPurchase, banking)
        assertEquals(planner.plan(listOf(a, b)), planner.plan(listOf(b, a)))
    }

    @Unit @Test
    fun explicitApplicationWinsOverTransitiveDependencyForTheSameRoot() {
        val plan = planner().plan(listOf(root(sharesPurchase, shares, banking)))
        assertEquals(
            setOf(ApplicationEntitlementContribution(sharesPurchase, banking, ApplicationGrantReason.EXPLICIT)),
            plan.contributions.filter { it.applicationId == banking }.toSet()
        )
    }

    @Unit @Test
    fun conflictingDefinitionOfOneRootFailsClosed() {
        assertThrows<InvalidApplicationGrantRoot.ConflictingDefinition> {
            planner().plan(listOf(root(sharesPurchase, shares), root(sharesPurchase, banking)))
        }
    }

    @Unit @Test
    fun emptyAndReservedRootsAreRejected() {
        assertThrows<InvalidApplicationGrantRoot.Empty> {
            planner().plan(listOf(root(sharesPurchase)))
        }
        assertThrows<InvalidApplicationGrantRoot.ReservedIdentity> {
            planner().plan(listOf(root(UUID(0L, 0L), shares)))
        }
    }

    @Unit @Test
    fun missingApplicationFailsWithoutPartialPlan() {
        assertThrows<InvalidApplicationDependency.MissingApplication> {
            planner().plan(listOf(root(sharesPurchase, shares), root(otherPurchase, missing)))
        }
    }

    @Unit @Test
    fun cyclesFailEvenIfNotReachableFromCurrentRoots() {
        val loop = ApplicationDependency(auctions, auctions)
        assertThrows<InvalidApplicationDependency.SelfDependency> {
            planner(loop).plan(listOf(root(sharesPurchase, shares)))
        }
    }

    @Unit @Test
    fun emptyRootSetGivesNoEntitlementsAndValidatesCatalog() {
        val plan = planner().plan(emptyList())
        assertTrue(plan.applicationClosure.isEmpty())
        assertTrue(plan.activationOrder.isEmpty())
        assertTrue(plan.contributions.isEmpty())
    }

    @Unit @Test
    fun auctionsWithoutIntegratedModeDoNotImplicitlyRequireShares() {
        val plan = planner().plan(listOf(root(otherPurchase, auctions)))
        assertEquals(setOf(auctions), plan.applicationClosure)
        assertTrue(plan.sourcesFor(shares).isEmpty())
    }
}
