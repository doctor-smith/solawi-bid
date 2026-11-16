package org.solyton.solawi.bid.application.data.db.migrations

import kotlinx.coroutines.runBlocking
import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.selectAll
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

class BankingMandatoryFlagsMigrationTest {
    private val tables = arrayOf(ContextsTable, ApplicationsTable, ModulesTable)

    private fun Transaction.flags(applicationId: UUID): Map<String, Boolean> =
        ModulesTable.selectAll()
            .where { ModulesTable.applicationId eq applicationId }
            .associate { row -> row[ModulesTable.name] to row[ModulesTable.isMandatory] }

    @DbFunctional
    @Test
    fun optionalBankingModulesAreCorrectedWithoutChangingMandatoryOrOtherApps() =
        runSimpleH2Test(*tables) {
            createRootContext("EMPTY")
            val bankingId = createApplication("BANKING", "Banking", UUID_ZERO).id.value
            listOf("MANAGE_BANK_ACCOUNTS", "MANAGE_LEGAL_ENTITIES", "MANAGE_SEPA", "MANAGE_DATEV")
                .forEach { name ->
                    createModule(name, name, bankingId, UUID_ZERO, isMandatory = true)
                }
            val otherId = createApplication("OTHER", "Other", UUID_ZERO).id.value
            createModule("MANAGE_SEPA", "Unrelated module", otherId, UUID_ZERO, isMandatory = true)

            // Execute the migration body within the setup transaction so the
            // H2 in-memory schema and test data remain visible throughout.
            val migration = Migration1794780961715(db)
            val tx = this
            runBlocking {
                with(migration) {
                    tx.up()
                    tx.up() // Updating the flags twice must be idempotent.
                }
            }

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

    @DbFunctional
    @Test
    fun missingBankingModuleAbortsBeforeAnyFlagIsModified() =
        runSimpleH2Test(*tables) {
            createRootContext("EMPTY")
            val bankingId = createApplication("BANKING", "Banking", UUID_ZERO).id.value
            listOf("MANAGE_BANK_ACCOUNTS", "MANAGE_LEGAL_ENTITIES", "MANAGE_SEPA")
                .forEach { name ->
                    createModule(name, name, bankingId, UUID_ZERO, isMandatory = true)
                }

            val tx = this
            assertThrows<IllegalStateException> {
                runBlocking {
                    with(Migration1794780961715(db)) {
                        tx.up()
                    }
                }
            }

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
