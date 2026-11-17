package org.solyton.solawi.bid.module.application.repository

import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.solyton.solawi.bid.module.application.schema.ApplicationDependenciesTable
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable
import org.solyton.solawi.bid.module.application.service.ApplicationDependency
import org.solyton.solawi.bid.module.application.service.ApplicationDependencyCatalog
import org.solyton.solawi.bid.module.application.service.ApplicationDependencyResolver
import java.util.UUID

/**
 * Reads applications and outgoing edges in one SELECT (one database snapshot
 * even when the caller uses READ COMMITTED). Call inside an Exposed transaction.
 */
fun Transaction.readApplicationDependencyCatalog(): ApplicationDependencyCatalog {
    val rows = ApplicationsTable.join(
        ApplicationDependenciesTable,
        JoinType.LEFT,
        onColumn = ApplicationsTable.id,
        otherColumn = ApplicationDependenciesTable.applicationId
    ).selectAll().toList()

    val names = rows.associate { row ->
        row[ApplicationsTable.id].value to row[ApplicationsTable.name]
    }
    val edges = rows.mapNotNull { row ->
        val required = row.getOrNull(ApplicationDependenciesTable.requiredApplicationId)
            ?: return@mapNotNull null
        ApplicationDependency(row[ApplicationsTable.id].value, required.value)
    }.toSet()
    return ApplicationDependencyCatalog(names, edges)
}

/**
 * Idempotent catalog insert; checks missing IDs, self-edges and cycles before
 * any write. Caller owns the transaction, so exceptions roll it back.
 * Unique FK constraints also protect direct database writers.
 */
fun Transaction.addApplicationDependency(
    applicationId: UUID,
    requiredApplicationId: UUID,
    actor: UUID = UUID_ZERO
): Boolean {
    val current = readApplicationDependencyCatalog()
    val edge = ApplicationDependency(applicationId, requiredApplicationId)
    if (edge in current.dependencies) return false

    ApplicationDependencyResolver(
        current.copy(dependencies = current.dependencies + edge)
    ).validateAndOrderCatalog()

    ApplicationDependenciesTable.insert {
        it[ApplicationDependenciesTable.applicationId] = applicationId
        it[ApplicationDependenciesTable.requiredApplicationId] = requiredApplicationId
        it[createdBy] = actor
    }
    return true
}
