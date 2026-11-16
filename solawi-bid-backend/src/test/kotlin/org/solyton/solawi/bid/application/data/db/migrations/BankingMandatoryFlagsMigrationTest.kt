package org.solyton.solawi.bid.application.data.db.migrations

import kotlinx.coroutines.runBlocking
import org.evoleq.exposedx.migrations.MigrationTable
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.application.repository.createApplication
import org.solyton.solawi.bid.module.application.repository.createModule
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable
import org.solyton.solawi.bid.module.application.schema.ModulesTable
import org.solyton.solawi.bid.module.permission.repository.createRootContext
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BankingMandatoryFlagsMigrationTest {
    private val tables = arrayOf(ContextsTable, ApplicationsTable, ModulesTable)

    /**
     * Keep the same in-memory H2 database alive across transactions.
     * runH2Test uses a DB URL without DB_CLOSE_DELAY=-1; H2 drops its schema
     * when the last connection closes between setup and migration execution.
     */
    private fun withMigrationDatabase(block: (Database) -> Unit) {
        val db = Database.connect(
            url = "jdbc:h2:mem:banking_mandatory_flags_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "root",
            password = ""
        )
        try {
            transaction(db) { SchemaUtils.create(*tables) }
            block(db)
        } finally {
            transaction(db) {
                SchemaUtils.drop(ModulesTable, ApplicationsTable, ContextsTable, MigrationTable)
            }
        }
    }

    private fun Transaction.flags(applicationId: UUID): Map<String, Boolean> =
        ModulesTable.selectAll()
            .where { ModulesTable.applicationId eq applicationId }
            .associate { row -> row[ModulesTable.name] to row[ModulesTable.isMandatory] }

    @DbFunctional
    @Test
    fun optionalBankingModulesAreCorrectedWithoutChangingMandatoryOrOtherApps() =
        withMigrationDatabase { db ->
            val (bankingId, otherId) = transaction(db) {
                createRootContext("EMPTY")
                val bankingId = createApplication("BANKING", "Banking", UUID_ZERO).id.value
                listOf("MANAGE_BANK_ACCOUNTS", "MANAGE_LEGAL_ENTITIES", "MANAGE_SEPA", "MANAGE_DATEV")
                    .forEach { name ->
                        createModule(name, name, bankingId, UUID_ZERO, isMandatory = true)
                    }
                val otherId = createApplication("OTHER", "Other", UUID_ZERO).id.value
                createModule("MANAGE_SEPA", "Unrelated module", otherId, UUID_ZERO, isMandatory = true)
                bankingId to otherId
            }

            runBlocking {
                val migration = Migration1794780961715(db)
                assertTrue(with(migration) { db.up().await() })
                assertFalse(with(migration) { db.up().await() }) // migration registry prevents a second execution
            }

            transaction(db) {
                assertEquals(
                    mapOf(
                        "MANAGE_BANK_ACCOUNTS" to true,
                        "MANAGE_LEGAL_ENTITIES" to true,
                        "MANAGE_SEPA" to false,
                        "MANAGE_DATEV" to false
                    ),
                    flags(bankingId)
                )
                assertEquals(mapOf("MANAGE_SEPA" to true), flags(otherId))
            }
        }

    @DbFunctional
    @Test
    fun missingBankingModuleAbortsBeforeAnyFlagIsModified() =
        withMigrationDatabase { db ->
            val bankingId = transaction(db) {
                createRootContext("EMPTY")
                val id = createApplication("BANKING", "Banking", UUID_ZERO).id.value
                listOf("MANAGE_BANK_ACCOUNTS", "MANAGE_LEGAL_ENTITIES", "MANAGE_SEPA")
                    .forEach { name ->
                        createModule(name, name, id, UUID_ZERO, isMandatory = true)
                    }
                id
            }
            assertThrows<IllegalStateException> {
                runBlocking { with(Migration1794780961715(db)) { db.up().await() } }
            }
            transaction(db) {
                assertEquals(
                    mapOf(
                        "MANAGE_BANK_ACCOUNTS" to true,
                        "MANAGE_LEGAL_ENTITIES" to true,
                        "MANAGE_SEPA" to true
                    ),
                    flags(bankingId)
                )
            }
        }
}
