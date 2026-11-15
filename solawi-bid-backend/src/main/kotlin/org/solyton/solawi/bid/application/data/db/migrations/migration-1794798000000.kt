package org.solyton.solawi.bid.application.data.db.migrations

import org.evoleq.exposedx.migrations.Migration
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.selectAll
import org.solyton.solawi.bid.module.application.repository.addApplicationDependency
import org.solyton.solawi.bid.module.application.schema.ApplicationDependenciesTable
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable

/**
 * SMA-615: additive global catalog relation, with precisely two required edges.
 * No booking actions, module grants, permission contexts or tenant mutations.
 */
class Migration1794798000000(override val database: Database) : Migration {
    override val id: Long = 1794798000000

    override suspend fun Transaction.up() {
        // Check all prerequisites BEFORE the DDL (MariaDB DDL can auto-commit).
        val names = setOf("SHARE_MANAGEMENT", "BANKING", "DISTRIBUTION")
        val byName = ApplicationsTable.selectAll().associate { row ->
            row[ApplicationsTable.name] to row[ApplicationsTable.id].value
        }
        val missing = names - byName.keys
        check(missing.isEmpty()) {
            "SMA-615 cannot seed application_dependencies; missing applications: " +
                missing.sorted().joinToString(", ")
        }

        SchemaUtils.create(ApplicationDependenciesTable)
        addApplicationDependency(
            applicationId = byName.getValue("SHARE_MANAGEMENT"),
            requiredApplicationId = byName.getValue("BANKING"),
            actor = UUID_ZERO
        )
        addApplicationDependency(
            applicationId = byName.getValue("SHARE_MANAGEMENT"),
            requiredApplicationId = byName.getValue("DISTRIBUTION"),
            actor = UUID_ZERO
        )
    }

    override suspend fun Database.down() {
        error("SMA-615 has no automatic destructive down migration")
    }
}
