package org.solyton.solawi.bid.application.data.db.migrations

import org.evoleq.exposedx.migrations.Migration
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable
import org.solyton.solawi.bid.module.application.schema.ModulesTable

/**
 * SMA-613: SEPA and DATEV are optional Banking modules.
 *
 * Corrects existing catalog data without changing historical migrations,
 * module registrations, bookings, contexts, rights or role assignments.
 * Generated at 2026-11-15T22:16:01.715Z (Unix milliseconds: 1794780961715).
 */
class Migration1794780961715(override val database: Database) : Migration {
    override val id: Long = 1794780961715

    override suspend fun Transaction.up() {
        val bankingId = ApplicationsTable.selectAll()
            .where { ApplicationsTable.name eq "BANKING" }
            .singleOrNull()
            ?.get(ApplicationsTable.id)?.value
            ?: error("SMA-613: BANKING application is missing; cannot update module flags")

        val mandatory = setOf("MANAGE_BANK_ACCOUNTS", "MANAGE_LEGAL_ENTITIES")
        val optional = setOf("MANAGE_SEPA", "MANAGE_DATEV")
        val present = ModulesTable.selectAll()
            .where { ModulesTable.applicationId eq bankingId }
            .map { it[ModulesTable.name] }.toSet()
        val missing = (mandatory + optional) - present
        check(missing.isEmpty()) {
            "SMA-613: BANKING modules missing: " + missing.sorted().joinToString(", ")
        }

        // Scope the update to BANKING and only rows that still need correction.
        // isMandatory expresses a runtime requirement, not a booking or grant.
        optional.forEach { name ->
            ModulesTable.update({
                (ModulesTable.applicationId eq bankingId) and
                    (ModulesTable.name eq name) and
                    (ModulesTable.isMandatory eq true)
            }) {
                it[ModulesTable.isMandatory] = false
                it[ModulesTable.modifiedBy] = UUID_ZERO
                it[ModulesTable.modifiedAt] = DateTime.now()
            }
        }
    }

    override suspend fun Database.down() {
        error("SMA-613: no automatic destructive down migration for catalog flag correction")
    }
}
