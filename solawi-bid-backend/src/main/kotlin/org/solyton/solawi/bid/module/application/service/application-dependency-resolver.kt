package org.solyton.solawi.bid.module.application.service

import java.util.UUID

/** A directed edge: applicationId requires requiredApplicationId. */
data class ApplicationDependency(
    val applicationId: UUID,
    val requiredApplicationId: UUID
)

/**
 * Immutable view of the global application catalog. Names are used only
 * for stable ordering and helpful diagnostics; IDs form the graph.
 */
data class ApplicationDependencyCatalog(
    val applicationNames: Map<UUID, String>,
    val dependencies: Set<ApplicationDependency>
)

data class ApplicationDependencyResolution(
    val applicationClosure: Set<UUID>,
    /** Required applications appear before the applications that depend on them. */
    val activationOrder: List<UUID>
)

sealed class InvalidApplicationDependency(message: String) : IllegalArgumentException(message) {
    class MissingApplication(val applicationId: UUID) :
        InvalidApplicationDependency("Application not found in dependency catalog: " + applicationId)

    class SelfDependency(val applicationId: UUID) :
        InvalidApplicationDependency("An application cannot depend on itself: " + applicationId)

    class Cycle(val path: List<UUID>) :
        InvalidApplicationDependency("Cycle in application dependency catalog: " + path.joinToString(" -> "))
}

/**
 * Pure, read-only DAG resolver. It deliberately knows nothing about bookings,
 * owners, mandatory modules, tenants, RRCT or permissions.
 */
class ApplicationDependencyResolver(private val catalog: ApplicationDependencyCatalog) {
    private val byApplication = catalog.dependencies.groupBy { it.applicationId }
    private val applicationOrder = compareBy<UUID>(
        { catalog.applicationNames[it] ?: "" },
        { it.toString() }
    )

    private fun requirementsOf(applicationId: UUID): List<UUID> =
        byApplication[applicationId].orEmpty()
            .map { it.requiredApplicationId }
            .distinct()
            .sortedWith(applicationOrder)

    /**
     * Validates *all* catalog edges, not just those reachable from a root.
     * A deterministic postorder is also the dependency-first topological order.
     */
    fun validateAndOrderCatalog(): List<UUID> {
        catalog.dependencies.forEach { edge ->
            if (edge.applicationId == edge.requiredApplicationId) {
                throw InvalidApplicationDependency.SelfDependency(edge.applicationId)
            }
            if (edge.applicationId !in catalog.applicationNames) {
                throw InvalidApplicationDependency.MissingApplication(edge.applicationId)
            }
            if (edge.requiredApplicationId !in catalog.applicationNames) {
                throw InvalidApplicationDependency.MissingApplication(edge.requiredApplicationId)
            }
        }

        val visiting = mutableSetOf<UUID>()
        val visited = mutableSetOf<UUID>()
        val path = mutableListOf<UUID>()
        val ordered = mutableListOf<UUID>()

        fun visit(applicationId: UUID) {
            if (applicationId in visiting) {
                throw InvalidApplicationDependency.Cycle(
                    path.drop(path.indexOf(applicationId)) + applicationId
                )
            }
            if (applicationId in visited) return

            visiting.add(applicationId)
            path.add(applicationId)
            requirementsOf(applicationId).forEach(::visit)
            path.removeAt(path.lastIndex)
            visiting.remove(applicationId)
            visited.add(applicationId)
            ordered.add(applicationId)
        }

        catalog.applicationNames.keys.sortedWith(applicationOrder).forEach(::visit)
        return ordered
    }

    fun resolve(requestedApplications: Set<UUID>): ApplicationDependencyResolution {
        val catalogOrder = validateAndOrderCatalog()
        requestedApplications.forEach { id ->
            if (id !in catalog.applicationNames) {
                throw InvalidApplicationDependency.MissingApplication(id)
            }
        }

        val closure = mutableSetOf<UUID>()
        fun include(applicationId: UUID) {
            if (!closure.add(applicationId)) return
            requirementsOf(applicationId).forEach(::include)
        }
        requestedApplications.sortedWith(applicationOrder).forEach(::include)

        return ApplicationDependencyResolution(
            applicationClosure = closure.toSet(),
            activationOrder = catalogOrder.filter { it in closure }
        )
    }
}
