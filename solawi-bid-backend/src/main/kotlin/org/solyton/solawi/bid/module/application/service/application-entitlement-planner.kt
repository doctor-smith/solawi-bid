package org.solyton.solawi.bid.module.application.service

import java.util.UUID

/**
 * One explicit source of application access (e.g. a purchase, trial or bundle).
 *
 * The caller must authorize, activate and scope every root to exactly one tenant
 * before passing it to this pure planner. A root by itself is NOT a permission.
 */
data class ApplicationGrantRoot(
    val rootId: UUID,
    val applicationIds: Set<UUID>
)

enum class ApplicationGrantReason {
    EXPLICIT,
    DEPENDENCY
}

/**
 * Explains one application's presence in the plan.
 *
 * A root can contribute an application only once: if the application is both
 * explicit and transitively reachable, EXPLICIT takes precedence for that root.
 */
data class ApplicationEntitlementContribution(
    val rootId: UUID,
    val applicationId: UUID,
    val reason: ApplicationGrantReason
)

/**
 * Read-only result; neither tenant entitlements nor user roles are changed.
 */
data class ApplicationEntitlementPlan(
    val applicationClosure: Set<UUID>,
    val activationOrder: List<UUID>,
    val contributions: Set<ApplicationEntitlementContribution>
) {
    fun sourcesFor(applicationId: UUID): Set<UUID> =
        contributions.asSequence()
            .filter { it.applicationId == applicationId }
            .map { it.rootId }
            .toSet()
}

sealed class InvalidApplicationGrantRoot(message: String) : IllegalArgumentException(message) {
    class ReservedIdentity :
        InvalidApplicationGrantRoot("Grant root ID must not be the zero UUID")

    class Empty(val rootId: UUID) :
        InvalidApplicationGrantRoot("Grant root must have at least one explicit application: $rootId")

    class ConflictingDefinition(val rootId: UUID) :
        InvalidApplicationGrantRoot("The same grant root has conflicting application sets: $rootId")
}

/**
 * Plans an application closure together with its *independent origins*.
 *
 * This layer is intentionally tenant-agnostic. It must NOT be called with
 * mixed-tenant roots: authorization and tenant binding belong to SMA-600 after
 * the SMA-597/598 tenant model is agreed. The result is not a grant operation.
 *
 * The planner deliberately reuses the global database-backed DAG resolver;
 * it does not infer entitlements from isMandatory or from Kotlin dependencies.
 */
class ApplicationEntitlementPlanner(private val resolver: ApplicationDependencyResolver) {

    fun plan(roots: Collection<ApplicationGrantRoot>): ApplicationEntitlementPlan {
        // Validate all sources before producing any output; identical repeated
        // requests are idempotent, conflicting redefinitions fail closed.
        roots.forEach { root ->
            if (root.rootId == UUID(0L, 0L)) {
                throw InvalidApplicationGrantRoot.ReservedIdentity()
            }
            if (root.applicationIds.isEmpty()) {
                throw InvalidApplicationGrantRoot.Empty(root.rootId)
            }
        }

        val normalizedRoots = roots.groupBy { it.rootId }.entries.map { (id, definitions) ->
            val first = definitions.first()
            if (definitions.any { it.applicationIds != first.applicationIds }) {
                throw InvalidApplicationGrantRoot.ConflictingDefinition(id)
            }
            first
        }.sortedBy { it.rootId.toString() }

        // resolve(emptySet()) still validates the entire catalog.
        val requested = normalizedRoots.flatMap { it.applicationIds }.toSet()
        val resolution = resolver.resolve(requested)

        val contributions = linkedSetOf<ApplicationEntitlementContribution>()
        normalizedRoots.forEach { root ->
            val reachable = resolver.resolve(root.applicationIds).applicationClosure
            // Use the global topological order to keep the result deterministic.
            resolution.activationOrder.filter { it in reachable }.forEach { applicationId ->
                contributions += ApplicationEntitlementContribution(
                    rootId = root.rootId,
                    applicationId = applicationId,
                    reason = if (applicationId in root.applicationIds) {
                        ApplicationGrantReason.EXPLICIT
                    } else {
                        ApplicationGrantReason.DEPENDENCY
                    }
                )
            }
        }

        return ApplicationEntitlementPlan(
            applicationClosure = resolution.applicationClosure,
            activationOrder = resolution.activationOrder,
            contributions = contributions
        )
    }
}
